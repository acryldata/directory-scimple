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

package org.apache.directory.scim.server.it;

import java.util.List;
import org.apache.directory.scim.core.repository.DefaultPatchHandler;
import org.apache.directory.scim.core.repository.PatchHandler;
import org.apache.directory.scim.core.repository.Repository;
import org.apache.directory.scim.core.repository.RepositoryRegistry;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.server.configuration.ServerConfiguration;
import org.apache.directory.scim.server.rest.EtagGenerator;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "org.apache.directory.scim")
public class ScimpleSpringTestApp {

  @Bean
  ServerConfiguration serverConfiguration() {
    return new ServerConfiguration().addAuthenticationSchema(ServiceProviderConfiguration.AuthenticationSchema.oauthBearer());
  }

  @Bean
  @ConditionalOnMissingBean
  SchemaRegistry schemaRegistry() {
    return new SchemaRegistry();
  }

  @Bean
  @ConditionalOnMissingBean
  RepositoryRegistry repositoryRegistry(
    SchemaRegistry schemaRegistry, List<Repository<? extends ScimResource>> scimResources) {

    RepositoryRegistry registry = new RepositoryRegistry(schemaRegistry);

    registry.registerRepositories(scimResources);

    return registry;
  }

  @Bean
  @ConditionalOnMissingBean
  EtagGenerator etagGenerator() {
    return new EtagGenerator();
  }

  @Bean
  @ConditionalOnMissingBean
  PatchHandler patchHandler(SchemaRegistry schemaRegistry) {
    return new DefaultPatchHandler(schemaRegistry);
  }

}
