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

package org.v31bank.customer.domain.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.v31bank.customer.domain.model.CustomerCategory;

/**
 * Structural rules of the customer category hierarchy.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class CustomerCategoryHierarchy {

	private CustomerCategoryHierarchy() {
	}

	public static boolean createsCycle(UUID nodeId, UUID parentId, Function<UUID, Optional<CustomerCategory>> lookup) {
		Set<UUID> visited = new HashSet<>();
		UUID ancestorId = parentId;
		while (ancestorId != null) {
			if (ancestorId.equals(nodeId) || !visited.add(ancestorId)) {
				return true;
			}
			ancestorId = lookup.apply(ancestorId).map(CustomerCategory::getParentId).orElse(null);
		}
		return false;
	}

}
