package com.dynamicentity.repo;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import jakarta.persistence.EntityManager;

public class DynamicJpaRepositoryFactory extends JpaRepositoryFactory {

	public DynamicJpaRepositoryFactory(EntityManager entityManager) {
		super(entityManager);
	}

	@Override
	protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information,
			EntityManager entityManager) {
		return new DynamicJpaRepositoryImpl<>(information.getDomainType(), entityManager);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return DynamicJpaRepositoryImpl.class;
	}

}
