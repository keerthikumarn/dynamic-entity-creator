package com.dynamicentity.repo;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.EntityManager;

public class DynamicJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    private final EntityManager entityManager;

    public DynamicJpaRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

}