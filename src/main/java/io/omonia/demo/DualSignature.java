package io.omonia.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DualSignature {
    private String PIMD;
    private String OIMD;
    private String POMD;
    private String signature;
}

