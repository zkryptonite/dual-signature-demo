package io.omonia.demo;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private String transactionId;
    private String itemName;
    private Double total;
}
