package com.scrumly.userservice.userservice.dto.service;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {
    private Long id;
    private String imageId;
    private String name;
    private String type;
    private byte[] data;
}
