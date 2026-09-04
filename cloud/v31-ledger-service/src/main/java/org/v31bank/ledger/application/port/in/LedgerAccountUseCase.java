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

package org.v31bank.ledger.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.core.HttpResponse;
import org.v31bank.ledger.application.dto.LedgerAccountPageQuery;
import org.v31bank.ledger.domain.constant.LedgerAccountStatus;
import org.v31bank.ledger.domain.constant.LedgerAccountType;
import org.v31bank.ledger.domain.model.LedgerAccount;

/**
 * Use cases for managing ledger accounts.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface LedgerAccountUseCase {

	HttpResponse<LedgerAccount> create(String code, String name, LedgerAccountType type);

	Optional<LedgerAccount> get(UUID id);

	HttpResponse<List<LedgerAccount>> page(LedgerAccountPageQuery query);

	HttpResponse<LedgerAccount> update(UUID id, String code, String name, LedgerAccountType type,
			LedgerAccountStatus status);

	HttpResponse<LedgerAccount> delete(UUID id);

}
