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

package org.v31bank.build.classpath;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Internal;

/**
 * Shared by the classpath checks.
 *
 * @author Xander Wang
 */
public abstract class ClasspathCheck extends DefaultTask {

	protected ClasspathCheck() {
		getOutputs().upToDateWhen((_) -> true);
	}

	@Classpath
	public abstract ConfigurableFileCollection getClasspathFiles();

	@Internal
	public abstract Property<ResolvedComponentResult> getRootComponent();

	public void setClasspath(Configuration classpath) {
		getClasspathFiles().setFrom(classpath);
		getRootComponent().set(classpath.getIncoming().getResolutionResult().getRootComponent());
	}

}
