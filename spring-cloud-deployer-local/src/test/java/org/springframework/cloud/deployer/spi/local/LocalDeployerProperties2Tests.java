package org.springframework.cloud.deployer.spi.local;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

public class LocalDeployerProperties2Tests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

	@Test
	@EnabledOnOs(OS.WINDOWS)
	public void testOnWindows() {
		this.contextRunner
			.withInitializer(context -> {
				Map<String, Object> map = new HashMap<>();
				map.put("spring.cloud.deployer.local.working-directories-root", "C:/");

				context.getEnvironment().getPropertySources().addLast(new SystemEnvironmentPropertySource(
					StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, map));
			})
			.withUserConfiguration(Config1.class)
			.run((context) -> {
				LocalDeployerProperties properties = context.getBean(LocalDeployerProperties.class);
				assertThat(properties.getWorkingDirectoriesRoot()).isNotNull();
				assertThat(properties.getWorkingDirectoriesRoot()).isEqualTo("C:/");
			});
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	public void testOnLinux() {
		this.contextRunner
			.withInitializer(context -> {
				Map<String, Object> map = new HashMap<>();
				map.put("spring.cloud.deployer.local.working-directories-root", "/tmp");

				context.getEnvironment().getPropertySources().addLast(new SystemEnvironmentPropertySource(
					StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, map));
			})
			.withUserConfiguration(Config1.class)
			.run((context) -> {
				LocalDeployerProperties properties = context.getBean(LocalDeployerProperties.class);
				assertThat(properties.getWorkingDirectoriesRoot()).isNotNull();
				assertThat(properties.getWorkingDirectoriesRoot().toString()).isEqualTo("/tmp");
			});
	}

	@EnableConfigurationProperties({ LocalDeployerProperties.class })
	private static class Config1 {
	}
}