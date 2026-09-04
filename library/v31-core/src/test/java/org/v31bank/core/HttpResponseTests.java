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

package org.v31bank.core;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link HttpResponse}.
 *
 * @author Xander Wang
 */
class HttpResponseTests {

	@Test
	void okWithoutPayloadReportsSuccess() {
		HttpResponse<Void> response = HttpResponse.ok();
		assertThat(response.succeeded()).isTrue();
		assertThat(response.code()).isEqualTo(HttpResponse.SUCCESS);
		assertThat(response.data()).isNull();
	}

	@Test
	void okCarriesPayload() {
		HttpResponse<String> response = HttpResponse.ok("42");
		assertThat(response.succeeded()).isTrue();
		assertThat(response.data()).isEqualTo("42");
	}

	@Test
	void okCarriesMessage() {
		HttpResponse<String> response = HttpResponse.ok("42", "Transfer submitted");
		assertThat(response.code()).isEqualTo(HttpResponse.SUCCESS);
		assertThat(response.message()).isEqualTo("Transfer submitted");
	}

	@Test
	void errorCarriesTheStatusItWasRefusedWith() {
		HttpResponse<String> response = HttpResponse.error(404, "No account exists with id 7");
		assertThat(response.succeeded()).isFalse();
		assertThat(response.code()).isEqualTo(404);
		assertThat(response.message()).isEqualTo("No account exists with id 7");
		assertThat(response.data()).isNull();
	}

	@Test
	void errorReplacesTheMessage() {
		HttpResponse<String> response = HttpResponse.error(404, "No account exists with id 7");
		assertThat(response.code()).isEqualTo(404);
		assertThat(response.message()).isEqualTo("No account exists with id 7");
	}

	@Test
	void successIsTheCode() {
		assertThat(new HttpResponse<>(404, "gone", null).succeeded()).isFalse();
		assertThat(new HttpResponse<>(HttpResponse.SUCCESS, "fine", "42").succeeded()).isTrue();
	}

	@Test
	void carriesNoTotalWhenThePayloadIsNotAPage() {
		assertThat(HttpResponse.ok("42").total()).isNull();
		assertThat(HttpResponse.ok().total()).isNull();
		assertThat(HttpResponse.error(404, "gone").total()).isNull();
		assertThat(HttpResponse.page(List.of(), 0).total()).isZero();
	}

	@Test
	void pageCarriesTheTotalAcrossAllPages() {
		HttpResponse<List<String>> page = HttpResponse.page(List.of("a", "b"), 21);
		assertThat(page.succeeded()).isTrue();
		assertThat(page.data()).containsExactly("a", "b");
		assertThat(page.total()).isEqualTo(21);
	}

	@Test
	void mapKeepsTheTotal() {
		HttpResponse<List<Integer>> lengths = HttpResponse.page(List.of("aa", "bbb"), 21)
			.map((records) -> records.stream().map(String::length).toList());
		assertThat(lengths.data()).containsExactly(2, 3);
		assertThat(lengths.total()).isEqualTo(21);
	}

	@Test
	void mapConvertsThePayloadAndKeepsTheVerdict() {
		HttpResponse<Integer> mapped = HttpResponse.ok("42", "Done").map(Integer::parseInt);
		assertThat(mapped.data()).isEqualTo(42);
		assertThat(mapped.succeeded()).isTrue();
		assertThat(mapped.code()).isEqualTo(HttpResponse.SUCCESS);
		assertThat(mapped.message()).isEqualTo("Done");
	}

	@Test
	void mapLeavesAFailureAlone() {
		HttpResponse<String> failure = HttpResponse.error(404, "gone");
		HttpResponse<Integer> mapped = failure.map((_) -> {
			throw new AssertionError("the converter must not run when there is no payload");
		});
		assertThat(mapped.succeeded()).isFalse();
		assertThat(mapped.code()).isEqualTo(404);
		assertThat(mapped.message()).isEqualTo("gone");
		assertThat(mapped.data()).isNull();
	}

	@Test
	void mapRejectsAMissingConverter() {
		assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> HttpResponse.ok("42").map(null));
	}

}
