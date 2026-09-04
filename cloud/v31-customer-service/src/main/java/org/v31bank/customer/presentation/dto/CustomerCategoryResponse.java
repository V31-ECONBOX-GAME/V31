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

package org.v31bank.customer.presentation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.v31bank.customer.domain.constant.CustomerCategoryStatus;
import org.v31bank.customer.domain.model.CustomerCategory;

public record CustomerCategoryResponse(UUID id, UUID parentId, String code, String name, Integer sortOrder,
		CustomerCategoryStatus status, Instant createdDate, Instant lastModifiedDate,
		List<CustomerCategoryResponse> children) {

	public static CustomerCategoryResponse from(CustomerCategory category) {
		return of(category, List.of());
	}

	public static CustomerCategoryResponse fromTree(CustomerCategory category) {
		return of(category, category.getChildren().stream().map(CustomerCategoryResponse::fromTree).toList());
	}

	private static CustomerCategoryResponse of(CustomerCategory category, List<CustomerCategoryResponse> children) {
		return new CustomerCategoryResponse(category.getId(), category.getParentId(), category.getCode(),
				category.getName(), category.getSortOrder(), category.getStatus(), category.getCreatedDate(),
				category.getLastModifiedDate(), children);
	}

}
