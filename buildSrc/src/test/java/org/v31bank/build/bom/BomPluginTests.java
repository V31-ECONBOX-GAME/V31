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

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlatformExtension;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for {@link BomPlugin}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
class BomPluginTests {

	@Test
	void appliesJavaPlatform() {
		assertThat(project().getPlugins().hasPlugin(JavaPlatformPlugin.class)).isTrue();
	}

	@Test
	void makesTheApiConfigurationAvailableToImportABomInto() {
		assertThat(project().getConfigurations().findByName(JavaPlatformPlugin.API_CONFIGURATION_NAME)).isNotNull();
	}

	@Test
	void allowsThePlatformToDependOnAnother() {
		Project project = project();
		assertThatCode(() -> project.getDependencies()
			.add(JavaPlatformPlugin.API_CONFIGURATION_NAME,
					project.getDependencies().platform("org.springframework.boot:spring-boot-dependencies:4.1.1")))
			.doesNotThrowAnyException();
	}

	@Test
	void addsNoBomOfItsOwn() {
		assertThat(project().getConfigurations().getByName(JavaPlatformPlugin.API_CONFIGURATION_NAME).getDependencies())
			.isEmpty();
	}

	@Test
	void exposesTheJavaPlatformExtension() {
		assertThat(project().getExtensions().findByType(JavaPlatformExtension.class)).isNotNull();
	}

	private Project project() {
		Project project = ProjectBuilder.builder().build();
		project.getPlugins().apply(BomPlugin.class);
		return project;
	}

}
