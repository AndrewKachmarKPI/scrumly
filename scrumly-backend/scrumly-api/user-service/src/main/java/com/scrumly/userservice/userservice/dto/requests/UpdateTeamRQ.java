package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.userservice.userservice.dto.service.team.TeamMemberDto;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UpdateTeamRQ {
    @Size(max = 200)
    @SqlInjectionSafe
    private String teamName;
    private List<UpdateTeamMemberRQ> updateMembers;
}
