package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class OrganizationInfoRQ {
    @Size(max = 200)
    @SqlInjectionSafe
    private String name;
    @Size(max = 1000)
    @SqlInjectionSafe
    private String about;
    private OrganizationStatus status;
    private boolean isRemoveLogo;
}
