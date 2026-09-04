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

package org.v31bank.bom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

/**
 * A build that resolves V31 by coordinate.
 *
 * @author Xander Wang
 */
final class ConsumerBuild {

	static final String VERSION = System.getProperty("v31Version");

	private static final String REPOSITORY = System.getProperty("testRepository");

	private final Path directory;

	ConsumerBuild(Path directory) {
		this.directory = directory;
	}

	BuildResult resolve(List<String> artifacts) throws IOException {
		Files.writeString(this.directory.resolve("settings.gradle.kts"), "rootProject.name = \"consumer\"\n");
		StringBuilder script = new StringBuilder("""
				plugins { java }
				repositories {
				    maven { url = uri("%s") }
				    mavenCentral()
				}
				dependencies {
				    implementation(platform("org.v31bank:v31-dependencies:%s"))
				""".formatted(REPOSITORY, VERSION));
		for (String artifact : artifacts) {
			script.append("    implementation(\"org.v31bank:%s\")%n".formatted(artifact));
		}
		script.append("""
				}
				tasks.register("resolve") {
				    val classpath = configurations.compileClasspath
				    doLast {
				        println("RESOLVED")
				        classpath.get().files.forEach { println(it.name) }
				    }
				}
				""");
		Files.writeString(this.directory.resolve("build.gradle.kts"), script.toString());
		return GradleRunner.create().withProjectDir(this.directory.toFile()).withArguments("resolve", "-q").build();
	}

}
