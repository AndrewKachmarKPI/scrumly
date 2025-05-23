package com.scrumly.integrationservice.service.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.dto.googleCalendar.CreateGoogleCalendarEventDto;
import com.scrumly.integrationservice.dto.googleCalendar.GetGoogleCalendarEventsDto;
import com.scrumly.integrationservice.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.integrationservice.enums.google.ClientApprovalPrompt;
import com.scrumly.integrationservice.service.ServiceCredentialsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.scrumly.integrationservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class GoogleCalendarServiceImpl implements GoogleCalendarService {
    private final ServiceCredentialsService credentialsService;
    private final ModelMapper modelMapper;

    @Value("${integration.google-calendar.client-id}")
    private String clientId;
    @Value("${integration.google-calendar.client-secret}")
    private String clientSecret;
    @Value("${integration.google-calendar.redirect-uri}")
    private String redirectUri;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static String DEFAULT_CALENDAR = "primary";

    @Override
    public String getAuthorizationUrl() {
        AuthorizationCodeRequestUrl authorizationUrl;
        try {
            GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow(ClientApprovalPrompt.AUTO);
            authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setAccessType("offline");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        if (authorizationUrl == null) {
            throw new ServiceErrorException(ServiceErrorCode.FAILED_AUTHORIZE_GOOGLE_CALENDAR);
        }
        return authorizationUrl.build();
    }


    @Override
    @Transactional
    public ServiceCredentialsDto authorize(ServiceAuthorizeRQ authorizeRQ) {
        try {
            GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow(ClientApprovalPrompt.AUTO);
            TokenResponse response = flow.newTokenRequest(authorizeRQ.getCode())
                    .setRedirectUri(redirectUri)
                    .execute();
            return ServiceCredentialsDto.builder()
                    .connectionId(authorizeRQ.getConnectingId())
                    .serviceType(ServiceType.GOOGLE_CALENDAR)
                    .accessToken(response.getAccessToken())
                    .tokenType(response.getTokenType())
                    .expiresIn(response.getExpiresInSeconds())
                    .refreshToken(response.getRefreshToken())
                    .scope(response.getScope())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public Events getCalendarEventList(GetGoogleCalendarEventsDto dto) {
        Events eventList = null;
        try {
            final DateTime startDate = new DateTime(dto.getStartDate());
            final DateTime endDate = new DateTime(dto.getEndDate());
            Calendar.Events events = getCalendarClient().events();
            eventList = events.list(DEFAULT_CALENDAR)
                    .setTimeMin(startDate)
                    .setTimeMax(endDate)
                    .setQ(dto.getQuery())
                    .setOrderBy(dto.getOrderBy())
                    .setSingleEvents(dto.getSingleEvents())
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        return eventList;
    }

    @Override
    public GoogleCalendarEventDto createCalendarEvent(CreateGoogleCalendarEventDto dto) {
        GoogleCalendarEventDto calendarEventDto = null;
        try {
            Calendar.Events service = getCalendarClient().events();

            Event event = createNewEventObject(null, dto);

            event = service
                    .insert(DEFAULT_CALENDAR, event)
                    .setConferenceDataVersion(1)
                    .setSendUpdates("all")
                    .execute();

            calendarEventDto = GoogleCalendarEventDto.builder()
                    .calendarEventId(event.getId())
                    .calendarEventUID(event.getICalUID())
                    .conferenceLink(event.getHangoutLink())
                    .eventLink(event.getHtmlLink())
                    .recurringEventId(event.getId())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        return calendarEventDto;
    }

    private static Event createNewEventObject(Event event, CreateGoogleCalendarEventDto dto) {
        if (event == null) {
            event = new Event();
        }
        event
                .setSummary(dto.getSummary())
                .setLocation(dto.getLocation())
                .setDescription(dto.getDescription())
                .setStart(new EventDateTime()
                        .setDateTime(new DateTime(dto.getStartDateTime()))
                        .setTimeZone(dto.getStartTimeZone()))
                .setEnd(new EventDateTime()
                        .setDateTime(new DateTime(dto.getEndDateTime()))
                        .setTimeZone(dto.getEndTimeZone()))
                .setAttendees(dto.getEventAttendees())
                .setReminders(dto.getReminders());
        if (dto.getRecurrence() != null && !dto.getRecurrence().isEmpty()) {
             event.setRecurrence(dto.getRecurrence());
        }
        if (dto.isCreateConference() && event.getConferenceData() == null) {
            event.setConferenceData(new ConferenceData()
                    .setCreateRequest(new CreateConferenceRequest()
                            .setRequestId(UUID.randomUUID().toString())
                            .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"))));
        }
        return event;
    }

    @Override
    public void deleteCalendarEvent(String eventId) {
        Calendar.Events service = getCalendarClient().events();
        try {
            service
                    .delete(DEFAULT_CALENDAR, eventId)
                    .setSendUpdates("all")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void deleteCalendarRecurrentEvent(String eventId, String recurrentEventId) {
        Calendar.Events service = getCalendarClient().events();
        try {
            Events instances = service.instances(DEFAULT_CALENDAR, recurrentEventId).execute();
            if (instances != null && instances.getItems() != null) {
                List<Event> events = instances.getItems();
                if (eventId != null) {
                    events = events.stream()
                            .filter(event -> event.getId().equals(eventId))
                            .toList();
                }
                for (Event event : events) {
                    service
                            .delete(DEFAULT_CALENDAR, event.getId())
                            .execute();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public GoogleCalendarEventDto updateCalendarEvent(String eventId, CreateGoogleCalendarEventDto dto) {
        Calendar.Events service = getCalendarClient().events();
        try {
            Event event = service.get(DEFAULT_CALENDAR, eventId).execute();
            event = createNewEventObject(event, dto);
            service
                    .update(DEFAULT_CALENDAR, event.getId(), event)
                    .setSendUpdates("all")
                    .execute();
            return GoogleCalendarEventDto.builder()
                    .calendarEventId(event.getId())
                    .calendarEventUID(event.getICalUID())
                    .conferenceLink(event.getHangoutLink())
                    .eventLink(event.getHtmlLink())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    private Calendar getCalendarClient() {
        Calendar client = null;
        try {
            ServiceCredentialsDto credentialsDto = credentialsService
                    .findCredentials(getUsername(), ServiceType.GOOGLE_CALENDAR);
            GoogleAuthorizationCodeFlow flow = getGoogleAuthorizationCodeFlow();
            TokenResponse tokenResponse = modelMapper.map(credentialsDto, TokenResponse.class);
            Credential credential = flow.createAndStoreCredential(tokenResponse, getUsername());
            client = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                    .setApplicationName(ServiceType.GOOGLE_CALENDAR.toString()).build();
        } catch (GeneralSecurityException | IOException e) {
            throw new ServiceErrorException(e);
        }
        return client;
    }

    private GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() throws GeneralSecurityException, IOException {
        return getGoogleAuthorizationCodeFlow(ClientApprovalPrompt.FORCE);
    }

    private GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow(ClientApprovalPrompt prompt) throws GeneralSecurityException, IOException {
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details()
                .setClientId(clientId)
                .setClientSecret(clientSecret);
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(web);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR))
                .setAccessType("offline")
                .setApprovalPrompt(prompt.getCode())
                .build();
    }
}
