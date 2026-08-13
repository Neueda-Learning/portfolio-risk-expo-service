package com.risk_busters.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "asset_class")
@Data
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

