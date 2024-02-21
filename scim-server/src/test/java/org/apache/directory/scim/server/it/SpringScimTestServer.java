package org.apache.directory.scim.server.it;

import org.apache.directory.scim.compliance.junit.EmbeddedServerExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;

public class SpringScimTestServer implements EmbeddedServerExtension.ScimTestServer {

  private ConfigurableApplicationContext context;

  @Override
  public URI start(int port) {
    context = SpringApplication.run(ScimpleSpringTestApp.class,
      "--server.servlet.context-path=/",
      "--server.port=" + port
    );
    return URI.create("http://localhost:" + port + "/scim/v2");
  }

  @Override
  public void shutdown() {
    context.stop();
  }
}
