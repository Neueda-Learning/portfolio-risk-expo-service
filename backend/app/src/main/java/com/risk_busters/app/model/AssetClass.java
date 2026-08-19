package com.risk_busters.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "asset_class")
@Getter
@Setter
@ToString(exclude = {"instruments"})
@EqualsAndHashCode(exclude = {"instruments"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetClass {

    @Id
    @Column(name = "asset_class_id")
    private Integer assetClassId;

    @Column(name = "asset_class_code", nullable = false, unique = true, length = 20)
    private String assetClassCode;

    @Column(name = "asset_class_name", nullable = false, length = 100)
    private String assetClassName;

    @Column(length = 300)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "assetClass", fetch = FetchType.LAZY)
    private List<Instrument> instruments;
}

