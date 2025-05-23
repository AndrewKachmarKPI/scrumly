package com.scrumly.userservice.userservice.dto.requests;

import com.scrumly.validations.SqlInjectionSafe;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@AllArgsConstructor
public class UserRegisterRQ {
    @NotNull
    @NotBlank
    @Email
    @SqlInjectionSafe
    @Schema(example = "example@scrumly.com")
    private String email;
    @NotNull
    @NotBlank
    @SqlInjectionSafe
    @Schema(example = "Alex")
    private String firstName;
    @NotNull
    @NotBlank
    @SqlInjectionSafe
    @Schema(example = "Doe")
    private String lastName;
    @NotNull
    @NotBlank
    @SqlInjectionSafe
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
            message = "Password must be at least 10 characters long, and include at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    private String password;
    @NotNull
    @Past
    private LocalDate dateOfBirth;
}
