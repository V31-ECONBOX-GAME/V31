/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.v31bank.build.bom;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlatformExtension;
import org.gradle.api.plugins.JavaPlatformPlugin;

import org.v31bank.build.constant.Configurations;

/**
 * Plugin for defining a bom.
 * <p>
 * Applied on its own, without {@code java-platform} beside it:
 *
 * <pre class="code">
 * plugins {
 *     id("org.v31bank.bom")
 * }
 *
 * bom {
 *     imports(SpringBootPlugin.BOM_COORDINATES)
 * }
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class BomPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaPlatformPlugin.class);
		project.getExtensions().getByType(JavaPlatformExtension.class).allowDependencies();
		createApiEnforcedConfiguration(project);
		project.getExtensions().create("bom", BomExtension.class, project);
	}

	private void createApiEnforcedConfiguration(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration apiEnforced = configurations.dependencyScope(Configurations.API_ENFORCED).get();
		configurations.named(JavaPlatformPlugin.ENFORCED_API_ELEMENTS_CONFIGURATION_NAME,
				(configuration) -> configuration.extendsFrom(apiEnforced));
		configurations.named(JavaPlatformPlugin.ENFORCED_RUNTIME_ELEMENTS_CONFIGURATION_NAME,
				(configuration) -> configuration.extendsFrom(apiEnforced));
	}

}
