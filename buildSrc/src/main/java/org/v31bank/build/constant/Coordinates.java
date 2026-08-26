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
 * The external modules this build's own logic names.
 * <p>
 * A version is settled by the platform, so what is named here is the module alone.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Coordinates {

	/**
	 * Finds and runs what a JUnit Platform engine discovers, so every source set that
	 * runs tests needs it at runtime.
	 */
	public static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";

	private Coordinates() {
	}

}
