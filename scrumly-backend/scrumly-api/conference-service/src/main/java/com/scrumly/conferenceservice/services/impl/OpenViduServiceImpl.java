package com.scrumly.conferenceservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scrumly.conferenceservice.dto.ConnectionServerDataDto;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.enums.ConnectionType;
import com.scrumly.conferenceservice.services.OpenViduService;
import com.scrumly.conferenceservice.utils.CodeGenerator;
import com.scrumly.conferenceservice.utils.RetryException;
import com.scrumly.conferenceservice.utils.RetryOptions;
import io.openvidu.java.client.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.scrumly.conferenceservice.utils.SecurityUtils.getUsername;

@Service
public class OpenViduServiceImpl implements OpenViduService {
    private final OpenVidu openvidu;
    private final ObjectMapper objectMapper;

    @Autowired
    public OpenViduServiceImpl(OpenVidu openvidu) {
        this.openvidu = openvidu;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Session createSession(String sessionId)
            throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException {
        RetryOptions retryOptions = new RetryOptions();
        return createSession(sessionId, retryOptions);
    }

    @Override
    public void closeSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        try {
            Session session = getSessionById(sessionId);
            session.fetch();
            session.close();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Session getActiveOrCreateSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException {
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return createSession(sessionId);
        }
        return session;
    }

    private Session createSession(String sessionId, RetryOptions retryOptions)
            throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException {
        while (retryOptions.canRetry()) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("customSessionId", sessionId);
                SessionProperties properties = SessionProperties.fromJson(params).build();
                Session session = openvidu.createSession(properties);
                session.fetch();
                return session;
            } catch (OpenViduHttpException e) {
                if ((e.getStatus() >= 500 && e.getStatus() <= 504) || e.getStatus() == 404) {
                    System.err.println("Error creating session: " + e.getMessage()
                                               + ". Retrying session creation..." + retryOptions.toString());
                    retryOptions.retrySleep();
                } else {
                    System.err.println("Error creating session: " + e.getMessage());
                    throw e;
                }
            }
        }
        throw new RetryException("Max retries exceeded");
    }

    public Connection createConnection(Session session, com.scrumly.conferenceservice.enums.ConnectionType connectionType,
                                       OpenViduRole role, UserProfileDto userProfileDto)
            throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
        return createConnection(session, connectionType, role, new RetryOptions(), userProfileDto);
    }

    @Override
    public Connection getOrCreateConnection(Session session, com.scrumly.conferenceservice.enums.ConnectionType connectionType,
                                            OpenViduRole role, UserProfileDto userProfileDto) throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
        String userId = getUsername();
        Connection connection = getSessionConnection(session, userId, connectionType);
        if (connection != null) {
            return connection;
        }
        return createConnection(session, connectionType, role, userProfileDto);
    }

    @Override
    public void closeConnections(String sessionId, String userId) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = getSessionById(sessionId);
        List<Connection> screenConnection = getUserConnections(session, userId);
        try {
            for (Connection connection : screenConnection) {
                session.forceDisconnect(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Session getSessionById(String sessionId) {
        return openvidu.getActiveSessions().stream()
                .filter(ses -> ses.getProperties().customSessionId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Session with id: " + sessionId + " is not found"));
    }

    private Session getSessionByIdOrNull(String sessionId) {
        return openvidu.getActiveSessions().stream()
                .filter(ses -> ses.getProperties().customSessionId().equals(sessionId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean hasConnection(String sessionId, String userId) {
        Session session = getSessionByIdOrNull(sessionId);
        if (session == null) {
            return false;
        }
        try {
            session.fetch();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
        }
        Connection screenConnection = getSessionActiveConnection(session, userId, ConnectionType.SCREEN);
        Connection cameraConnection = getSessionActiveConnection(session, userId, ConnectionType.CAMERA);
        return isConnectionActive(screenConnection) || isConnectionActive(cameraConnection);
    }

    @Override
    public Set<String> getActiveConnectionsUserIds(String sessionId) {
        Session session = getSessionByIdOrNull(sessionId);
        if (session == null) {
            return new HashSet<>();
        }
        try {
            session.fetch();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
        }
        return session.getActiveConnections().stream()
                .filter(this::isConnectionActive)
                .map(connection -> CodeGenerator.getConnectionData(connection.getServerData()))
                .map(ConnectionServerDataDto::getUserId)
                .collect(Collectors.toSet());
    }

    public boolean isConnectionActive(Connection connection) {
        return connection != null && connection.getStatus().equalsIgnoreCase("active");
    }

    private Connection getSessionActiveConnection(Session session, String userId, ConnectionType connectionType) {
        for (Connection connection : session.getActiveConnections()) {
            ConnectionServerDataDto data = CodeGenerator.getConnectionData(connection.getServerData());
            if (data.getConnectionType().equals(connectionType) && data.getUserId().equals(userId)) {
                return connection;
            }
        }
        return null;
    }

    private Connection getSessionConnection(Session session, String userId, ConnectionType connectionType) {
        for (Connection connection : session.getConnections()) {
            ConnectionServerDataDto data = CodeGenerator.getConnectionData(connection.getServerData());
            if (data.getConnectionType().equals(connectionType) && data.getUserId().equals(userId)) {
                return connection;
            }
        }
        return null;
    }

    private List<Connection> getUserConnections(Session session, String userId) {
        List<Connection> connections = new ArrayList<>();
        for (Connection connection : session.getConnections()) {
            ConnectionServerDataDto data = CodeGenerator.getConnectionData(connection.getServerData());
            if (data.getUserId().equals(userId)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    private Connection createConnection(Session session, com.scrumly.conferenceservice.enums.ConnectionType connectionType,
                                        OpenViduRole role, RetryOptions retryOptions, UserProfileDto profileDto)
            throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> connectionData = new HashMap<String, Object>();

        if (connectionType != null) {
            connectionData.put("connectionType", connectionType.toString());
            connectionData.put("userId", getUsername());
            try {
                connectionData.put("profileDto", objectMapper.writeValueAsString(profileDto));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        params.put("role", role.name());
        params.put("data", connectionData.toString());
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();

        Connection connection = null;
        while (retryOptions.canRetry()) {
            try {
                connection = session.createConnection(properties);
                break;
            } catch (OpenViduHttpException e) {
                if (e.getStatus() >= 500 && e.getStatus() <= 504) {
                    // Retry is used for OpenVidu Enterprise High Availability for reconnecting purposes
                    // to allow fault tolerance
                    System.err.println("Error creating connection: " + e.getMessage()
                                               + ". Retrying connection creation..." + retryOptions.toString());
                    retryOptions.retrySleep();
                } else {
                    System.err.println("Error creating connection: " + e.getMessage());
                    throw e;
                }
            }
        }

        if (connection == null) {
            throw new RetryException("Max retries exceeded");
        }
        return connection;
    }
}
