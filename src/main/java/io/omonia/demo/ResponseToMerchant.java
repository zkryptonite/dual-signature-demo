package io.omonia.demo;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseToMerchant {
   private OrderInfo orderInfo;
   private String PIMD;
   private String dualSignature;
   private String encryptedMessageToBank;
   private String encryptedSessionKey;
   private String customerCert;
}
