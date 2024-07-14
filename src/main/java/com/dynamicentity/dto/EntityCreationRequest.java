package com.dynamicentity.dto;

import java.util.List;

import lombok.Data;

@Data
public class EntityCreationRequest {
	private String entityName;
	private List<ColumnDefinition> columns;

	@Data
	public static class ColumnDefinition {
		private String name;
		private String type;
	}
}
