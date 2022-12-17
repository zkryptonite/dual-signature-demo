package io.omonia.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

@Slf4j
@RestController
@RequestMapping("/bank")
public class BankController {
    @Autowired
    private PrivateKey caPrivateKey;

    @Autowired
    private DualSignatureService dualSignatureService;

    @Autowired
    private Certificate customerCert;

    private ResponseToBank globalResponseToBank;

    @PostMapping
    public ResponseEntity<String> validateTransaction(@RequestBody ResponseToBank responseToBank) throws CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, JsonProcessingException {
        globalResponseToBank = responseToBank;
        return ResponseEntity.ok("Ok");
    }

    @GetMapping("/step/{num}")
    public ResponseEntity<Step> handleStep(@PathVariable Integer num) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, JsonProcessingException {
        String encryptedMessageToBank = globalResponseToBank.getEncryptedMessageToBank();
        String decryptSessionKey = RSAUtil.decrypt(globalResponseToBank.getEncryptedSessionKey(), caPrivateKey);
        SecretKey sessionKey = AESUtil.convertStringToKey(decryptSessionKey);
        String decryptedMessageToBank = AESUtil.decrypt(encryptedMessageToBank, sessionKey);
        MessageToBank messageToBank = new ObjectMapper().readValue(decryptedMessageToBank, MessageToBank.class);

        return switch (num) {
            case 0 -> ResponseEntity.ok(

                    Step.builder()
                            .title("Step 0: Verify Customer's Certificate: ")
                            .originalContent(globalResponseToBank.getCustomerCert())
                            .transformedContent("")
                            .result("Certificate Verified")
                            .build()
            );

            case 1 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 1: Decrypt Encrypted Session Key: ")
                            .originalContent(globalResponseToBank.getEncryptedSessionKey())
                            .transformedContent(RSAUtil.decrypt(globalResponseToBank.getEncryptedSessionKey(), caPrivateKey))
                            .build()
            );

            case 2 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 2: Decrypt to receive the message sent from the client: ")
                            .originalContent(encryptedMessageToBank)
                            .transformedContent(decryptedMessageToBank)
                            .build()
            );

            case 3 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 3: Calculate Payment Information Message Digest (PIMD): ")
                            .originalContent(JsonMapper.toJsonString(messageToBank.getPaymentInfo()))
                            .transformedContent(dualSignatureService.calculatePIMD(messageToBank.getPaymentInfo()))
                            .build()
            );

            case 4 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 4: Calculate Payment Order Message Digest (POMD): ")
                            .originalContent(messageToBank.getOIMD() + dualSignatureService.calculatePIMD(messageToBank.getPaymentInfo()))
                            .transformedContent(dualSignatureService.calculatePOMD(messageToBank.getOIMD(),
                                    dualSignatureService.calculatePIMD(messageToBank.getPaymentInfo())))
                            .build()
            );

            case 5 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 5: Decrypt Dual Signature to get Payment Order Message Digest (POMD): ")
                            .originalContent(messageToBank.getDualSignature())
                            .transformedContent(RSAUtil.decrypt(messageToBank.getDualSignature(), customerCert.getPublicKey()))
                            .build()
            );

            case 6 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 6: Compare Payment Order Message Digest (POMD): ")
                            .originalContent(RSAUtil.decrypt(messageToBank.getDualSignature(), customerCert.getPublicKey()))
                            .transformedContent(dualSignatureService.calculatePOMD(messageToBank.getOIMD(),
                                    dualSignatureService.calculatePIMD(messageToBank.getPaymentInfo())))
                            .result("Dual Signature Verified")
                            .build()
            );

            default -> ResponseEntity.ok(Step.builder().build());
        };
    }
}
