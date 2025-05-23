package com.scrumly.userservice.userservice.dto.service.invite;

import com.scrumly.userservice.userservice.enums.invite.InviteChangeAction;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class InviteHistoryDto {
    private Long id;
    private LocalDateTime dateTime;
    private String performedBy;
    private InviteStatus previousStatus;
    private InviteStatus newStatus;
    private InviteChangeAction changeAction;
}
