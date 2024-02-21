/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.protocol.ResourceTypesResource;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;


@ApplicationScoped
@RestController
@RequestMapping("/scim/v2/ResourceTypes")
public class ResourceTypesResourceImpl implements ResourceTypesResource {

  private final SchemaRegistry schemaRegistry;

  // TODO: Field injection of UriInfo should work with all implementations
  // CDI can be used directly in Jakarta WS 4
  @Context
  UriInfo uriInfo;

  @Inject
  public ResourceTypesResourceImpl(SchemaRegistry schemaRegistry) {
    this.schemaRegistry = schemaRegistry;
  }

  public ResourceTypesResourceImpl() {
    // CDI
    this(null);
  }

  @Override
  public ResponseEntity<ListResponse<ResourceType>> getAllResourceTypes(@RequestParam(name = "filter", required = false) String filter) {
    
    if (filter != null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Collection<ResourceType> resourceTypes = schemaRegistry.getAllResourceTypes();
    UriComponentsBuilder absolutePath = ServletUriComponentsBuilder.fromCurrentRequestUri().replaceQuery(null).replacePath(null);
    for (ResourceType resourceType : resourceTypes) {
      Meta meta = new Meta();
      String location = absolutePath.path(resourceType.getName()).build().toString();
      meta.setLocation(location);
      meta.setResourceType(resourceType.getResourceType());
      
      resourceType.setMeta(meta);
    }
    
    ListResponse<ResourceType> listResponse = new ListResponse<>();
    listResponse.setItemsPerPage(resourceTypes.size());
    listResponse.setStartIndex(1);
    listResponse.setTotalResults(resourceTypes.size());
    
    List<ResourceType> objectList = new ArrayList<>(resourceTypes);
    listResponse.setResources(objectList);
    
    return ResponseEntity.ok(listResponse);
  }

  @Override
  public ResponseEntity<ResourceType> getResourceType(@PathVariable(name = "name") String name) {
    ResourceType resourceType = schemaRegistry.getResourceType(name);
    if (resourceType == null){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
    Meta meta = new Meta();

    String location = ServletUriComponentsBuilder.fromCurrentRequestUri().replaceQuery(null).replacePath(null)
      .build().toString();

    meta.setLocation(location);

    meta.setResourceType(resourceType.getResourceType());
    
    resourceType.setMeta(meta);
    
    return ResponseEntity.ok(resourceType);
  }

}
