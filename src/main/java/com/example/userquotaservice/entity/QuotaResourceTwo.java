package com.example.userquotaservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "quota_resource_two")
public class QuotaResourceTwo extends QuotaResource {
    
    public QuotaResourceTwo(Long id, Integer blockingThreshold) {
        super(id, blockingThreshold, "QuotaResourceTwo");
    }
} 