package io.omonia.demo;


import org.apache.tomcat.util.codec.binary.Base64;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyAndCertUtil {
    public static RSAPrivateKey readPrivateKey(File file) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = Files.readString(file.toPath(), Charset.defaultCharset());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static X509Certificate readCertificate(File file) throws CertificateException, FileNotFoundException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new FileInputStream(file);
        return (X509Certificate) certificateFactory.generateCertificate(inputStream);
    }

    public static X509Certificate readCertificate(String certString) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new ByteArrayInputStream(certString.getBytes());
        return (X509Certificate) certificateFactory.generateCertificate(inputStream);
    }
}
