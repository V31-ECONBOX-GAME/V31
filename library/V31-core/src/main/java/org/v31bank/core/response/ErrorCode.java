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

package org.v31bank.core.response;

import java.io.Serializable;

/**
 * A machine-readable reason a request failed, one enum per service. The code is the
 * contract, not the message.
 * <p>
 * {@code Serializable} because {@link org.v31bank.core.exception.ApiException} carries
 * one and every {@code Throwable} is serializable.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface ErrorCode extends Serializable {

	String code();

	String defaultMessage();

	/**
	 * Return the HTTP status to answer with, 422 unless a code says otherwise.
	 * @return the HTTP status code
	 */
	default int httpStatus() {
		return 422;
	}

}
