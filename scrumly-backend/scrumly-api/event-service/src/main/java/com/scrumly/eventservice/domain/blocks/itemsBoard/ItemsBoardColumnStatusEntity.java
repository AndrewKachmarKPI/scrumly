package com.scrumly.eventservice.domain.blocks.itemsBoard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Entity
public class ItemsBoardColumnStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String statusId;
    @Column
    private String backlogId;
    @Column
    private String status;
    @Column
    private String color;

    public ItemsBoardColumnStatusEntity(ItemsBoardColumnStatusEntity that) {
        this.id = that.getId();
        this.backlogId = that.getBacklogId();
        this.statusId = that.getStatusId();
        this.status = that.getStatus();
        this.color = that.getColor();
    }
}
