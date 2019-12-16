/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.deployer.spi.local;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.deployer.spi.local.LocalDeployerProperties.PREFIX;

@Ignore
public class JavaExecutionCommandBuilderTests {

    private JavaCommandBuilder commandBuilder;
    private List<String> args;
    private Map<String, String> deploymentProperties;
    private LocalDeployerProperties localDeployerProperties;

    @Before
    public void setUp() {
        args = new ArrayList<>();
        deploymentProperties = new HashMap<>();
        localDeployerProperties = new LocalDeployerProperties();
        commandBuilder = new JavaCommandBuilder(localDeployerProperties);
    }

    @Test
    public void testDirectJavaMemoryOption() {
        deploymentProperties.put(AppDeployer.MEMORY_PROPERTY_KEY, "1024m");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(1));
        assertThat(args.get(0), is("-Xmx1024m"));
    }

    @Test
    public void testDirectJavaMemoryOptionWithG() {
        deploymentProperties.put(AppDeployer.MEMORY_PROPERTY_KEY, "1g");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(1));
        assertThat(args.get(0), is("-Xmx1024m"));
    }

    @Test
    public void testJavaMemoryOption() {
        deploymentProperties.put(PREFIX + ".javaOpts", "-Xmx1024m");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(1));
        assertThat(args.get(0), is("-Xmx1024m"));
    }

    @Test
    public void testJavaMemoryOptionWithKebabCase() {
        deploymentProperties.put(PREFIX + ".java-opts", "-Xmx1024m");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(1));
        assertThat(args.get(0), is("-Xmx1024m"));
    }

    @Test
    public void testJavaCmdOption() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put(PREFIX + ".javaCmd", "/test/java");
        Resource resource = mock(Resource.class);
        when(resource.getFile()).thenReturn(new File("/"));
        AppDeploymentRequest appDeploymentRequest = new AppDeploymentRequest(mock(AppDefinition.class), resource, properties);
        String[] commands = commandBuilder.buildExecutionCommand(appDeploymentRequest, Collections.EMPTY_MAP, Optional.of(1));
        assertThat(commands[0], is("/test/java"));
    }

    @Test
    public void testJavaCmdOptionWithKebabCase() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put(PREFIX + ".java-cmd", "/test/java");
        Resource resource = mock(Resource.class);
        when(resource.getFile()).thenReturn(new File("/"));
        AppDeploymentRequest appDeploymentRequest = new AppDeploymentRequest(mock(AppDefinition.class), resource, properties);
        String[] commands = commandBuilder.buildExecutionCommand(appDeploymentRequest, Collections.EMPTY_MAP, Optional.of(1));
        assertThat(commands[0], is("/test/java"));
    }


    @Test
    public void testOverrideMemoryOptions() {
        deploymentProperties.put(AppDeployer.MEMORY_PROPERTY_KEY, "1024m");
        deploymentProperties.put(PREFIX + ".javaOpts", "-Xmx2048m");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(1));
        assertThat(args.get(0), is("-Xmx2048m"));
    }

    @Test
    public void testDirectMemoryOptionsWithOtherOptions() {
        deploymentProperties.put(AppDeployer.MEMORY_PROPERTY_KEY, "1024m");
        deploymentProperties.put(PREFIX + ".javaOpts", "-Dtest=foo");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(2));
        assertThat(args.get(0), is("-Xmx1024m"));
        assertThat(args.get(1), is("-Dtest=foo"));
    }

    @Test
    public void testMultipleOptions() {
        deploymentProperties.put(PREFIX + ".javaOpts", "-Dtest=foo -Dbar=baz");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(2));
        assertThat(args.get(0), is("-Dtest=foo"));
        assertThat(args.get(1), is("-Dbar=baz"));
    }

    @Test
    public void testConfigurationPropertiesOverride() {
        localDeployerProperties.setJavaOpts("-Dfoo=test -Dbaz=bar");
        commandBuilder.addJavaOptions(args, deploymentProperties, localDeployerProperties);
        assertThat(args.size(), is(2));
        assertThat(args.get(0), is("-Dfoo=test"));
        assertThat(args.get(1), is("-Dbaz=bar"));
    }


    @Test
    public void testJarExecution() {
        AppDefinition definition = new AppDefinition("randomApp", new HashMap<>());
        deploymentProperties.put(PREFIX + ".javaOpts", "-Dtest=foo -Dbar=baz");
        AppDeploymentRequest appDeploymentRequest =
                new AppDeploymentRequest(definition, testResource(), deploymentProperties);
        commandBuilder.addJavaExecutionOptions(args, appDeploymentRequest);
        assertThat(args.size(), is(2));
        assertThat(args.get(0), is("-jar"));
        assertThat(args.get(1), containsString("testResource.txt"));
    }

    @Test(expected = IllegalStateException.class)
    public void testBadResourceExecution() throws MalformedURLException {
        AppDefinition definition = new AppDefinition("randomApp", new HashMap<>());
        deploymentProperties.put(PREFIX + ".javaOpts", "-Dtest=foo -Dbar=baz");
        AppDeploymentRequest appDeploymentRequest =
                new AppDeploymentRequest(definition, new UrlResource("https://spring.io"), deploymentProperties);
        commandBuilder.addJavaExecutionOptions(args, appDeploymentRequest);
    }

    @Test
    public void testCommandBuilderSpringApplicationJson() {
        LocalDeployerProperties properties = new LocalDeployerProperties();
        LocalAppDeployer deployer = new LocalAppDeployer(properties);
        AppDefinition definition = new AppDefinition("foo", Collections.singletonMap("foo","bar"));

        deploymentProperties.put(LocalDeployerProperties.DEBUG_PORT, "9999");
        deploymentProperties.put(LocalDeployerProperties.DEBUG_SUSPEND, "y");
        deploymentProperties.put(LocalDeployerProperties.INHERIT_LOGGING, "true");
        AppDeploymentRequest request = new AppDeploymentRequest(definition, testResource(), deploymentProperties);


        ProcessBuilder builder = deployer.buildProcessBuilder(request, definition.getProperties(), Optional.of(1), "foo" );
        assertThat(builder.environment().keySet(), hasItem(AbstractLocalDeployerSupport.SPRING_APPLICATION_JSON));
        assertThat(builder.environment().get(AbstractLocalDeployerSupport.SPRING_APPLICATION_JSON), is("{\"foo\":\"bar\"}"));
    }

    @Test
    public void testCommandBuilderWithSpringApplicationJson() {
        LocalDeployerProperties properties = new LocalDeployerProperties();
        LocalAppDeployer deployer = new LocalAppDeployer(properties);
        Map<String,String> applicationProperties = new HashMap<>();
        applicationProperties.put("foo","bar");
        String SAJ = "{\"debug\":\"true\"}";
        applicationProperties.put(AbstractLocalDeployerSupport.SPRING_APPLICATION_JSON,SAJ);
        AppDefinition definition = new AppDefinition("foo", applicationProperties);

        deploymentProperties.put(LocalDeployerProperties.DEBUG_PORT, "9999");
        deploymentProperties.put(LocalDeployerProperties.DEBUG_SUSPEND, "y");
        deploymentProperties.put(LocalDeployerProperties.INHERIT_LOGGING, "true");
        AppDeploymentRequest request = new AppDeploymentRequest(definition, testResource(), deploymentProperties);


        ProcessBuilder builder = deployer.buildProcessBuilder(request, definition.getProperties(), Optional.of(1), "foo" );
        assertThat(builder.environment().keySet(), hasItem(AbstractLocalDeployerSupport.SPRING_APPLICATION_JSON));
        assertThat(builder.environment().get(AbstractLocalDeployerSupport.SPRING_APPLICATION_JSON), is("{\"foo\":\"bar\",\"debug\":\"true\"}"));

    }

    protected Resource testResource() {
        return new ClassPathResource("testResource.txt");
    }

}
