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

package org.v31bank.core.exception;

import java.io.Serial;
import java.util.Objects;

import org.v31bank.core.response.ErrorCode;

/**
 * Thrown when a request is refused for a reason the caller is meant to see. The
 * {@link ErrorCode} supplies the status and wire code, so one handler answers every such
 * failure and a new one costs an enum constant rather than an exception class. The
 * message reaches the caller, so it must disclose nothing they may not see. A bug or a
 * broken dependency is not one of these: let it propagate instead.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ApiException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;

	public ApiException(ErrorCode errorCode, String message) {
		this(errorCode, message, null);
	}

	public ApiException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}

}
