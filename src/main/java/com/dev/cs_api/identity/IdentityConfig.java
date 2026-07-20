package com.dev.cs_api.identity;

import com.dev.cs_api.identity.auth.dtos.RsaKeyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableConfigurationProperties(RsaKeyProperties.class)
@EnableJpaAuditing
public class IdentityConfig { }
