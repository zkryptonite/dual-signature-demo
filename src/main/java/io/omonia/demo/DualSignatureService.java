package io.omonia.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

@Service
public class DualSignatureService {
    @Autowired
    private RSAPrivateKey customerPrivateKey;

    public String calculateSignature(String pomd) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        return RSAUtil.encrypt(pomd, customerPrivateKey);
    }

    public String calculateOIMD(OrderInfo orderInfo) throws JsonProcessingException, NoSuchAlgorithmException {
        String orderInfoJsonString = JsonMapper.toJsonString(orderInfo);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(orderInfoJsonString.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    public String calculatePIMD(PaymentInfo paymentInfo) throws JsonProcessingException, NoSuchAlgorithmException {
        String paymentInfoJsonString = JsonMapper.toJsonString(paymentInfo);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(paymentInfoJsonString.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    public String calculatePOMD(String oimd, String pimd) throws NoSuchAlgorithmException {
        String pomd = oimd + pimd;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(pomd.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

}
