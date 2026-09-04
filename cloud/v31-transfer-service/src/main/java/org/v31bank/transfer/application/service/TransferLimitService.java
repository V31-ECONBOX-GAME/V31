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

package org.v31bank.transfer.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.v31bank.core.HttpResponse;
import org.v31bank.transfer.application.dto.TransferLimitPageQuery;
import org.v31bank.transfer.application.port.in.TransferLimitUseCase;
import org.v31bank.transfer.application.port.out.TransferLimitPort;
import org.v31bank.transfer.domain.constant.TransferLimitStatus;
import org.v31bank.transfer.domain.model.TransferLimit;

/**
 * Default {@link TransferLimitUseCase} implementation.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@Service
@Transactional
public class TransferLimitService implements TransferLimitUseCase {

	private final TransferLimitPort transferLimitRepository;

	public TransferLimitService(TransferLimitPort transferLimitRepository) {
		this.transferLimitRepository = transferLimitRepository;
	}

	@Override
	public HttpResponse<TransferLimit> create(String code, String name, BigDecimal dailyMax) {
		if (this.transferLimitRepository.existsByCode(code)) {
			return HttpResponse.error(HttpStatus.CONFLICT.value(), "Code '" + code + "' is already in use");
		}
		TransferLimit transferLimit = new TransferLimit();
		transferLimit.setCode(code);
		transferLimit.setName(name);
		transferLimit.setDailyMax(dailyMax);
		return HttpResponse.ok(this.transferLimitRepository.save(transferLimit));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TransferLimit> get(UUID id) {
		return this.transferLimitRepository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public HttpResponse<List<TransferLimit>> page(TransferLimitPageQuery query) {
		return this.transferLimitRepository.findPage(query);
	}

	@Override
	public HttpResponse<TransferLimit> update(UUID id, String code, String name, BigDecimal dailyMax,
			TransferLimitStatus status) {
		Optional<TransferLimit> found = this.transferLimitRepository.findById(id);
		if (found.isEmpty()) {
			return HttpResponse.error(HttpStatus.NOT_FOUND.value(), "No transfer limit exists with id " + id);
		}
		TransferLimit transferLimit = found.get();
		if (!transferLimit.getCode().equals(code) && this.transferLimitRepository.existsByCode(code)) {
			return HttpResponse.error(HttpStatus.CONFLICT.value(), "Code '" + code + "' is already in use");
		}
		transferLimit.setCode(code);
		transferLimit.setName(name);
		transferLimit.setDailyMax(dailyMax);
		if (status != null) {
			transferLimit.setStatus(status);
		}
		return HttpResponse.ok(this.transferLimitRepository.save(transferLimit));
	}

	@Override
	public HttpResponse<TransferLimit> delete(UUID id) {
		Optional<TransferLimit> found = this.transferLimitRepository.findById(id);
		if (found.isEmpty()) {
			return HttpResponse.error(HttpStatus.NOT_FOUND.value(), "No transfer limit exists with id " + id);
		}
		TransferLimit transferLimit = found.get();
		this.transferLimitRepository.delete(transferLimit);
		return HttpResponse.ok(transferLimit);
	}

}
