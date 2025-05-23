package com.scrumly.userservice.userservice.api;

import com.scrumly.userservice.userservice.dto.requests.UserProfileRQ;
import com.scrumly.userservice.userservice.dto.requests.UserRegisterRQ;
import com.scrumly.userservice.userservice.dto.service.user.UserInfoDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.services.UserService;
import com.scrumly.userservice.userservice.utils.SecurityUtils;
import com.scrumly.userservice.userservice.utils.ValidFile;
import com.scrumly.validations.SqlInjectionSafe;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegisterRQ userRegisterRQ) {
        userService.registerUser(userRegisterRQ);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> findUserProfile() {
        return ResponseEntity.ok(userService.findUserProfile(SecurityUtils.getUsername()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDto> findUserProfile(@PathVariable("username")
                                                          @NotNull @NotEmpty @NotBlank
                                                          @SqlInjectionSafe String username) {
        return ResponseEntity.ok(userService.findUserProfile(username));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileDto> loadUserProfile(@PathVariable("username")
                                                          @NotNull @NotEmpty @NotBlank
                                                          @SqlInjectionSafe String username) {
        return ResponseEntity.ok(userService.findUserProfile(username));
    }


    @GetMapping("/{username}/info")
    public ResponseEntity<UserInfoDto> findUserInfoProfile(@PathVariable("username")
                                                           @NotNull @NotEmpty @NotBlank
                                                           @SqlInjectionSafe String username) {
        return ResponseEntity.ok(userService.findUserInfoProfile(username));
    }

    @GetMapping("/{query}/autocomplete")
    public ResponseEntity<List<UserInfoDto>> findUserInfoProfileAutocomplete(@PathVariable("query")
                                                                             @NotNull @NotEmpty @NotBlank
                                                                             @SqlInjectionSafe String username) {
        return ResponseEntity.ok(userService.findUserInfoProfileAutocomplete(username));
    }

    @GetMapping("/{query}/autocomplete/{orgId}/org")
    public ResponseEntity<List<UserInfoDto>> findUserInfoProfileAutocompleteOrg(@PathVariable("query")
                                                                                @NotNull @NotEmpty @NotBlank
                                                                                @SqlInjectionSafe String username,
                                                                                @PathVariable("orgId")
                                                                                @NotNull @NotEmpty @NotBlank
                                                                                @SqlInjectionSafe String orgId) {
        return ResponseEntity.ok(userService.findUserInfoProfileAutocompleteOrg(username, orgId));
    }


    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateUserProfile(@RequestPart("userProfileRQ") @Valid UserProfileRQ userProfileRQ,
                                                            @RequestPart(value = "profileImage", required = false) @ValidFile MultipartFile profileImage) {
        return ResponseEntity.ok(userService.updateUserProfile(SecurityUtils.getUsername(), userProfileRQ, profileImage));
    }

    @PostMapping("/users")
    public ResponseEntity<List<UserProfileDto>> findUsers(@RequestBody Set<String> userIds) {
        return ResponseEntity.ok(userService.findUserProfiles(userIds));
    }
}
