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

package org.v31bank.compliance.presentation.controller.v1;

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

import org.v31bank.compliance.application.dto.ComplianceCasePageQuery;
import org.v31bank.compliance.application.port.in.ComplianceCaseUseCase;
import org.v31bank.compliance.domain.model.ComplianceCase;
import org.v31bank.compliance.presentation.dto.ComplianceCaseRequest;
import org.v31bank.compliance.presentation.dto.ComplianceCaseResponse;
import org.v31bank.core.response.HttpResponse;

/**
 * REST endpoints for managing compliance cases.
 * <p>
 * Commands come back from the use case as an {@link HttpResponse} already carrying the
 * verdict, so this layer converts the payload from the domain model to the wire record
 * and puts the matching status on the response.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(ComplianceCaseController.PATH)
public class ComplianceCaseController {

	static final String PATH = "/api/v1/compliance-cases";

	private final ComplianceCaseUseCase complianceCaseInputPort;

	public ComplianceCaseController(ComplianceCaseUseCase complianceCaseInputPort) {
		this.complianceCaseInputPort = complianceCaseInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<ComplianceCaseResponse>> create(
			@Valid @RequestBody ComplianceCaseRequest request) {
		HttpResponse<ComplianceCase> result = this.complianceCaseInputPort.create(request.caseNumber(),
				request.customerId(), request.type(), request.summary());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(ComplianceCaseResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<ComplianceCaseResponse>> get(@PathVariable UUID id) {
		return this.complianceCaseInputPort.get(id)
			.map((complianceCase) -> ResponseEntity.ok(HttpResponse.ok(ComplianceCaseResponse.from(complianceCase))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No compliance case exists with id " + id));
	}

	/**
	 * Return a page of cases, newest first. Pass {@code customerId}, {@code status},
	 * {@code type} or {@code caseNumber} to narrow it.
	 * @param query the filters and the pagination request
	 * @return the page of matching cases
	 */
	@GetMapping
	public HttpResponse<List<ComplianceCaseResponse>> page(ComplianceCasePageQuery query) {
		return this.complianceCaseInputPort.page(query)
			.map((records) -> records.stream().map(ComplianceCaseResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<ComplianceCaseResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody ComplianceCaseRequest request) {
		return toResponseEntity(this.complianceCaseInputPort.update(id, request.caseNumber(), request.customerId(),
				request.type(), request.status(), request.summary()));
	}

	/**
	 * Answer a delete with the envelope carrying the case that was removed, rather than a
	 * bare {@code 204}, so that this endpoint is parsed like every other one.
	 * @param id the case to delete
	 * @return the response to send
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<ComplianceCaseResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.complianceCaseInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<ComplianceCaseResponse>> toResponseEntity(
			HttpResponse<ComplianceCase> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(ComplianceCaseResponse::from));
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
