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

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlatformPlugin;

/**
 * DSL extensions for {@link BomPlugin}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class BomExtension {

	private final Project project;

	@Inject
	public BomExtension(Project project) {
		this.project = project;
	}

	public void imports(String coordinate) {
		DependencyHandler dependencies = this.project.getDependencies();
		dependencies.add(JavaPlatformPlugin.API_CONFIGURATION_NAME, dependencies.platform(coordinate));
		dependencies.add(BomPlugin.API_ENFORCED_CONFIGURATION_NAME, dependencies.enforcedPlatform(coordinate));
	}

}
