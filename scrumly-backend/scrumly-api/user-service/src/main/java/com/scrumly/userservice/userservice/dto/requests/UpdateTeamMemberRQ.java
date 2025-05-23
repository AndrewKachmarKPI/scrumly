package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.userservice.userservice.enums.TeamMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateTeamMemberRQ {
    @NotNull
    @NotBlank
    @NotEmpty
    private String userId;
    private TeamMemberRole memberRole;
    private String badge;
}
