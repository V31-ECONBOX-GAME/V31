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

package org.v31bank.ledger.presentation.controller.v1;

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

import org.v31bank.core.response.HttpResponse;
import org.v31bank.ledger.application.dto.LedgerAccountPageQuery;
import org.v31bank.ledger.application.port.in.LedgerAccountUseCase;
import org.v31bank.ledger.domain.model.LedgerAccount;
import org.v31bank.ledger.presentation.dto.LedgerAccountRequest;
import org.v31bank.ledger.presentation.dto.LedgerAccountResponse;

/**
 * REST endpoints for managing ledger accounts.
 * <p>
 * Commands come back from the use case as an {@link HttpResponse} already carrying the
 * verdict, so this layer converts the payload to the wire record and puts the matching
 * status on the response.
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
		HttpResponse<LedgerAccount> result = this.ledgerAccountInputPort.create(request.code(), request.name(),
				request.type());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(LedgerAccountResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<LedgerAccountResponse>> get(@PathVariable UUID id) {
		return this.ledgerAccountInputPort.get(id)
			.map((ledgerAccount) -> ResponseEntity.ok(HttpResponse.ok(LedgerAccountResponse.from(ledgerAccount))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No ledger account exists with id " + id));
	}

	/**
	 * Return a page of records, newest first.
	 * @param query the filters and the pagination request
	 * @return the page of matching records
	 */
	@GetMapping
	public HttpResponse<List<LedgerAccountResponse>> page(LedgerAccountPageQuery query) {
		return this.ledgerAccountInputPort.page(query)
			.map((records) -> records.stream().map(LedgerAccountResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<LedgerAccountResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody LedgerAccountRequest request) {
		return toResponseEntity(this.ledgerAccountInputPort.update(id, request.code(), request.name(), request.type(),
				request.status()));
	}

	/**
	 * Answer a delete with the envelope carrying the record that was removed, rather than
	 * a bare {@code 204}, so that this endpoint is parsed like every other one.
	 * @param id the record to delete
	 * @return the response to send
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<LedgerAccountResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.ledgerAccountInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<LedgerAccountResponse>> toResponseEntity(
			HttpResponse<LedgerAccount> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(LedgerAccountResponse::from));
	}

	/**
	 * Recover the HTTP status belonging to an outcome.
	 * @param result the outcome to place
	 * @return the status to answer with
	 */
	private static int statusOf(HttpResponse<?> result) {
		return result.code();
	}

	private static <T> ResponseEntity<HttpResponse<T>> error(int code, String message) {
		return ResponseEntity.status(code).body(HttpResponse.error(code, message));
	}

}
