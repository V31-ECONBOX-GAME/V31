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

package org.v31bank.customer.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.core.HttpResponse;
import org.v31bank.customer.application.dto.CustomerCategoryPageQuery;
import org.v31bank.customer.domain.constant.CustomerCategoryStatus;
import org.v31bank.customer.domain.model.CustomerCategory;

/**
 * Use cases for managing the customer category hierarchy.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface CustomerCategoryUseCase {

	HttpResponse<CustomerCategory> create(String code, String name, UUID parentId, Integer sortOrder,
			CustomerCategoryStatus status);

	Optional<CustomerCategory> get(UUID id);

	HttpResponse<List<CustomerCategory>> page(CustomerCategoryPageQuery query);

	List<CustomerCategory> tree(UUID rootId, CustomerCategoryStatus status);

	HttpResponse<CustomerCategory> update(UUID id, String code, String name, UUID parentId, Integer sortOrder,
			CustomerCategoryStatus status);

	HttpResponse<CustomerCategory> delete(UUID id);

}
