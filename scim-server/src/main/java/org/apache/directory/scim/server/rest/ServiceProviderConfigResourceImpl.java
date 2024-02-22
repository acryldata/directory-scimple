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

import jakarta.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;

import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.protocol.ServiceProviderConfigResource;
import org.apache.directory.scim.protocol.data.ErrorResponse;
import org.apache.directory.scim.server.exception.EtagGenerationException;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.AuthenticationSchema;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.BulkConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.FilterConfiguration;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration.SupportedConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@ApplicationScoped
@RestController
@RequestMapping("/scim/v2/ServiceProviderConfig")
public class ServiceProviderConfigResourceImpl implements ServiceProviderConfigResource {

  private final ServerConfiguration serverConfiguration;

  private final EtagGenerator etagGenerator;

  @Inject
  public ServiceProviderConfigResourceImpl(ServerConfiguration serverConfiguration, EtagGenerator etagGenerator) {
    this.serverConfiguration = serverConfiguration;
    this.etagGenerator = etagGenerator;
  }

  public ServiceProviderConfigResourceImpl() {
    // CDI
    this(null, null);
  }

  @Override
  public ResponseEntity getServiceProviderConfiguration() {
    ServiceProviderConfiguration serviceProviderConfiguration = new ServiceProviderConfiguration();
    List<AuthenticationSchema> authenticationSchemas = serverConfiguration.getAuthenticationSchemas();
    BulkConfiguration bulk = serverConfiguration.getBulkConfiguration();
    SupportedConfiguration changePassword = serverConfiguration.getChangePasswordConfiguration();
    SupportedConfiguration etagConfig = serverConfiguration.getEtagConfiguration();
    FilterConfiguration filter = serverConfiguration.getFilterConfiguration();
    SupportedConfiguration patch = serverConfiguration.getPatchConfiguration();
    SupportedConfiguration sort = serverConfiguration.getSortConfiguration();
    String documentationUrl = serverConfiguration.getDocumentationUri();
    String externalId = serverConfiguration.getId();
    String id = serverConfiguration.getId();
    Meta meta = new Meta();
    String location = ServletUriComponentsBuilder.fromCurrentRequestUri().replaceQuery(null).replacePath(null).toUriString();
    String resourceType = "ServiceProviderConfig";
    LocalDateTime now = LocalDateTime.now();

    meta.setCreated(now);
    meta.setLastModified(now);
    meta.setLocation(location);
    meta.setResourceType(resourceType);
    serviceProviderConfiguration.setAuthenticationSchemes(authenticationSchemas);
    serviceProviderConfiguration.setBulk(bulk);
    serviceProviderConfiguration.setChangePassword(changePassword);
    serviceProviderConfiguration.setDocumentationUrl(documentationUrl);
    serviceProviderConfiguration.setEtag(etagConfig);
    serviceProviderConfiguration.setExternalId(externalId);
    serviceProviderConfiguration.setFilter(filter);
    serviceProviderConfiguration.setId(id);
    serviceProviderConfiguration.setMeta(meta);
    serviceProviderConfiguration.setPatch(patch);
    serviceProviderConfiguration.setSort(sort);
    
    try {
      String etag = etagGenerator.generateEtag(serviceProviderConfiguration);
      return ResponseEntity.ok().header(HttpHeaders.ETAG, etag).body(serviceProviderConfiguration);
    } catch (EtagGenerationException e) {
      return createETagErrorResponse();
    }
  }
  
  private ResponseEntity<ErrorResponse> createETagErrorResponse() {
    ErrorResponse er = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate the etag");
    return er.toResponseEntity();
  }
}
