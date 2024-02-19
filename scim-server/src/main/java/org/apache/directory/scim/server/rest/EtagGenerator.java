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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.directory.scim.core.json.ObjectMapperFactory;
import org.apache.directory.scim.server.exception.EtagGenerationException;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Meta;

import jakarta.ws.rs.core.EntityTag;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@ApplicationScoped
public class EtagGenerator {

  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

  public String generateEtag(ScimResource resource) throws EtagGenerationException {

    try {
      Meta meta = resource.getMeta();

      if (meta == null) {
        meta = new Meta();
      }

      resource.setMeta(null);
      String writeValueAsString = objectMapper.writeValueAsString(resource);

      String etag = hash(writeValueAsString);

      meta.setVersion(etag);

      resource.setMeta(meta);

      return etag;
    } catch (JsonProcessingException | NoSuchAlgorithmException e) {
      throw new EtagGenerationException("Failed to generate etag for SCIM resource: " + resource.getId(), e);
    }
  }
  
  private static String hash(String input) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(input.getBytes(StandardCharsets.UTF_8));
    byte[] hash = digest.digest();
    return Base64.getEncoder().encodeToString(hash);
  }
}
