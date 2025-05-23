package com.scrumly.integrationservice.domain;

import com.scrumly.enums.integration.ServiceType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ServiceCredentialsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    @Column(nullable = false)
    private String connectionId;
    @Column(nullable = false)
    private String connectionOwner;
    @Lob
    private String accessToken;
    @Lob
    private String tokenType;
    @Column
    private Long expiresIn;
    @Lob
    private String refreshToken;
    @Lob
    private String scope;
}
