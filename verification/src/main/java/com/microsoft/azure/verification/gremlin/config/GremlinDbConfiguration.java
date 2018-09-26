package com.microsoft.azure.verification.gremlin.config;

import com.microsoft.spring.data.gremlin.common.GremlinConfiguration;
import com.microsoft.spring.data.gremlin.config.AbstractGremlinConfiguration;
import com.microsoft.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableGremlinRepositories(basePackages = "com.microsoft.azure.verification.gremlin")
public class GremlinDbConfiguration extends AbstractGremlinConfiguration {
    @Value("${gremlin.endpoint}")
    private String endpoint;

    @Value("${gremlin.port}")
    private String port;

    @Value("${gremlin.username}")
    private String username;

    @Value("${gremlin.password}")
    private String password;

    @Override
    public GremlinConfiguration getGremlinConfiguration() {
        GremlinConfiguration config = new GremlinConfiguration();
        config.setEndpoint(endpoint);
        config.setPort(port);
        config.setUsername(username);
        config.setPassword(password);

        return config;
    }
}