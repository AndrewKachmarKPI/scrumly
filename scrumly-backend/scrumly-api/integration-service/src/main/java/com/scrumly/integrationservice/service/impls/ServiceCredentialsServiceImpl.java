package com.scrumly.integrationservice.service.impls;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integrationservice.domain.ServiceCredentialsEntity;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.repository.UserCredentialsRepository;
import com.scrumly.integrationservice.service.EncryptService;
import com.scrumly.integrationservice.service.ServiceCredentialsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.scrumly.integrationservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class ServiceCredentialsServiceImpl implements ServiceCredentialsService {
    private final UserCredentialsRepository credentialsRepository;
    private final EncryptService encryptService;

    @Override
    public void saveCredentials(ServiceCredentialsDto credentialsDto) {
        ServiceCredentialsEntity credentials = credentialsRepository
                .findByConnectionIdAndConnectionOwnerAndServiceType(credentialsDto.getConnectionId(), getUsername(), credentialsDto.getServiceType());
        if (credentials != null) {
            throw new DuplicateEntityException(ServiceErrorCode.DUPLICATED_CREDENTIALS);
        }
        ServiceCredentialsEntity entity = ServiceCredentialsEntity.builder()
                .serviceType(credentialsDto.getServiceType())
                .connectionId(credentialsDto.getConnectionId())
                .connectionOwner(getUsername())
                .accessToken(encryptService.encrypt(credentialsDto.getAccessToken()))
                .tokenType(encryptService.encrypt(credentialsDto.getTokenType()))
                .expiresIn(credentialsDto.getExpiresIn())
                .refreshToken(encryptService.encrypt(credentialsDto.getRefreshToken()))
                .scope(encryptService.encrypt(credentialsDto.getScope()))
                .build();
        credentialsRepository.save(entity);
    }

    @Override
    public void updateCredentials(ServiceCredentialsDto credentialsDto) {
        ServiceCredentialsEntity credentials = findCredentialsByConnectionAndType(credentialsDto.getConnectionId(), credentialsDto.getServiceType());
        if (!credentials.getConnectionOwner().equals(getUsername())) {
            throw new ServiceErrorException(ServiceErrorCode.ILLEGAL_CREDENTIAL_OWNER);
        }
        updateCredentials(credentialsDto, credentials);
    }

    @Override
    public void updateCredentialsWithoutOwner(ServiceCredentialsDto credentialsDto) {
        ServiceCredentialsEntity credentials = findCredentialsByConnectionAndTypeWithoutOwner(credentialsDto.getConnectionId(), credentialsDto.getServiceType());
        updateCredentials(credentialsDto, credentials);
    }

    private void updateCredentials(ServiceCredentialsDto credentialsDto,
                                   ServiceCredentialsEntity credentials) {
        ServiceCredentialsEntity updatedCredentials = credentials.toBuilder()
                .accessToken(Optional.ofNullable(credentialsDto.getAccessToken())
                                     .map(encryptService::encrypt)
                                     .orElse(credentials.getAccessToken()))
                .expiresIn(Optional.ofNullable(credentialsDto.getExpiresIn()).orElse(credentials.getExpiresIn()))
                .refreshToken(Optional.ofNullable(credentialsDto.getRefreshToken())
                                      .map(encryptService::encrypt)
                                      .orElse(credentials.getRefreshToken()))
                .scope(Optional.ofNullable(credentialsDto.getScope())
                               .map(encryptService::encrypt)
                               .orElse(credentials.getScope()))
                .build();
        credentialsRepository.save(updatedCredentials);
    }

    @Override
    public void removeCredentials(String connectionId, ServiceType serviceType) {
        ServiceCredentialsEntity credentials = findCredentialsByConnectionAndType(connectionId, serviceType);
        if (!credentials.getConnectionOwner().equals(getUsername())) {
            throw new ServiceErrorException(ServiceErrorCode.ILLEGAL_CREDENTIAL_OWNER);
        }
        credentialsRepository.delete(credentials);
    }

    @Override
    public Boolean isConnected(String connectionId, ServiceType serviceType) {
        return credentialsRepository.findByConnectionIdAndServiceType(connectionId, serviceType) != null;
    }

    @Override
    public Boolean isConnectionOwner(String connectionId, ServiceType serviceType) {
        return credentialsRepository.findByConnectionIdAndConnectionOwnerAndServiceType(connectionId, getUsername(), serviceType) != null;
    }

    @Override
    public ServiceCredentialsDto findCredentials(String connectionId, ServiceType serviceType) {
        ServiceCredentialsEntity credentials = findCredentialsByConnectionAndType(connectionId, serviceType);
        if (!credentials.getConnectionOwner().equals(getUsername())) {
            throw new ServiceErrorException(ServiceErrorCode.ILLEGAL_CREDENTIAL_OWNER);
        }
        return getServiceCredentialsDto(credentials);
    }

    @Override
    public ServiceCredentialsDto findCredentialsWithoutOwner(String connectionId, ServiceType serviceType) {
        ServiceCredentialsEntity credentials = findCredentialsByConnectionAndTypeWithoutOwner(connectionId, serviceType);
        return getServiceCredentialsDto(credentials);
    }

    private ServiceCredentialsDto getServiceCredentialsDto(ServiceCredentialsEntity credentials) {
        return ServiceCredentialsDto.builder()
                .serviceType(credentials.getServiceType())
                .connectionId(credentials.getConnectionId())
                .connectionOwner(credentials.getConnectionOwner())
                .accessToken(encryptService.decrypt(credentials.getAccessToken()))
                .tokenType(encryptService.decrypt(credentials.getTokenType()))
                .expiresIn(credentials.getExpiresIn())
                .refreshToken(encryptService.decrypt(credentials.getRefreshToken()))
                .scope(encryptService.decrypt(credentials.getScope()))
                .build();
    }

    private ServiceCredentialsEntity findCredentialsByConnectionAndType(String connectionId, ServiceType serviceType) {
        ServiceCredentialsEntity credentials = credentialsRepository.findByConnectionIdAndConnectionOwnerAndServiceType(connectionId, getUsername(), serviceType);
        if (credentials == null) {
            throw new EntityNotFoundException(ServiceErrorCode.CREDENTIALS_NOTFOUND);
        }
        return credentials;
    }

    private ServiceCredentialsEntity findCredentialsByConnectionAndTypeWithoutOwner(String connectionId, ServiceType serviceType) {
        ServiceCredentialsEntity credentials = credentialsRepository.findByConnectionIdAndServiceType(connectionId, serviceType);
        if (credentials == null) {
            throw new EntityNotFoundException(ServiceErrorCode.CREDENTIALS_NOTFOUND);
        }
        return credentials;
    }
}
