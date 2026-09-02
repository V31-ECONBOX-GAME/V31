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

import org.junit.jupiter.api.Test;

import org.v31bank.core.response.CommonErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link ApiException}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
class ApiExceptionTests {

	@Test
	void carriesTheMessageDescribingTheOccurrence() {
		ApiException exception = new ApiException(CommonErrorCode.NOT_FOUND, "No account exists with id 7");
		assertThat(exception.getMessage()).isEqualTo("No account exists with id 7");
	}

	@Test
	void keepsTheFailureItWasTranslatedFrom() {
		IllegalStateException cause = new IllegalStateException("unique constraint violated");
		ApiException exception = new ApiException(CommonErrorCode.CONFLICT, "Code already in use", cause);
		assertThat(exception.getCause()).isSameAs(cause);
		assertThat(exception.getErrorCode().httpStatus()).isEqualTo(409);
	}

	@Test
	void rejectsMissingCode() {
		assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> new ApiException(null, "boom"));
	}

}
