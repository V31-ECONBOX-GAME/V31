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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The body every REST endpoint returns, so a caller parses one shape whether the call
 * succeeded or failed. {@code success} is derived from the code, never stored as given,
 * so an instance cannot claim success while carrying a failure.
 *
 * @param success whether the call succeeded, derived from the code
 * @param code {@code "OK"}, or the {@link ErrorCode} that refused the request
 * @param message a description for the caller, disclosing nothing they may not see
 * @param data the payload, absent on failure
 * @param violations the rejected fields, present only when validation failed
 * @param traceId correlates this response with the server logs of the same request
 * @param timestamp when the response was produced, by the server's clock
 * @param <T> the type of the payload
 * @author Xander Wang
 * @since 0.2.0
 */
public record ApiResponse<T>(boolean success, String code, String message, T data, List<FieldViolation> violations,
		String traceId, Instant timestamp) {

	static final String SUCCESS_CODE = "OK";

	private static final String SUCCESS_MESSAGE = "Success";

	public ApiResponse {
		Objects.requireNonNull(code, "code must not be null");
		success = SUCCESS_CODE.equals(code);
		violations = (violations != null) ? List.copyOf(violations) : null;
	}

	public static ApiResponse<Void> ok() {
		return ok(null);
	}

	public static <T> ApiResponse<T> ok(T data) {
		return ok(data, SUCCESS_MESSAGE);
	}

	public static <T> ApiResponse<T> ok(T data, String message) {
		return new ApiResponse<>(true, SUCCESS_CODE, message, data, null, null, Instant.now());
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		Objects.requireNonNull(errorCode, "errorCode must not be null");
		return error(errorCode, errorCode.defaultMessage());
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
		Objects.requireNonNull(errorCode, "errorCode must not be null");
		return new ApiResponse<>(false, errorCode.code(), message, null, null, null, Instant.now());
	}

	/**
	 * Report {@link CommonErrorCode#VALIDATION_FAILED} with the rejected fields.
	 * @param violations the rejected fields
	 * @param <T> the type the endpoint returns when it succeeds
	 * @return the response
	 */
	public static <T> ApiResponse<T> invalid(List<FieldViolation> violations) {
		Objects.requireNonNull(violations, "violations must not be null");
		return new ApiResponse<>(false, CommonErrorCode.VALIDATION_FAILED.code(),
				CommonErrorCode.VALIDATION_FAILED.defaultMessage(), null, violations, null, Instant.now());
	}

	/**
	 * Return this response with its payload converted, the verdict intact. A failure has
	 * no payload, so the converter is not called.
	 * @param converter the conversion to apply to the payload
	 * @param <R> the target payload type
	 * @return the converted response
	 */
	public <R> ApiResponse<R> map(Function<? super T, ? extends R> converter) {
		Objects.requireNonNull(converter, "converter must not be null");
		R converted = (this.data != null) ? converter.apply(this.data) : null;
		return new ApiResponse<>(this.success, this.code, this.message, converted, this.violations, this.traceId,
				this.timestamp);
	}

	/**
	 * Return a copy stamped with the identifier that correlates it with the server logs.
	 * @param traceId the identifier to stamp, ignored when {@code null}
	 * @return the stamped copy, or this response when there is nothing to stamp
	 */
	public ApiResponse<T> withTraceId(String traceId) {
		if (traceId == null) {
			return this;
		}
		return new ApiResponse<>(this.success, this.code, this.message, this.data, this.violations, traceId,
				this.timestamp);
	}

}
