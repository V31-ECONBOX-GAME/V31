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

package org.v31bank.core.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PageQuery}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
class PageQueryTests {

	@Test
	void startsAtTheFirstPage() {
		PageQuery query = new PageQuery();
		assertThat(query.getPageNumber()).isEqualTo(PageQuery.FIRST_PAGE_NUMBER);
		assertThat(query.getPageSize()).isEqualTo(PageQuery.DEFAULT_PAGE_SIZE);
	}

	@Test
	void treatsAPageBeforeTheFirstAsTheFirst() {
		assertThat(query(0, 10).normalizedPageNumber()).isEqualTo(PageQuery.FIRST_PAGE_NUMBER);
		assertThat(query(-5, 10).normalizedPageNumber()).isEqualTo(PageQuery.FIRST_PAGE_NUMBER);
		assertThat(query(3, 10).normalizedPageNumber()).isEqualTo(3);
	}

	/**
	 * The page size arrives from the caller, so it is the one number in a listing request
	 * that can be used to ask for the whole table at once.
	 */
	@Test
	void refusesToReturnMoreThanTheMaximumPage() {
		assertThat(query(1, PageQuery.MAX_PAGE_SIZE + 1).normalizedPageSize()).isEqualTo(PageQuery.MAX_PAGE_SIZE);
		assertThat(query(1, Integer.MAX_VALUE).normalizedPageSize()).isEqualTo(PageQuery.MAX_PAGE_SIZE);
	}

	@Test
	void refusesAPageOfNothing() {
		assertThat(query(1, 0).normalizedPageSize()).isEqualTo(1);
		assertThat(query(1, -10).normalizedPageSize()).isEqualTo(1);
	}

	private static PageQuery query(int pageNumber, int pageSize) {
		PageQuery query = new PageQuery();
		query.setPageNumber(pageNumber);
		query.setPageSize(pageSize);
		return query;
	}

}
