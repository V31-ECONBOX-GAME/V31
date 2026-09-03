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

package org.v31bank.jooq.util;

import java.util.List;
import java.util.Objects;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectLimitStep;

import org.v31bank.core.request.PageQuery;
import org.v31bank.core.response.HttpResponse;

/**
 * Runs a jOOQ query one page at a time, since jOOQ has no equivalent of Spring Data's
 * repository pagination.
 * <p>
 * The query must carry its own filtering and ordering but no limit: the count and the
 * page are both derived from it. Without ordering a row can appear on two consecutive
 * pages or on neither.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class JooqPages {

	private JooqPages() {
	}

	/**
	 * Count the rows the query matches and fetch the page asked for, skipping the second
	 * round trip when nothing matched.
	 * @param dsl the context to run the count with
	 * @param query the query to page over, filtered and ordered but not limited
	 * @param page the pagination asked for
	 * @param <R> the record type the query returns
	 * @return the response carrying the page and the total across all pages
	 */
	public static <R extends Record> HttpResponse<List<R>> fetch(DSLContext dsl, SelectLimitStep<R> query,
			PageQuery page) {
		Objects.requireNonNull(dsl, "dsl must not be null");
		Objects.requireNonNull(query, "query must not be null");
		Objects.requireNonNull(page, "page must not be null");
		int number = page.normalizedPageNumber();
		int size = page.normalizedPageSize();
		long total = dsl.fetchCountLarge(query);
		if (total == 0) {
			return HttpResponse.page(List.of(), 0);
		}
		Result<R> records = query.offset((number - 1) * (long) size).limit(size).fetch();
		return HttpResponse.page(records, total);
	}

}
