package io.omonia.demo;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {
    private String transactionId;
    private String cardNumber;
    private Integer cvv;
    private String expiryDate;
    private Double total;
}
