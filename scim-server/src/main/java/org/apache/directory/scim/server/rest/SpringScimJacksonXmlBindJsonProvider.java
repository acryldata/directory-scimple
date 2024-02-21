package org.apache.directory.scim.server.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.List;
import org.apache.directory.scim.core.json.ObjectMapperFactory;
import org.apache.directory.scim.core.schema.SchemaRegistry;
import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.data.ListResponse;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.ServiceProviderConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * It is equivalent of @link{ScimJacksonXmlBindJsonProvider} for Spring WebMVC
 */
@Configuration
@EnableWebMvc
public class SpringScimJacksonXmlBindJsonProvider implements WebMvcConfigurer {

  @Autowired
  SchemaRegistry schemaRegistry;
  private static final Set<Package> SUPPORTED_PACKAGES = new HashSet<>(Arrays.asList(
    ScimResource.class.getPackage(),
    ListResponse.class.getPackage(),
    ServiceProviderConfiguration.class.getPackage()
  ));

  @Bean
  public MappingJackson2HttpMessageConverter jsonMessageConverter(SchemaRegistry schemaRegistry) {
    ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper(schemaRegistry);
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.valueOf(Constants.SCIM_CONTENT_TYPE)));
    return converter;
  }

  @Bean
  public MappingJackson2XmlHttpMessageConverter xmlMessageConverter(SchemaRegistry schemaRegistry) {
    ObjectMapper objectMapper = ObjectMapperFactory.createXmlObjectMapper(schemaRegistry);
    MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter(objectMapper);
    converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.valueOf(Constants.SCIM_CONTENT_TYPE)));
    return converter;
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer
      .favorPathExtension(false)
      .favorParameter(true)
      .parameterName("format")
      .ignoreAcceptHeader(true)
      .useRegisteredExtensionsOnly(true)
      .mediaType("json", MediaType.APPLICATION_JSON)
      .mediaType("xml", MediaType.APPLICATION_XML)
      .mediaType("scim+json", MediaType.APPLICATION_JSON)
      .defaultContentType(MediaType.valueOf(Constants.SCIM_CONTENT_TYPE));
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(jsonMessageConverter(schemaRegistry));
    converters.add(xmlMessageConverter(schemaRegistry));
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.clear();
    converters.add(jsonMessageConverter(schemaRegistry));
    converters.add(xmlMessageConverter(schemaRegistry));
  }
}
