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
