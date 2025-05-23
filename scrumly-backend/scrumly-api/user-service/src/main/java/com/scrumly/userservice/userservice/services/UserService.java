package com.scrumly.userservice.userservice.services;

import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.requests.UserProfileRQ;
import com.scrumly.userservice.userservice.dto.requests.UserRegisterRQ;
import com.scrumly.userservice.userservice.dto.service.user.OrganizationConnectionDto;
import com.scrumly.userservice.userservice.dto.service.user.UserInfoDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface UserService {
    void registerUser(UserRegisterRQ userRegisterRQ);

    UserProfileDto findUserProfile(String username);

    UserInfoDto findUserInfoProfile(String username);

    List<UserInfoDto> findUserInfoProfileAutocomplete(String query);
    List<UserInfoDto> findUserInfoProfileAutocompleteOrg(String query, String orgId);

    List<UserProfileDto> findUserProfiles(Set<String> usernames);

    UserProfileDto updateUserProfile(String username, UserProfileRQ userProfileRQ, MultipartFile profileImage);

    UserProfileEntity getUserProfileOrThrow(String username);

    UserProfileEntity getUserProfileByEmailOrThrow(String username);

    UserProfileEntity getUserProfileOrNull(String username);


    void changeOrganizationConnection(String username, OrganizationConnectionDto connectionDto);

    void removeOrganizationConnection(String username, String orgId);
    void removeAllOrganizationConnection(String orgId);

    List<UserProfileEntity> getProfilesByUsername(List<String> usernames);
}
