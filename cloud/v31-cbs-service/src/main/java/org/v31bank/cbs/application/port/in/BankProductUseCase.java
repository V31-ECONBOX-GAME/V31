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

package org.v31bank.cbs.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.cbs.application.dto.BankProductPageQuery;
import org.v31bank.cbs.domain.constant.BankProductCategory;
import org.v31bank.cbs.domain.constant.BankProductStatus;
import org.v31bank.cbs.domain.model.BankProduct;
import org.v31bank.core.HttpResponse;

/**
 * Use cases for managing the bank product catalogue.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface BankProductUseCase {

	HttpResponse<BankProduct> create(String code, String name, BankProductCategory category, BigDecimal interestRate);

	Optional<BankProduct> get(UUID id);

	HttpResponse<List<BankProduct>> page(BankProductPageQuery query);

	HttpResponse<BankProduct> update(UUID id, String code, String name, BankProductCategory category,
			BankProductStatus status, BigDecimal interestRate);

	HttpResponse<BankProduct> delete(UUID id);

}
