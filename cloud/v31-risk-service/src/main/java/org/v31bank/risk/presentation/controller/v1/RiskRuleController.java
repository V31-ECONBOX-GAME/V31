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

package org.v31bank.risk.presentation.controller.v1;

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
import org.v31bank.risk.application.dto.RiskRulePageQuery;
import org.v31bank.risk.application.port.in.RiskRuleUseCase;
import org.v31bank.risk.domain.model.RiskRule;
import org.v31bank.risk.presentation.dto.RiskRuleRequest;
import org.v31bank.risk.presentation.dto.RiskRuleResponse;

/**
 * REST endpoints for managing risk rules.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(RiskRuleController.PATH)
public class RiskRuleController {

	static final String PATH = "/api/v1/risk-rules";

	private final RiskRuleUseCase riskRuleInputPort;

	public RiskRuleController(RiskRuleUseCase riskRuleInputPort) {
		this.riskRuleInputPort = riskRuleInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<RiskRuleResponse>> create(@Valid @RequestBody RiskRuleRequest request) {
		HttpResponse<RiskRule> result = this.riskRuleInputPort.create(request.code(), request.name(),
				request.severity());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(RiskRuleResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<RiskRuleResponse>> get(@PathVariable UUID id) {
		return this.riskRuleInputPort.get(id)
			.map((riskRule) -> ResponseEntity.ok(HttpResponse.ok(RiskRuleResponse.from(riskRule))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No risk rule exists with id " + id));
	}

	@GetMapping
	public HttpResponse<List<RiskRuleResponse>> page(RiskRulePageQuery query) {
		return this.riskRuleInputPort.page(query)
			.map((records) -> records.stream().map(RiskRuleResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<RiskRuleResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody RiskRuleRequest request) {
		return toResponseEntity(this.riskRuleInputPort.update(id, request.code(), request.name(), request.severity(),
				request.status()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<RiskRuleResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.riskRuleInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<RiskRuleResponse>> toResponseEntity(HttpResponse<RiskRule> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(RiskRuleResponse::from));
	}

	private static int statusOf(HttpResponse<?> result) {
		return result.code();
	}

	private static <T> ResponseEntity<HttpResponse<T>> error(int code, String message) {
		return ResponseEntity.status(code).body(HttpResponse.error(code, message));
	}

}
