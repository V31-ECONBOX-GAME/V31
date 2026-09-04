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

package org.v31bank.cbs.application.dto;

import org.v31bank.cbs.domain.constant.BankProductCategory;
import org.v31bank.cbs.domain.constant.BankProductStatus;
import org.v31bank.core.PageQuery;

/**
 * Paginated bank product query, using one-based page numbering.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class BankProductPageQuery extends PageQuery {

	private BankProductCategory category;

	private BankProductStatus status;

	public BankProductCategory getCategory() {
		return this.category;
	}

	public void setCategory(BankProductCategory category) {
		this.category = category;
	}

	public BankProductStatus getStatus() {
		return this.status;
	}

	public void setStatus(BankProductStatus status) {
		this.status = status;
	}

}
