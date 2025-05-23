package com.scrumly.userservice.userservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String imageId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;
    @Lob
    private byte[] data;

    public ImageEntity(ImageEntity imageEntity) {
        this.data = imageEntity.data;
        this.name = imageEntity.name;
        this.type = imageEntity.type;
    }
}
