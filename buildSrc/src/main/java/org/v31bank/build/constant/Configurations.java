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
 * Build configuration names.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Configurations {

	public static final String DEPENDENCY_MANAGEMENT = "dependencyManagement";

	public static final String API_ENFORCED = "apiEnforced";

	public static final String OPTIONAL = "optional";

	public static final String MAVEN_REPOSITORY = "mavenRepository";

	public static final String PROTO_TOOLCHAIN = "protoToolchain";

	public static final String RESOLVED_PROTO_TOOLCHAIN = "resolvedProtoToolchain";

	public static final String STARTER_METADATA = "starterMetadata";

	public static final String CONFIGURATION_PROPERTIES_METADATA = "configurationPropertiesMetadata";

	public static final String CONFIGURATION_PROPERTIES_METADATA_USAGE = "configuration-properties-metadata";

	public static final String AUTO_CONFIGURATION_METADATA = "autoConfigurationMetadata";

	public static final String AUTO_CONFIGURATION_METADATA_USAGE = "auto-configuration-metadata";

	public static final String AUTO_CONFIGURATION_REQUIRED_CLASSPATH = "autoConfigurationRequiredClasspath";

	public static final String AUTO_CONFIGURATION_OPTIONAL_CLASSPATH = "autoConfigurationOptionalClasspath";

	private Configurations() {
	}

}
