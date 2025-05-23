package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InviteMembersRQ {
    @NotEmpty
    @Size(min = 1, max = 10)
    private List<@NotNull @NotBlank @SqlInjectionSafe String> usernames;
}
