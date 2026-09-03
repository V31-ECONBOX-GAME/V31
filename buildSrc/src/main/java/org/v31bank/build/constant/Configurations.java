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
 * The configuration names this build agrees on, kept here so no plugin reads another's
 * constants.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Configurations {

	/**
	 * Carries the enforced platform to every classpath, including the ones a source set
	 * added later brings.
	 */
	public static final String DEPENDENCY_MANAGEMENT = "dependencyManagement";

	/**
	 * Holds what a consumer asking a BOM for an enforced platform gets. A project cannot
	 * add to the enforced variants directly: they are consumable, and only a
	 * configuration they extend can be declared into.
	 */
	public static final String API_ENFORCED = "apiEnforced";

	/**
	 * Declares what is compiled and tested against here and left out of what a consumer
	 * resolves.
	 */
	public static final String OPTIONAL = "optional";

	/**
	 * Holds a published project's own repository and the repositories of everything it
	 * depends on.
	 */
	public static final String MAVEN_REPOSITORY = "mavenRepository";

	/**
	 * Offers the installed buf and generators, and is what an API project declares the
	 * install on.
	 */
	public static final String PROTO_TOOLCHAIN = "protoToolchain";

	/**
	 * Resolves {@link #PROTO_TOOLCHAIN}, which Gradle keeps apart from declaring it.
	 */
	public static final String RESOLVED_PROTO_TOOLCHAIN = "resolvedProtoToolchain";

	/**
	 * Offers the metadata describing a starter. Names the task that writes it as well.
	 */
	public static final String STARTER_METADATA = "starterMetadata";

	/**
	 * Offers a module's configuration metadata, generated or hand-written.
	 */
	public static final String CONFIGURATION_PROPERTIES_METADATA = "configurationPropertiesMetadata";

	/**
	 * Labels {@link #CONFIGURATION_PROPERTIES_METADATA}, so that a consumer naming the
	 * project alone still gets the metadata rather than the jar.
	 */
	public static final String CONFIGURATION_PROPERTIES_METADATA_USAGE = "configuration-properties-metadata";

	/**
	 * Offers a module's auto-configuration metadata. Names the task that writes it as
	 * well.
	 */
	public static final String AUTO_CONFIGURATION_METADATA = "autoConfigurationMetadata";

	/**
	 * Labels {@link #AUTO_CONFIGURATION_METADATA}.
	 */
	public static final String AUTO_CONFIGURATION_METADATA_USAGE = "auto-configuration-metadata";

	/**
	 * Holds everything a module always resolves.
	 */
	public static final String AUTO_CONFIGURATION_REQUIRED_CLASSPATH = "autoConfigurationRequiredClasspath";

	/**
	 * Holds what only an optional dependency brings in.
	 */
	public static final String AUTO_CONFIGURATION_OPTIONAL_CLASSPATH = "autoConfigurationOptionalClasspath";

	private Configurations() {
	}

}
