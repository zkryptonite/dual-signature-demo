package io.omonia.demo;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Step {
    private String title;
    private String originalContent;
    private String transformedContent;
    private String result;
}
