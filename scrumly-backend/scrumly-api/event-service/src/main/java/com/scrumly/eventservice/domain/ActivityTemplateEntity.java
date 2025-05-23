package com.scrumly.eventservice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ActivityTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String templateId;
    @Column(nullable = false)
    private String name;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column
    private String previewImageId;
    @ElementCollection
    private List<String> tags;
    @ManyToOne
    private ActivityTypeEntity type;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private ActivityOwnerEntity owner;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ActivityBlockConfigEntity> blocks = new ArrayList<>();

    public ActivityTemplateEntity(ActivityTemplateEntity template) {
        this.templateId = template.templateId;
        this.name = template.name;
        this.description = template.description;
        this.previewImageId = template.previewImageId;
        this.tags = new ArrayList<>(template.tags);
        this.type = new ActivityTypeEntity(template.type);
        this.owner = new ActivityOwnerEntity(template.owner);
        this.blocks = template.blocks.stream()
                .map(ActivityBlockConfigEntity::new)
                .collect(Collectors.toList());
    }
}
