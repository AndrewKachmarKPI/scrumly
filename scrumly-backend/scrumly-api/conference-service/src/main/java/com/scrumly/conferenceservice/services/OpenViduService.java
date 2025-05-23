package com.scrumly.conferenceservice.services;

import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.utils.RetryException;
import io.openvidu.java.client.*;

import java.util.*;

public interface OpenViduService {
    Session createSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException;
    void closeSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException;

    Session getActiveOrCreateSession(String sessionId)  throws OpenViduJavaClientException, OpenViduHttpException, InterruptedException, RetryException;

    Connection createConnection(Session session, com.scrumly.conferenceservice.enums.ConnectionType connectionType,
                                OpenViduRole role, UserProfileDto userProfileDto)
            throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException;

    Connection getOrCreateConnection(Session session, com.scrumly.conferenceservice.enums.ConnectionType connectionType,
                                     OpenViduRole role, UserProfileDto userProfileDto)
            throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException;

    void closeConnections(String sessionId, String userId) throws OpenViduJavaClientException, OpenViduHttpException;

    boolean hasConnection(String sessionId, String userId);

    Set<String> getActiveConnectionsUserIds(String sessionId);
}
