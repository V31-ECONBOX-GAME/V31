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

package org.v31bank.transfer.presentation.controller.v1;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.v31bank.core.HttpResponse;
import org.v31bank.transfer.application.dto.TransferLimitPageQuery;
import org.v31bank.transfer.application.port.in.TransferLimitUseCase;
import org.v31bank.transfer.domain.model.TransferLimit;
import org.v31bank.transfer.presentation.dto.TransferLimitRequest;
import org.v31bank.transfer.presentation.dto.TransferLimitResponse;

/**
 * REST endpoints for managing transfer limits.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(TransferLimitController.PATH)
public class TransferLimitController {

	static final String PATH = "/api/v1/transfer-limits";

	private final TransferLimitUseCase transferLimitInputPort;

	public TransferLimitController(TransferLimitUseCase transferLimitInputPort) {
		this.transferLimitInputPort = transferLimitInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<TransferLimitResponse>> create(
			@Valid @RequestBody TransferLimitRequest request) {
		HttpResponse<TransferLimit> result = this.transferLimitInputPort.create(request.code(), request.name(),
				request.dailyMax());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(TransferLimitResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<TransferLimitResponse>> get(@PathVariable UUID id) {
		return this.transferLimitInputPort.get(id)
			.map((transferLimit) -> ResponseEntity.ok(HttpResponse.ok(TransferLimitResponse.from(transferLimit))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No transfer limit exists with id " + id));
	}

	@GetMapping
	public HttpResponse<List<TransferLimitResponse>> page(TransferLimitPageQuery query) {
		return this.transferLimitInputPort.page(query)
			.map((records) -> records.stream().map(TransferLimitResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<TransferLimitResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody TransferLimitRequest request) {
		return toResponseEntity(this.transferLimitInputPort.update(id, request.code(), request.name(),
				request.dailyMax(), request.status()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<TransferLimitResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.transferLimitInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<TransferLimitResponse>> toResponseEntity(
			HttpResponse<TransferLimit> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(TransferLimitResponse::from));
	}

	private static int statusOf(HttpResponse<?> result) {
		return result.code();
	}

	private static <T> ResponseEntity<HttpResponse<T>> error(int code, String message) {
		return ResponseEntity.status(code).body(HttpResponse.error(code, message));
	}

}
