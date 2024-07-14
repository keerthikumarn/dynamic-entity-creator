package com.dynamicentity.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dynamicentity.dto.EntityCreationRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;

@Service
public class DynamicEntityService {

	@Autowired
	private EntityManager entityManager;

	private final Map<String, Class<?>> dynamicEntities = new HashMap<>();

	@Transactional
	public void createEntity(EntityCreationRequest request) throws Exception {
		String entityName = request.getEntityName();

		// DynamicType.Builder using ByteBuddy
		DynamicType.Builder<?> builder = new ByteBuddy().subclass(Object.class)
				.name("com.dynamicentity.entities." + entityName)
				.annotateType(AnnotationDescription.Builder.ofType(Entity.class).build())
				.annotateType(AnnotationDescription.Builder.ofType(Table.class).define("name", entityName.toLowerCase())
						.build());

		// Add ID field (assuming Long as ID type)
		builder = builder.defineField("id", Long.class, Visibility.PRIVATE)
				.annotateField(AnnotationDescription.Builder.ofType(Id.class).build())
				.annotateField(AnnotationDescription.Builder.ofType(GeneratedValue.class)
						.define("strategy", GenerationType.IDENTITY).build());

		// Add getter and setter for ID
		builder = builder.defineMethod("getId", Long.class, Visibility.PUBLIC).intercept(FieldAccessor.ofField("id"))
				.defineMethod("setId", void.class, Visibility.PUBLIC).withParameter(Long.class)
				.intercept(FieldAccessor.ofField("id"));

		// Create a list to hold column definitions for table creation
		List<String> columnDefinitions = new ArrayList<>();
		columnDefinitions.add("id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY");

		for (EntityCreationRequest.ColumnDefinition column : request.getColumns()) {
			Class<?> fieldType = getFieldType(column.getType());
			builder = builder.defineField(column.getName(), fieldType, Visibility.PRIVATE)
					.annotateField(AnnotationDescription.Builder.ofType(Column.class)
							.define("name", column.getName().toLowerCase()).build());

			// Add getter and setter for each field
			String capitalizedName = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);
			builder = builder.defineMethod("get" + capitalizedName, fieldType, Visibility.PUBLIC)
					.intercept(FieldAccessor.ofField(column.getName()))
					.defineMethod("set" + capitalizedName, void.class, Visibility.PUBLIC).withParameter(fieldType)
					.intercept(FieldAccessor.ofField(column.getName()));

			// Add column definition for table creation
			columnDefinitions.add(column.getName().toLowerCase() + " " + getSqlType(column.getType()));
		}

		// Create the dynamic type
		Class<?> dynamicType = builder.make().load(getClass().getClassLoader()).getLoaded();

		// **Alternatively, create the table dynamically (not recommended):**

		String createTableQuery = "CREATE TABLE IF NOT EXISTS " + entityName.toLowerCase() + " ("
				+ String.join(", ", columnDefinitions) + ")";
		entityManager.createNativeQuery(createTableQuery).executeUpdate();

		// **Registering the entity with Hibernate is not necessary for persisting**
		// entityManager.getEntityManagerFactory().unwrap(SessionFactory.class)
		// .getMetamodel().getEntities().add(dynamicType);

		System.out.println(dynamicType.getCanonicalName());
		// **Persist the entity:**
		entityManager.persist(dynamicType.newInstance());

		// entityManager.persist(entityManager.createProxy(dynamicType));
	}

	private String getSqlType(String type) {
		switch (type.toLowerCase()) {
		case "string":
			return "VARCHAR(255)";
		case "integer":
			return "INTEGER";
		case "long":
			return "BIGINT";
		case "double":
			return "DOUBLE PRECISION";
		case "boolean":
			return "BOOLEAN";
		case "date":
			return "DATE";
		default:
			throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}

	private Class<?> getFieldType(String type) {
		switch (type.toLowerCase()) {
		case "string":
			return String.class;
		case "integer":
			return Integer.class;
		case "long":
			return Long.class;
		case "double":
			return Double.class;
		case "boolean":
			return Boolean.class;
		case "date":
			return java.util.Date.class;
		default:
			throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}

	@Transactional
	public Object save(String entityName, Map<String, Object> entityData) throws Exception {
		Class<?> entityClass = dynamicEntities.get(entityName);
		if (entityClass == null) {
			throw new IllegalArgumentException("Entity not found: " + entityName);
		}
		Object entity = entityClass.getDeclaredConstructor().newInstance();
		for (Map.Entry<String, Object> entry : entityData.entrySet()) {
			Field field = entityClass.getDeclaredField(entry.getKey());
			field.setAccessible(true);
			field.set(entity, entry.getValue());
		}
		return entityManager.merge(entity);
	}

	@Transactional
	public Object findById(String entityName, Long id) {
		Class<?> entityClass = dynamicEntities.get(entityName);
		if (entityClass == null) {
			throw new IllegalArgumentException("Entity not found: " + entityName);
		}
		return entityManager.find(entityClass, id);
	}

	@Transactional
	public List<Object> findAll(String entityName) {
		Class<?> entityClass = dynamicEntities.get(entityName);
		if (entityClass == null) {
			throw new IllegalArgumentException("Entity not found: " + entityName);
		}

		String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
		return (List<Object>) entityManager.createQuery(jpql, entityClass).getResultList();
	}
}