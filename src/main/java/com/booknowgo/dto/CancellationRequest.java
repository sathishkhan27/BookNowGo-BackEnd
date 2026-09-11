package com.booknowgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationRequest {
    @NotBlank(message = "Reason for cancellation is required")
    private String reason;
}
