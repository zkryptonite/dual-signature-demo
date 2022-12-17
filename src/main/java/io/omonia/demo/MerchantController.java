package io.omonia.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
@RestController
@RequestMapping("/merchant")
public class MerchantController {
    @Autowired
    private DualSignatureService dualSignatureService;

    @Autowired
    private X509Certificate rootCACert;

    private ResponseToMerchant globalResponseToMerchant;

    @PostMapping
    public ResponseEntity<ResponseToMerchant> handlePurchaseRequest(@RequestBody ResponseToMerchant responseToMerchant) {
        log.info(responseToMerchant.toString());
        globalResponseToMerchant = responseToMerchant;
        return ResponseEntity.ok(responseToMerchant);
    }

    @GetMapping("/step/{num}")
    public ResponseEntity<Step> handleStep(@PathVariable Integer num) throws NoSuchAlgorithmException, JsonProcessingException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, CertificateException, SignatureException, NoSuchProviderException {
        String oimd = dualSignatureService.calculateOIMD(globalResponseToMerchant.getOrderInfo());
        String pimd = globalResponseToMerchant.getPIMD();
        String ds = globalResponseToMerchant.getDualSignature();

        X509Certificate customerCert = KeyAndCertUtil.readCertificate(globalResponseToMerchant.getCustomerCert());
        customerCert.verify(rootCACert.getPublicKey());

        return switch (num) {
            case 0 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 0: Verify Customer's Certificate: ")
                            .originalContent(globalResponseToMerchant.getCustomerCert())
                            .transformedContent("")
                            .result("Certificate Verified")
                            .build()
            );

            case 1 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 1: Calculate Order Information Message Digest (OIMD): ")
                            .originalContent(JsonMapper.toJsonString(globalResponseToMerchant.getOrderInfo()))
                            .transformedContent(oimd)
                            .build()
            );

            case 2 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 2: Calculate Payment Order Message Digest (POMD): ")
                            .originalContent(oimd + pimd)
                            .transformedContent(dualSignatureService.calculatePOMD(oimd, pimd))
                            .build()
            );

            case 3 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 3: Decrypt Dual Signature to get Payment Order Message Digest (POMD): ")
                            .originalContent(ds)
                            .transformedContent(RSAUtil.decrypt(ds, customerCert.getPublicKey()))
                            .build()
            );

            case 4 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 4: Compare Payment Order Message Digest (POMD): ")
                            .originalContent(RSAUtil.decrypt(ds, customerCert.getPublicKey()))
                            .transformedContent(dualSignatureService.calculatePOMD(oimd, pimd))
                            .result("Dual Signature Verified")
                            .build()
            );

            case 5 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 5: Send Result to Bank: ")
                            .originalContent(JsonMapper.toJsonString(ResponseToBank.builder()
                                            .dualSignature(globalResponseToMerchant.getDualSignature())
                                            .encryptedMessageToBank(globalResponseToMerchant.getEncryptedMessageToBank())
                                            .encryptedSessionKey(globalResponseToMerchant.getEncryptedSessionKey())
                                            .customerCert(globalResponseToMerchant.getCustomerCert()).build()))
                            .transformedContent("")
                            .build()
            );

            default -> ResponseEntity.ok(null);
        };
    }
}
