package com.scrumly.eventservice.domain.blocks.itemsBoard;

import com.scrumly.eventservice.domain.blocks.reflect.ReflectColumnEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class ItemsBoardColumnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer columnOrder;
    @Column(nullable = false)
    private String title;
    @Column
    private String color;
    @Column
    private String instruction;
    @Column
    private Integer maxItems;
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ItemsBoardColumnStatusEntity> statusMapping;

    public ItemsBoardColumnEntity(ItemsBoardColumnEntity columnEntity) {
        this.columnOrder = columnEntity.columnOrder;
        this.title = columnEntity.title;
        this.color = columnEntity.color;
        this.instruction = columnEntity.instruction;
        this.maxItems = columnEntity.maxItems;
    }
}
