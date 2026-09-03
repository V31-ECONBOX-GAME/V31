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

/**
 * The pagination a caller asked for, numbered from one and read through
 * {@link #normalizedPageNumber()} and {@link #normalizedPageSize()}, which clamp what
 * arrived from outside.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class PageQuery {

	/**
	 * The number of the first page.
	 */
	public static final int FIRST_PAGE_NUMBER = 1;

	/**
	 * The page size applied when none is asked for.
	 */
	public static final int DEFAULT_PAGE_SIZE = 10;

	/**
	 * The largest page a caller may have. More is clamped to this, not refused.
	 */
	public static final int MAX_PAGE_SIZE = 500;

	private int pageNumber = FIRST_PAGE_NUMBER;

	private int pageSize = DEFAULT_PAGE_SIZE;

	public int getPageNumber() {
		return this.pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int normalizedPageNumber() {
		return Math.max(this.pageNumber, FIRST_PAGE_NUMBER);
	}

	public int normalizedPageSize() {
		return Math.clamp(this.pageSize, 1, MAX_PAGE_SIZE);
	}

}
