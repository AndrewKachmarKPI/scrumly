package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreateOrganizationRQ {
    @NotNull
    @NotBlank
    @NotEmpty
    @Size(max = 200)
    @SqlInjectionSafe
    private String organizationName;
    @Size(max = 200)
    @SqlInjectionSafe
    private String teamName;
    private List<@NotNull @NotBlank String> inviteMembers;
}
