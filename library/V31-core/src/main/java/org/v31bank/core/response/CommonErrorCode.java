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

import java.util.Optional;

/**
 * The failures any service can report. Ones specific to a domain belong in that service's
 * own {@link ErrorCode} enum.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public enum CommonErrorCode implements ErrorCode {

	/**
	 * The request was malformed, or a field failed validation.
	 */
	VALIDATION_FAILED(400, "The request is not valid"),

	/**
	 * No credentials were presented, or the ones presented were not accepted.
	 */
	UNAUTHENTICATED(401, "Authentication is required"),

	/**
	 * The caller is known but is not allowed to perform this operation.
	 */
	FORBIDDEN(403, "This operation is not permitted"),

	/**
	 * The addressed resource does not exist, or is not visible to this caller.
	 */
	NOT_FOUND(404, "The requested resource does not exist"),

	/**
	 * The request conflicts with the current state, such as a duplicate value or an edit
	 * against a stale version.
	 */
	CONFLICT(409, "The request conflicts with the current state of the resource"),

	/**
	 * The request was well formed but a business rule refused it.
	 */
	UNPROCESSABLE(422, "The request could not be processed"),

	/**
	 * The caller exceeded its quota and should retry later.
	 */
	RATE_LIMITED(429, "Too many requests, please retry later"),

	/**
	 * The service failed for a reason the caller cannot act on; the cause belongs in the
	 * logs, correlated by the trace identifier.
	 */
	INTERNAL_ERROR(500, "The request could not be completed"),

	/**
	 * A service this call depends on is unavailable. Unlike {@link #INTERNAL_ERROR},
	 * retrying is usually worthwhile.
	 */
	DEPENDENCY_UNAVAILABLE(503, "A required service is temporarily unavailable"),

	/**
	 * A dependency did not answer in time, so the outcome is unknown and a retry must
	 * send the same {@code Idempotency-Key}.
	 */
	DEPENDENCY_TIMEOUT(504, "A required service did not respond in time");

	private final int httpStatus;

	private final String defaultMessage;

	CommonErrorCode(int httpStatus, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.defaultMessage = defaultMessage;
	}

	/**
	 * Look up a code by its wire form, for a layer holding only a serialised
	 * {@link ApiResponse}.
	 * @param code the code to look up, which may be {@code null}
	 * @return the matching code, or empty when it is not one of these
	 */
	public static Optional<CommonErrorCode> find(String code) {
		if (code == null) {
			return Optional.empty();
		}
		for (CommonErrorCode candidate : values()) {
			if (candidate.name().equals(code)) {
				return Optional.of(candidate);
			}
		}
		return Optional.empty();
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public String defaultMessage() {
		return this.defaultMessage;
	}

	@Override
	public int httpStatus() {
		return this.httpStatus;
	}

}
