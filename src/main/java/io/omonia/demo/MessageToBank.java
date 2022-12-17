package io.omonia.demo;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MessageToBank {
    public String OIMD;
    public PaymentInfo paymentInfo;
    public String dualSignature;
}
