package com.scrumly.conferenceservice.utils;

import com.scrumly.conferenceservice.dto.ConnectionServerDataDto;
import com.scrumly.conferenceservice.enums.ConnectionType;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CodeGenerator {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final int SEGMENT_LENGTH = 3;
    private static final int SEGMENTS = 3;
    private static final Random RANDOM = new SecureRandom();

    public static String generateConferenceId() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < SEGMENTS; i++) {
            if (i > 0) {
                code.append("-");
            }
            code.append(generateSegment());
        }
        return code.toString();
    }

    private static String generateSegment() {
        StringBuilder segment = new StringBuilder();

        for (int i = 0; i < SEGMENT_LENGTH; i++) {
            segment.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return segment.toString();
    }

    public static ConnectionServerDataDto getConnectionData(String input) {
        input = input.substring(1, input.length() - 1);
        Map<String, String> dataMap = new HashMap<>();
        for (String pair : input.split(", ")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                dataMap.put(keyValue[0], keyValue[1]);
            }
        }
        return new ConnectionServerDataDto(ConnectionType.valueOf(dataMap.get("connectionType")), dataMap.get("userId"));
    }

    public static String formatConferenceId(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        return code.replaceAll("-", "");
    }

}
