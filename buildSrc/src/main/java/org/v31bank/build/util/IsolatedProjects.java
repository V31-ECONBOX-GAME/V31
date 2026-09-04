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

package org.v31bank.build.util;

import org.gradle.api.Project;
import org.gradle.api.project.IsolatedProject;

/**
 * The one project a project cannot ask another project for.
 *
 * @author Xander Wang
 */
public final class IsolatedProjects {

	private IsolatedProjects() {
	}

	/**
	 * The project the build is rooted at, as an isolated view rather than the project
	 * itself: an isolated project may not reach the root project. What is wanted of it —
	 * its directory, its name — is read from the view.
	 * @param project the project asking
	 * @return the isolated view of the project the build is rooted at
	 */
	public static IsolatedProject rootOf(Project project) {
		return project.getIsolated().getRootProject();
	}

}
