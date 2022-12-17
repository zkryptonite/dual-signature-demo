package io.omonia.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

@Configuration
public class KeyAndCertConfig {
    @Bean
    public RSAPrivateKey customerPrivateKey() throws Exception {
        return KeyAndCertUtil.readPrivateKey(new File("src/main/resources/static/certificate/customer-key.pem"));
    }

    @Bean
    public RSAPrivateKey caPrivateKey() throws Exception {
        return KeyAndCertUtil.readPrivateKey(new File("src/main/resources/static/certificate/ca-key.pem"));
    }

    @Bean
    public X509Certificate rootCACert() throws FileNotFoundException, CertificateException {
        return KeyAndCertUtil.readCertificate(new File("src/main/resources/static/certificate/ca-cert.pem"));
    }

    @Bean
    public X509Certificate customerCert() throws FileNotFoundException, CertificateException {
        return KeyAndCertUtil.readCertificate(new File("src/main/resources/static/certificate/customer-cert.pem"));
    }
}
