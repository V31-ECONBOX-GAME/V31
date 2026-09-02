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

package org.v31bank.core.constant;

/**
 * Names of the HTTP headers the platform reads and forwards.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class ApiHeaders {

	/**
	 * Correlates one request's log lines, downstream calls and response. Taken from the
	 * caller when present, generated otherwise.
	 */
	public static final String REQUEST_ID = "X-Request-Id";

	private ApiHeaders() {
	}

}
