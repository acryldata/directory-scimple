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

package org.apache.directory.scim.spec.resources;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.directory.scim.spec.annotation.ScimAttribute;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.exception.InvalidExtensionException;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.spec.schema.Schema.Attribute.Returned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class defines the attributes shared by all SCIM resources. It also
 * provides BVF annotations to allow validation of the POJO.
 * 
 * @author smoyer1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ScimResource extends BaseResource<ScimResource> implements Serializable {

  private static final long serialVersionUID = 3673404125396687366L;

  private static final Logger LOG = LoggerFactory.getLogger(ScimResource.class);

  @XmlElement
  @NotNull
  @ScimAttribute(returned = Returned.ALWAYS)
  Meta meta;

  @XmlElement
  @Size(min = 1)
  @ScimAttribute(required = true, returned = Returned.ALWAYS, mutability = Schema.Attribute.Mutability.READ_ONLY, uniqueness = Schema.Attribute.Uniqueness.SERVER, description = "A unique identifier for a SCIM resource as defined by the service provider.")
  String id;

  @XmlElement
  @ScimAttribute(caseExact = true, mutability = Schema.Attribute.Mutability.READ_WRITE)
  String externalId;

  // TODO - Figure out JAXB equivalent of JsonAnyGetter and JsonAnySetter
  // (XmlElementAny?)
  private Map<String, ScimExtension> extensions = new LinkedHashMap<>();

  private final String baseUrn;

  private final String resourceType;

  public ScimResource(String urn, String resourceType) {
    super(urn);
    this.baseUrn = urn;
    this.resourceType = resourceType;

    ScimResourceType resourceTypeAnnotation = getClass().getAnnotation(ScimResourceType.class);
    if (resourceTypeAnnotation != null) {
      this.meta = new Meta().setResourceType(resourceTypeAnnotation.id());
    }
  }

  /**
   * Add an extension to the ScimResource
   * @param extension the scim extension
   * @throws InvalidExtensionException if the ScimExtension passed in is improperly configured.  
   */
  public ScimResource addExtension(ScimExtension extension) {
    ScimExtensionType[] se = extension.getClass().getAnnotationsByType(ScimExtensionType.class);

    if (se.length != 1) {
      throw new InvalidExtensionException("Registered extensions must have an ScimExtensionType annotation");
    }

    String extensionUrn = se[0].id();
    extensions.put(extensionUrn, extension);
    
    addSchema(extensionUrn);
    return this;
  }

  public ScimExtension getExtension(String urn) {
    return extensions.get(urn);
  }
  
  /**
   * Returns the scim extension of a particular class
   * @param extensionClass 
   * @return
   * @throws InvalidExtensionException if the ScimExtension passed in is improperly configured.  
   */
  @SuppressWarnings("unchecked")
  public <T> T getExtension(Class<T> extensionClass) {
    ScimExtensionType se = lookupScimExtensionType(extensionClass);
    
    return (T) extensions.get(se.id());
  }

  private <T> ScimExtensionType lookupScimExtensionType(Class<T> extensionClass) {
    ScimExtensionType[] se = extensionClass.getAnnotationsByType(ScimExtensionType.class);

    if (se.length != 1) {
      throw new InvalidExtensionException("Registered extensions must have an ScimExtensionType annotation");
    }

    return se[0];
  }

  public String getBaseUrn() {
    return baseUrn;
  }

  @JsonAnyGetter
  public Map<String, ScimExtension> getExtensions() {
    return extensions;
  }

  public ScimExtension removeExtension(String urn) {
    return extensions.remove(urn);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T removeExtension(Class<T> extensionClass) {
    ScimExtensionType se = lookupScimExtensionType(extensionClass);
    
    return (T) extensions.remove(se.id());
  }
}
