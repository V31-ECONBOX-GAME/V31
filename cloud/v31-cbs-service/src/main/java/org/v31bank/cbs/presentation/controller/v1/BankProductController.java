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

package org.v31bank.cbs.presentation.controller.v1;

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

import org.v31bank.cbs.application.dto.BankProductPageQuery;
import org.v31bank.cbs.application.port.in.BankProductUseCase;
import org.v31bank.cbs.domain.model.BankProduct;
import org.v31bank.cbs.presentation.dto.BankProductRequest;
import org.v31bank.cbs.presentation.dto.BankProductResponse;
import org.v31bank.core.response.HttpResponse;

/**
 * REST endpoints for managing the bank product catalogue.
 * <p>
 * Commands come back from the use case as an {@link HttpResponse} already carrying the
 * verdict, so this layer converts the payload from the domain model to the wire record
 * and puts the matching status on the response.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(BankProductController.PATH)
public class BankProductController {

	static final String PATH = "/api/v1/bank-products";

	private final BankProductUseCase bankProductInputPort;

	public BankProductController(BankProductUseCase bankProductInputPort) {
		this.bankProductInputPort = bankProductInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<BankProductResponse>> create(@Valid @RequestBody BankProductRequest request) {
		HttpResponse<BankProduct> result = this.bankProductInputPort.create(request.code(), request.name(),
				request.category(), request.interestRate());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(BankProductResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<BankProductResponse>> get(@PathVariable UUID id) {
		return this.bankProductInputPort.get(id)
			.map((product) -> ResponseEntity.ok(HttpResponse.ok(BankProductResponse.from(product))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No bank product exists with id " + id));
	}

	/**
	 * Return a page of products, newest first. Pass {@code category} or {@code status} to
	 * narrow it.
	 * @param query the filters and the pagination request
	 * @return the page of matching products
	 */
	@GetMapping
	public HttpResponse<List<BankProductResponse>> page(BankProductPageQuery query) {
		return this.bankProductInputPort.page(query)
			.map((records) -> records.stream().map(BankProductResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<BankProductResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody BankProductRequest request) {
		return toResponseEntity(this.bankProductInputPort.update(id, request.code(), request.name(), request.category(),
				request.status(), request.interestRate()));
	}

	/**
	 * Answer a delete with the envelope carrying the product that was removed, rather
	 * than a bare {@code 204}, so that this endpoint is parsed like every other one.
	 * @param id the product to delete
	 * @return the response to send
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<BankProductResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.bankProductInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<BankProductResponse>> toResponseEntity(
			HttpResponse<BankProduct> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(BankProductResponse::from));
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
