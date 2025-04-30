package com.example.userquotaservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.userquotaservice.entity.QuotaResource;

@NoRepositoryBean
public interface QuotaResourceRepository<T extends QuotaResource> extends JpaRepository<T, Long> {
    Optional<T> findByResourceName(String resourceName);
} 