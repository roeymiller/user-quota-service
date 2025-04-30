package com.example.userquotaservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "quota_resource_one")
public class QuotaResourceOne extends QuotaResource {
    
    public QuotaResourceOne(Long id, Integer blockingThreshold) {
        super(id, blockingThreshold, "QuotaResourceOne");
    }
} 