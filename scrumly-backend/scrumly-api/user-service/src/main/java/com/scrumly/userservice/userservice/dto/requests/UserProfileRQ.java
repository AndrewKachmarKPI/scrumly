package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.userservice.userservice.utils.ValidFile;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileRQ {
    @NotNull
    @NotBlank
    @NotEmpty
    @SqlInjectionSafe
    private String firstName;
    @NotNull
    @NotBlank
    @NotEmpty
    @SqlInjectionSafe
    private String lastName;
    @Size(max = 300)
    @SqlInjectionSafe
    private String bio;
    @Size(max = 15)
    @SqlInjectionSafe
    private String phoneNumber;
    @NotNull
    private LocalDate dateOfBirth;
    @Size(max = 10)
    private List<@SqlInjectionSafe @NotBlank String> skills;
    private Boolean isRemoveAvatar;
}
