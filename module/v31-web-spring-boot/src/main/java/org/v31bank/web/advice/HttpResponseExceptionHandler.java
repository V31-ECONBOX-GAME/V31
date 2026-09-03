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

package org.v31bank.web.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.v31bank.core.response.HttpResponse;

/**
 * Answers every failure that escapes a controller with the platform's response envelope,
 * so a service does not report its failures in a different shape from its successes.
 * <p>
 * Extends {@link ResponseEntityExceptionHandler} because Spring raises its own exceptions
 * before application code runs — an unparseable path variable, a body that is not JSON.
 * An advice catching only {@link Exception} would answer all of those with {@code 500}.
 * <p>
 * An unrecognised failure is reported as {@code 500} with that status's own reason
 * phrase: the exception's message routinely names a table, a constraint or a host. That
 * goes to the log instead.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class HttpResponseExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(HttpResponseExceptionHandler.class);

	/**
	 * Answer anything unrecognised without saying what it was.
	 * @param ex the failure
	 * @return the response to send
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<HttpResponse<Void>> handleUnexpectedException(Exception ex) {
		logger.error("Unhandled failure", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(HttpResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
	}

	/**
	 * Replace the {@code ProblemDetail} Spring would send with the platform's envelope,
	 * leaving the status alone. Only the kind and status are logged — the message quotes
	 * what arrived, and these endpoints are sent names and account identifiers.
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		logger.debug("Rejected with {} ({})", statusCode, ex.getClass().getSimpleName());
		HttpResponse<?> envelope = (body instanceof HttpResponse<?> supplied) ? supplied
				: HttpResponse.error(statusCode.value(), reasonPhraseOf(statusCode));
		return super.handleExceptionInternal(ex, envelope, headers, statusCode, request);
	}

	/**
	 * Describe a status without assuming it is one {@link HttpStatus} declares: Spring
	 * raises {@code ResponseStatusException} with anything in {@code 100..999}, and
	 * throwing here would lose the envelope this class exists to write.
	 * @param statusCode the status Spring chose
	 * @return its reason phrase, or a generic one when it has none
	 */
	private static String reasonPhraseOf(HttpStatusCode statusCode) {
		HttpStatus resolved = HttpStatus.resolve(statusCode.value());
		return (resolved != null) ? resolved.getReasonPhrase() : "The request could not be completed";
	}

}
