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

package org.v31bank.customer.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.core.HttpResponse;
import org.v31bank.customer.application.dto.CustomerCategoryPageQuery;
import org.v31bank.customer.domain.constant.CustomerCategoryStatus;
import org.v31bank.customer.domain.model.CustomerCategory;

/**
 * Output port for {@link CustomerCategory} persistence, implemented by the infrastructure
 * layer.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface CustomerCategoryPort {

	CustomerCategory save(CustomerCategory category);

	Optional<CustomerCategory> findById(UUID id);

	List<CustomerCategory> findAll(CustomerCategoryStatus status);

	HttpResponse<List<CustomerCategory>> findPage(CustomerCategoryPageQuery query);

	boolean existsByParentId(UUID parentId);

	boolean existsByCode(String code);

	void delete(CustomerCategory category);

}
