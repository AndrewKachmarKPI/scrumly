package com.scrumly.userservice.userservice.dto.service.user;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarId;
}
