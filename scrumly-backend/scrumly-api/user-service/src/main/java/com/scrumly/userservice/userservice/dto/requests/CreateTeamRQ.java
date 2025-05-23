package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class CreateTeamRQ {
    @NotNull
    @NotBlank
    @NotEmpty
    private String organizationId;
    @NotNull
    @NotBlank
    @NotEmpty
    @Size(max = 200)
    @SqlInjectionSafe
    private String teamName;
    @NotEmpty
    private List<@NotNull @NotBlank @NotEmpty String> inviteMembers = new ArrayList<>();
}
