package com.scrumly.integrationservice.dto;

import com.scrumly.enums.integration.ServiceType;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCredentialsDto {
    private ServiceType serviceType;
    private String connectionId;
    private String connectionOwner;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
    private String scope;
}
