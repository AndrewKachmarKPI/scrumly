package com.scrumly.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FileDocument {
    private String id;
    private String name;
    private LocalDateTime createdDateTime;
    private long fileSize;
    private String fileType;
    private byte[] file;
}
