package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.user.OrganizationConnectionEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.keycloak.KeycloakRegisterUserDto;
import com.scrumly.userservice.userservice.dto.keycloak.KeycloakUserDto;
import com.scrumly.userservice.userservice.dto.requests.UserProfileRQ;
import com.scrumly.userservice.userservice.dto.requests.UserRegisterRQ;
import com.scrumly.userservice.userservice.dto.service.user.OrganizationConnectionDto;
import com.scrumly.userservice.userservice.dto.service.user.UserInfoDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.feign.IntegrationServiceFeignClient;
import com.scrumly.userservice.userservice.repository.OrganizationConnectionRepository;
import com.scrumly.userservice.userservice.repository.UserProfileEntityRepository;
import com.scrumly.userservice.userservice.services.ImageService;
import com.scrumly.userservice.userservice.services.KeycloakService;
import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final KeycloakService keycloakService;
    private final ModelMapper modelMapper;
    private final ImageService imageService;
    private final UserProfileEntityRepository profileRepository;
    private final OrganizationConnectionRepository organizationConnectionRepository;

    @Override
    @Transactional
    public void registerUser(UserRegisterRQ userRegisterRQ) {
        if (profileRepository.existsByEmail(userRegisterRQ.getEmail())) {
            throw new DuplicateEntityException(ServiceErrorCode.DUPLICATE_USER);
        }
        KeycloakUserDto keycloakUserDto = new KeycloakUserDto();
        try {
            keycloakUserDto = keycloakService.createUser(new KeycloakRegisterUserDto(userRegisterRQ));
            profileRepository.save(UserProfileEntity.builder()
                    .userId(keycloakUserDto.getId())
                    .email(keycloakUserDto.getEmail())
                    .firstName(keycloakUserDto.getFirstName())
                    .lastName(keycloakUserDto.getLastName())
                    .registered(LocalDateTime.now())
                    .lastActivity(LocalDateTime.now())
                    .dateOfBirth(userRegisterRQ.getDateOfBirth())
                    .build());
        } catch (Exception e) {
            if (keycloakUserDto.getId() != null) {
                keycloakService.deleteUser(keycloakUserDto.getId());
            }
            e.printStackTrace();
            throw new ServiceErrorException(ServiceErrorCode.FAILED_REGISTER_USER);
        }
    }

    @Override
    public UserProfileDto findUserProfile(String username) {
        UserProfileEntity profileEntity = findByUsername(username);
        return modelMapper.map(profileEntity, UserProfileDto.class);
    }

    @Override
    public UserInfoDto findUserInfoProfile(String username) {
        UserProfileEntity profileEntity = findByUsername(username);
        return modelMapper.map(profileEntity, UserInfoDto.class);
    }

    @Override
    public List<UserInfoDto> findUserInfoProfileAutocomplete(String query) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(new SearchFilter("firstName", SearchOperators.LIKE, CompareOption.OR, query));
        searchQuery.appendSearchFilter(new SearchFilter("lastName", SearchOperators.LIKE, CompareOption.OR, query));
        searchQuery.appendSearchFilter(new SearchFilter("email", SearchOperators.LIKE, CompareOption.OR, query));
        Page<UserProfileEntity> page = getUsersBySearchQuery(searchQuery);
        return page.getContent().stream()
                .map(profile -> modelMapper.map(profile, UserInfoDto.class))
                .toList();
    }

    @Override
    public List<UserInfoDto> findUserInfoProfileAutocompleteOrg(String query, String orgId) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(new SearchFilter("firstName", SearchOperators.LIKE, CompareOption.OR, query));
        searchQuery.appendSearchFilter(new SearchFilter("lastName", SearchOperators.LIKE, CompareOption.OR, query));
        searchQuery.appendSearchFilter(new SearchFilter("email", SearchOperators.LIKE, CompareOption.OR, query));
        Page<UserProfileEntity> page = getUsersBySearchQuery(searchQuery);
        return page.getContent().stream()
                .filter(profile -> profile.getConnectedOrganizations().stream()
                        .anyMatch(con -> con.getOrganizationId().equals(orgId) && con.getIsActive()))
                .map(profile -> modelMapper.map(profile, UserInfoDto.class))
                .toList();
    }

    public Page<UserProfileEntity> getUsersBySearchQuery(SearchQuery searchQuery) {
        Specification<UserProfileEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        return profileRepository.findAll(specification, pageable);
    }

    @Override
    public List<UserProfileDto> findUserProfiles(Set<String> usernames) {
        return usernames.stream()
                .map(username -> {
                    UserProfileEntity profileEntity = profileRepository.findByUserId(username);
                    if (profileEntity == null) {
                        profileEntity = profileRepository.findByEmail(username);
                    }
                    return profileEntity != null
                            ? modelMapper.map(profileEntity, UserProfileDto.class)
                            : null;
                }).toList();
    }

    @Override
    public UserProfileDto updateUserProfile(String username, UserProfileRQ userProfileRQ, MultipartFile profileImage) {
        UserProfileEntity profileEntity = findByUsername(username);
        if (userProfileRQ.getFirstName() != null) {
            profileEntity.setFirstName(userProfileRQ.getFirstName());
        }
        if (userProfileRQ.getLastName() != null) {
            profileEntity.setLastName(userProfileRQ.getLastName());
        }
        if (userProfileRQ.getBio() != null) {
            profileEntity.setBio(userProfileRQ.getBio());
        }
        if (userProfileRQ.getPhoneNumber() != null) {
            profileEntity.setPhoneNumber(userProfileRQ.getPhoneNumber());
        }
        if (userProfileRQ.getDateOfBirth() != null) {
            profileEntity.setDateOfBirth(userProfileRQ.getDateOfBirth());
        }
        if (userProfileRQ.getSkills() != null) {
            profileEntity.setSkills(userProfileRQ.getSkills());
        }
        if (profileImage != null) {
            if (profileEntity.getAvatar() != null) {
                imageService.deleteImage(profileEntity.getAvatar().getImageId());
            }
            profileEntity.setAvatar(imageService.saveImage(profileImage));
        }
        if (userProfileRQ.getIsRemoveAvatar() && profileEntity.getAvatar() != null) {
            imageService.deleteImage(profileEntity.getAvatar().getImageId());
            profileEntity.setAvatar(null);
        }
        profileEntity = profileRepository.save(profileEntity);

        UserProfileDto profileDto = modelMapper.map(profileEntity, UserProfileDto.class);
        if (profileEntity.getAvatar() != null) {
            profileDto.setAvatarId(profileEntity.getAvatar().getImageId());
        }

        keycloakService.updateUser(profileEntity.getEmail(), profileDto);

        return profileDto;
    }

    @Override
    public UserProfileEntity getUserProfileOrThrow(String username) {
        return findByUsername(username);
    }

    @Override
    public UserProfileEntity getUserProfileByEmailOrThrow(String email) {
        return findByEmail(email);
    }

    @Override
    public UserProfileEntity getUserProfileOrNull(String username) {
        return profileRepository.findByUserId(username);
    }


    @Override
    public void changeOrganizationConnection(String username, OrganizationConnectionDto connectionDto) {
        UserProfileEntity userProfileEntity = findByUsername(username);
        OrganizationConnectionEntity connection = userProfileEntity.getConnectedOrganizations()
                .stream().filter(con -> con.getOrganizationId().equals(connectionDto.getOrganizationId()))
                .findAny().orElse(null);
        if (connection == null) {
            userProfileEntity.getConnectedOrganizations()
                    .add(OrganizationConnectionEntity.builder()
                            .isActive(connectionDto.getIsActive())
                            .dateConnected(connectionDto.getDateConnected())
                            .organizationId(connectionDto.getOrganizationId())
                            .build());
        } else {
            connection.setDateConnected(connectionDto.getDateConnected());
            connection.setIsActive(connectionDto.getIsActive());
            connection.setOrganizationId(connectionDto.getOrganizationId());
            organizationConnectionRepository.save(connection);
        }
        profileRepository.save(userProfileEntity);
    }

    @Override
    public void removeOrganizationConnection(String username, String orgId) {
        UserProfileEntity userProfileEntity = findByUsername(username);
        userProfileEntity.getConnectedOrganizations()
                .removeIf(con -> con.getOrganizationId().equals(orgId));
        profileRepository.save(userProfileEntity);
    }

    @Override
    @Transactional
    public void removeAllOrganizationConnection(String orgId) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setPageSize(10000);
        searchQuery.appendSearchFilter(new SearchFilter("connectedOrganizations.organizationId", SearchOperators.EQUALS, CompareOption.AND, orgId));
        Specification<UserProfileEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<UserProfileEntity> page = profileRepository.findAll(specification, pageable);
        List<UserProfileEntity> profiles = page.getContent();
        for (UserProfileEntity userProfileEntity : profiles) {
            userProfileEntity.getConnectedOrganizations().removeIf(con -> con.getOrganizationId().equals(orgId));
        }
        profileRepository.saveAll(profiles);
    }

    @Override
    public List<UserProfileEntity> getProfilesByUsername(List<String> usernames) {
        return profileRepository.findAllByUserIdIn(usernames);
    }

    private UserProfileEntity findByUsername(String username) {
        UserProfileEntity profileEntity = profileRepository.findByUserId(username);
        if (profileEntity == null) {
            throw new EntityNotFoundException(ServiceErrorCode.USER_NOTFOUND);
        }
        return profileEntity;
    }

    private UserProfileEntity findByEmail(String email) {
        UserProfileEntity profileEntity = profileRepository.findByEmail(email);
        if (profileEntity == null) {
            throw new EntityNotFoundException(ServiceErrorCode.USER_NOTFOUND);
        }
        return profileEntity;
    }
}
