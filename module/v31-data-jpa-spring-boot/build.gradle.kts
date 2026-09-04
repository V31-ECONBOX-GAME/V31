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

plugins {
    `java-library`
    id("org.v31bank.auto-configuration")
    id("org.v31bank.optional-dependencies")
}

description = "V31 Data JPA auto-configuration"

dependencies {
    api(project(":library:v31-core"))
    api("org.springframework.boot:spring-boot-data-jpa")

    // Compiled against here, absent for a consumer: anything running an
    // auto-configuration already has it.
    optional("org.springframework.boot:spring-boot-autoconfigure")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // A real persistence unit, so the auditing this module configures is asserted
    // by what reaches a row rather than by which beans exist.
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testRuntimeOnly("com.h2database:h2")
}
