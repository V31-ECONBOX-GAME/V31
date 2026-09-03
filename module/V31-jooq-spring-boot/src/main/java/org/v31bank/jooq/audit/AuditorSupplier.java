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

package org.v31bank.jooq.audit;

import java.util.Optional;

/**
 * Answers who is acting, so {@link AuditRecordListener} can record it against every row a
 * request writes. jOOQ's counterpart to Spring Data's {@code AuditorAware}, declared here
 * so a jOOQ-only service need not bring in Spring Data.
 * <p>
 * Read once per statement, on the thread running it — an implementation backed by a
 * request scope has to account for work handed to another thread.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@FunctionalInterface
public interface AuditorSupplier {

	/**
	 * Return who is currently acting.
	 * @return the auditor, or empty when nobody is identified, which leaves the audit
	 * columns as they are rather than filling them with a placeholder
	 */
	Optional<String> currentAuditor();

}
