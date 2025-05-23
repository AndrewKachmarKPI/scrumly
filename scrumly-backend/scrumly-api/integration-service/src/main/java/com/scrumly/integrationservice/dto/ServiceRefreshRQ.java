package com.scrumly.integrationservice.dto;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRefreshRQ {
    @NotNull
    private ServiceType serviceType;
    @NotNull
    @NotEmpty
    @NotBlank
    @SqlInjectionSafe
    private String connectingId;
}
