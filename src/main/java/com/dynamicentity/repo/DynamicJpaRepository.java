package com.dynamicentity.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DynamicJpaRepository<T, ID> extends JpaRepository<T, ID> {
	
}
