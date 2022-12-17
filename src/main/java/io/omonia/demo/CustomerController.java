package io.omonia.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private DualSignatureService dualSignatureService;

    @Autowired
    private X509Certificate rootCACert;

    @Autowired
    private PrivateKey caPrivateKey;

    private PurchaseRequest globalPurchaseRequest;

    private ResponseToMerchant globalResponseToMerchant;

    @PostMapping
    public ResponseEntity<ResponseToMerchant> createTransaction(@RequestBody PurchaseRequest purchaseRequest)
            throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        globalPurchaseRequest = purchaseRequest;
        //String uuid = UUID.randomUUID().toString();
        String uuid = "3527c442-b6af-42b4-9b37-aad7d9cd6a1e";
        OrderInfo orderInfo = purchaseRequest.getOrderInfo();
        PaymentInfo paymentInfo = purchaseRequest.getPaymentInfo();

        orderInfo.setTransactionId(uuid);
        paymentInfo.setTransactionId(uuid);



        String oimd = dualSignatureService.calculateOIMD(orderInfo);
        String pimd = dualSignatureService.calculatePIMD(paymentInfo);
        String pomd = dualSignatureService.calculatePOMD(oimd, pimd);
        String signature = dualSignatureService.calculateSignature(pomd);

        SecretKey sessionKey = AESUtil.generateKey(256);
        String encodedSessionKey = AESUtil.convertKeyToString(sessionKey);
        String encryptedSessionKey = RSAUtil.encrypt(encodedSessionKey, rootCACert.getPublicKey());

        MessageToBank messageToBank = MessageToBank.builder()
                .paymentInfo(paymentInfo)
                .OIMD(oimd)
                .dualSignature(signature)
                .build();
        String messageToBankJsonString = JsonMapper.toJsonString(messageToBank);
        String encryptedMessageToBank = AESUtil.encrypt(messageToBankJsonString, sessionKey);

        String customerCert = FileUtil.readFileToString("src/main/resources/static/certificate/customer-cert.pem");

        ResponseToMerchant response = ResponseToMerchant.builder()
                .orderInfo(orderInfo)
                .PIMD(pimd)
                .dualSignature(signature)
                .encryptedSessionKey(encryptedSessionKey)
                .encryptedMessageToBank(encryptedMessageToBank)
                .customerCert(customerCert).build();
        globalResponseToMerchant = response;
        return ResponseEntity.ok(response);
    }

    @GetMapping("/step/{num}")
    public ResponseEntity<Step> handleStep(@PathVariable Integer num)
            throws JsonProcessingException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        OrderInfo orderInfo = globalPurchaseRequest.getOrderInfo();
        PaymentInfo paymentInfo = globalPurchaseRequest.getPaymentInfo();
        String oimd = dualSignatureService.calculateOIMD(orderInfo);
        String pimd = globalResponseToMerchant.getPIMD();
        String pomd = dualSignatureService.calculatePOMD(oimd, pimd);
        String ds = globalResponseToMerchant.getDualSignature();

        return switch (num) {
            case 1 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 1: Calculate Order Information Message Digest (OIMD): ")
                            .originalContent(JsonMapper.toJsonString(orderInfo))
                            .transformedContent(oimd)
                            .build()
            );
            case 2 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 2: Calculate Payment Information Message Digest (PIMD): ")
                            .originalContent(JsonMapper.toJsonString(paymentInfo))
                            .transformedContent(pimd)
                            .build()
            );
            case 3 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 3: Calculate Payment Order Message Digest (POMD): ")
                            .originalContent(oimd + pimd)
                            .transformedContent(dualSignatureService.calculatePOMD(oimd, pimd))
                            .build()
            );
            case 4 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 4: Calculate Dual Signature using Customer's Private Key: ")
                            .originalContent(pomd)
                            .transformedContent(globalResponseToMerchant.getDualSignature())
                            .build()
            );
            case 5 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 5: Generate Session Key and Encrypt it by Bank's Public Key: ")
                            .originalContent(RSAUtil.decrypt(globalResponseToMerchant.getEncryptedSessionKey(), caPrivateKey))
                            .transformedContent(globalResponseToMerchant.getEncryptedSessionKey())
                            .build()
            );
            case 6 -> ResponseEntity.ok(
                    Step.builder()
                            .title("Step 6: Encrypt {OIMD, Payment Information, Dual Signature} by Session Key: ")
                            .originalContent(JsonMapper.toJsonString(
                                    MessageToBank.builder()
                                            .OIMD(oimd)
                                            .paymentInfo(paymentInfo)
                                            .dualSignature(ds)
                                            .build()
                            ))
                            .transformedContent(globalResponseToMerchant.getEncryptedMessageToBank())
                            .build()
            );

            case 7 -> ResponseEntity.ok(
                    Step.builder()
                    .title("Step 7: Send Result to Merchant: ")
                    .originalContent(JsonMapper.toJsonString(globalResponseToMerchant))
                    .transformedContent("")
                    .build()
            );

            default -> ResponseEntity.ok(null);
        };
    }
}
