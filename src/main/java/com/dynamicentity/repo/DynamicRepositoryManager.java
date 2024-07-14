package com.dynamicentity.repo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class DynamicRepositoryManager {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, DynamicJpaRepository<?, ?>> repositories = new HashMap<>();

    public DynamicJpaRepository<?, ?> getRepository(String entityName, Class<?> entityClass) {
        return repositories.computeIfAbsent(entityName, k -> createRepository(entityClass));
    }

    @SuppressWarnings("unchecked")
    private DynamicJpaRepository<?, ?> createRepository(Class<?> entityClass) {
        RepositoryFactorySupport factory = new DynamicJpaRepositoryFactory(entityManager);
        return factory.getRepository((Class<DynamicJpaRepository<?, ?>>) applicationContext.getBean(DynamicJpaRepository.class).getClass());
    }
}
