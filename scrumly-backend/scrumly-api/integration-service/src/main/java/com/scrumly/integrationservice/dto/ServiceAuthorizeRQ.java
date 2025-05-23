package com.scrumly.integrationservice.dto;

import com.scrumly.enums.integration.ServiceType;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAuthorizeRQ {
    private ServiceType serviceType;
    private String connectingId;
    private String code;
}
