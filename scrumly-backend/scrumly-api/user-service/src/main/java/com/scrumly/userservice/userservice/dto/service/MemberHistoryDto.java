package com.scrumly.userservice.userservice.dto.service;

import com.scrumly.userservice.userservice.enums.members.MemberChangeAction;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class MemberHistoryDto {
    private LocalDateTime dateTime;
    private String performedBy;
    private MemberStatus previousStatus;
    private MemberStatus newStatus;
    private MemberChangeAction changeAction;
}
