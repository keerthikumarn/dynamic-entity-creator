package com.dynamicentity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dynamicentity.dto.EntityCreationRequest;
import com.dynamicentity.service.DynamicEntityService;

@RestController
@RequestMapping("/api/entities")
public class DynamicEntityController {
	
	@Autowired
	private DynamicEntityService dynamicEntityService;
	
	@PostMapping("/create")
    public ResponseEntity<String> createEntity(@RequestBody EntityCreationRequest request) throws Exception {
        dynamicEntityService.createEntity(request);
        return ResponseEntity.ok("Entity created successfully");
    }

}
