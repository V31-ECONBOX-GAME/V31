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

package org.v31bank.notification.presentation.controller.v1;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.v31bank.core.HttpResponse;
import org.v31bank.notification.application.dto.LedgerAccountSummary;
import org.v31bank.notification.application.port.in.LedgerAccountUseCase;
import org.v31bank.notification.presentation.dto.LedgerAccountRequest;
import org.v31bank.notification.presentation.dto.LedgerAccountResponse;

/**
 * The chart of accounts, as this service exposes it over HTTP.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(LedgerAccountController.PATH)
public class LedgerAccountController {

	static final String PATH = "/api/v1/ledger-accounts";

	private final LedgerAccountUseCase ledgerAccountInputPort;

	public LedgerAccountController(LedgerAccountUseCase ledgerAccountInputPort) {
		this.ledgerAccountInputPort = ledgerAccountInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<LedgerAccountResponse>> create(
			@Valid @RequestBody LedgerAccountRequest request) {
		LedgerAccountSummary created = this.ledgerAccountInputPort.create(request.code(), request.name(),
				request.type());
		return ResponseEntity.created(URI.create(PATH + "/" + created.id()))
			.body(HttpResponse.ok(LedgerAccountResponse.from(created)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<LedgerAccountResponse>> get(@PathVariable UUID id) {
		return this.ledgerAccountInputPort.get(id)
			.map((summary) -> ResponseEntity.ok(HttpResponse.ok(LedgerAccountResponse.from(summary))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No ledger account exists with id " + id));
	}

	@GetMapping
	public HttpResponse<List<LedgerAccountResponse>> page(@RequestParam(defaultValue = "1") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String code) {
		return this.ledgerAccountInputPort.page(pageNumber, pageSize, code)
			.map((records) -> records.stream().map(LedgerAccountResponse::from).toList());
	}

	@PutMapping("/{id}")
	public HttpResponse<LedgerAccountResponse> update(@PathVariable UUID id,
			@Valid @RequestBody LedgerAccountRequest request) {
		return HttpResponse.ok(LedgerAccountResponse.from(this.ledgerAccountInputPort.update(id, request.code(),
				request.name(), request.type(), request.status())));
	}

	@DeleteMapping("/{id}")
	public HttpResponse<LedgerAccountResponse> delete(@PathVariable UUID id) {
		return HttpResponse.ok(LedgerAccountResponse.from(this.ledgerAccountInputPort.delete(id)));
	}

	private static <T> ResponseEntity<HttpResponse<T>> error(int code, String message) {
		return ResponseEntity.status(code).body(HttpResponse.error(code, message));
	}

}
