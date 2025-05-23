package com.scrumly.conferenceservice.dto;

import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.enums.ConnectionType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ConnectionServerDataDto {
    private ConnectionType connectionType;
    private String userId;
}
