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

package org.v31bank.notification.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.v31bank.core.HttpResponse;
import org.v31bank.notification.application.dto.LedgerAccountSummary;
import org.v31bank.notification.application.port.in.LedgerAccountUseCase;
import org.v31bank.notification.application.port.out.LedgerAccountPort;

/**
 * Default {@link LedgerAccountUseCase} implementation.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@Service
public class LedgerAccountService implements LedgerAccountUseCase {

	private final LedgerAccountPort ledgerAccountRepository;

	public LedgerAccountService(LedgerAccountPort ledgerAccountRepository) {
		this.ledgerAccountRepository = ledgerAccountRepository;
	}

	@Override
	public LedgerAccountSummary create(String code, String name, String type) {
		return this.ledgerAccountRepository.create(code, name, type);
	}

	@Override
	public Optional<LedgerAccountSummary> get(UUID id) {
		return this.ledgerAccountRepository.findById(id);
	}

	@Override
	public HttpResponse<List<LedgerAccountSummary>> page(int pageNumber, int pageSize, String code) {
		return this.ledgerAccountRepository.findPage(pageNumber, pageSize, code);
	}

	@Override
	public LedgerAccountSummary update(UUID id, String code, String name, String type, String status) {
		return this.ledgerAccountRepository.update(id, code, name, type, status);
	}

	@Override
	public LedgerAccountSummary delete(UUID id) {
		return this.ledgerAccountRepository.delete(id);
	}

}
