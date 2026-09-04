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

package org.v31bank.transfer.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.core.HttpResponse;
import org.v31bank.transfer.application.dto.TransferLimitPageQuery;
import org.v31bank.transfer.domain.constant.TransferLimitStatus;
import org.v31bank.transfer.domain.model.TransferLimit;

/**
 * Use cases for managing transfer limits.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface TransferLimitUseCase {

	HttpResponse<TransferLimit> create(String code, String name, BigDecimal dailyMax);

	Optional<TransferLimit> get(UUID id);

	HttpResponse<List<TransferLimit>> page(TransferLimitPageQuery query);

	HttpResponse<TransferLimit> update(UUID id, String code, String name, BigDecimal dailyMax,
			TransferLimitStatus status);

	HttpResponse<TransferLimit> delete(UUID id);

}
