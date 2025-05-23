package com.scrumly.integrationservice.dto;


import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationServiceDto {
    private Long id;
    private String connectionId;
    private String serviceName;
    private Long totalConnected;
    private Boolean isConnected;
    private Boolean isConnectionOwner;
}
