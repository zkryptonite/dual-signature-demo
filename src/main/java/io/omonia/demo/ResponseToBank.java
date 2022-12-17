package io.omonia.demo;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseToBank {
    public String dualSignature;
    public String customerCert;
    public String encryptedMessageToBank;
    public String encryptedSessionKey;
}
