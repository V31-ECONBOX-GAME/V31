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

package org.v31bank.build.constant;

/**
 * The properties this build reads from {@code gradle.properties}.
 * <p>
 * None of them is defaulted: a value the build guesses at is a value nobody notices is
 * wrong.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class GradleProperties {

	/**
	 * Names the JDK that compiles the code and runs the tests.
	 */
	public static final String BUILD_JAVA_VERSION = "buildJavaVersion";

	/**
	 * Names the JDK the result must run on, which is a different question.
	 */
	public static final String RUNTIME_JAVA_VERSION = "runtimeJavaVersion";

	/**
	 * The checkstyle release the checks run on, rather than Gradle's default.
	 */
	public static final String CHECKSTYLE_TOOL_VERSION = "checkstyleToolVersion";

	/**
	 * The buf release the proto toolchain is installed at.
	 */
	public static final String BUF_VERSION = "bufVersion";

	private GradleProperties() {
	}

}
