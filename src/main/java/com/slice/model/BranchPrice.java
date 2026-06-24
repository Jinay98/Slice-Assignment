package com.slice.model;

import com.slice.model.enums.CarType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(
        name = "branch_prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_branch_prices_branch_type",
                columnNames = {"branch_id", "car_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BranchPrice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false, length = 50)
    private CarType carType;

    @Column(name = "price_per_hour", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerHour;
}
