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

import java.security.Principal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response.Status;

import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.server.exception.UnableToResolveIdResourceException;
import org.apache.directory.scim.core.repository.SelfIdResolver;
import org.apache.directory.scim.protocol.SelfResource;
import org.apache.directory.scim.protocol.UserResource;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;


@Slf4j
@ApplicationScoped
public class SelfResourceImpl implements SelfResource {

  private final UserResource userResource;

  private final Instance<SelfIdResolver> selfIdResolver;

  // TODO: Field injection of SecurityContext should work with all implementations
  // CDI can be used directly in Jakarta WS 4
  @Context
  SecurityContext securityContext;

  @Inject
  public SelfResourceImpl(UserResource userResource, Instance<SelfIdResolver> selfIdResolver) {
    this.userResource = userResource;
    this.selfIdResolver = selfIdResolver;
  }

  @Override
  public ResponseEntity<? extends ScimResource> getSelf(WebRequest request, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    String internalId = getInternalId();
    return userResource.getById(request, internalId, attributes, excludedAttributes);
  }

  // @Override
  // public Response create(ScimUser resource, AttributeReferenceListWrapper
  // attributes, AttributeReferenceListWrapper excludedAttributes) {
  // String internalId = getInternalId();
  // //TODO check if ids match in request
  // return userResourceImpl.create(resource, attributes, excludedAttributes);
  // }

  @Override
  public ResponseEntity<? extends ScimResource> update(WebRequest request, ScimUser resource, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    String internalId = getInternalId();
    return userResource.update(request, resource, internalId, attributes, excludedAttributes);
  }

  @Override
  public ResponseEntity<? extends ScimResource> patch(WebRequest request, PatchRequest patchRequest, AttributeReferenceListWrapper attributes, AttributeReferenceListWrapper excludedAttributes) throws ScimException, ResourceException {
    String internalId = getInternalId();
    return userResource.patch(request, patchRequest, internalId, attributes, excludedAttributes);
  }

  @Override
  public ResponseEntity<? extends ScimResource> delete() throws ScimException, ResourceException {
    String internalId = getInternalId();
    return userResource.delete(internalId);
  }

  private String getInternalId() throws ResourceException {
    Principal callerPrincipal = securityContext.getUserPrincipal();

    if (callerPrincipal != null) {
      log.debug("Resolved SelfResource principal to : {}", callerPrincipal.getName());
    } else {
      throw new UnableToResolveIdResourceException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    if (selfIdResolver.isUnsatisfied()) {
      throw new UnableToResolveIdResourceException(HttpStatus.NOT_IMPLEMENTED, "Caller SelfIdResolver not available");
    }

    return selfIdResolver.get().resolveToInternalId(callerPrincipal);
  }
}
