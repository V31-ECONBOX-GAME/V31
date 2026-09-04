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

package org.v31bank.customer.presentation.controller.v1;

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
import org.v31bank.customer.application.dto.CustomerCategoryPageQuery;
import org.v31bank.customer.application.port.in.CustomerCategoryUseCase;
import org.v31bank.customer.domain.constant.CustomerCategoryStatus;
import org.v31bank.customer.domain.model.CustomerCategory;
import org.v31bank.customer.presentation.dto.CustomerCategoryRequest;
import org.v31bank.customer.presentation.dto.CustomerCategoryResponse;

/**
 * REST endpoints for managing the customer category hierarchy.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(CustomerCategoryController.PATH)
public class CustomerCategoryController {

	static final String PATH = "/api/v1/customer-categories";

	private final CustomerCategoryUseCase customerCategoryInputPort;

	public CustomerCategoryController(CustomerCategoryUseCase customerCategoryInputPort) {
		this.customerCategoryInputPort = customerCategoryInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<CustomerCategoryResponse>> create(
			@Valid @RequestBody CustomerCategoryRequest request) {
		HttpResponse<CustomerCategory> result = this.customerCategoryInputPort.create(request.code(), request.name(),
				request.parentId(), request.sortOrder(), request.status());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(CustomerCategoryResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<CustomerCategoryResponse>> get(@PathVariable UUID id) {
		return this.customerCategoryInputPort.get(id)
			.map((category) -> ResponseEntity.ok(HttpResponse.ok(CustomerCategoryResponse.from(category))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No customer category exists with id " + id));
	}

	@GetMapping
	public HttpResponse<List<CustomerCategoryResponse>> page(CustomerCategoryPageQuery query) {
		return this.customerCategoryInputPort.page(query)
			.map((records) -> records.stream().map(CustomerCategoryResponse::from).toList());
	}

	@GetMapping("/tree")
	public HttpResponse<List<CustomerCategoryResponse>> tree(@RequestParam(required = false) UUID rootId,
			@RequestParam(required = false) CustomerCategoryStatus status) {
		return HttpResponse.ok(this.customerCategoryInputPort.tree(rootId, status)
			.stream()
			.map(CustomerCategoryResponse::fromTree)
			.toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<CustomerCategoryResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody CustomerCategoryRequest request) {
		return toResponseEntity(this.customerCategoryInputPort.update(id, request.code(), request.name(),
				request.parentId(), request.sortOrder(), request.status()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<CustomerCategoryResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.customerCategoryInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<CustomerCategoryResponse>> toResponseEntity(
			HttpResponse<CustomerCategory> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(CustomerCategoryResponse::from));
	}

	private static int statusOf(HttpResponse<?> result) {
		return result.code();
	}

	private static <T> ResponseEntity<HttpResponse<T>> error(int code, String message) {
		return ResponseEntity.status(code).body(HttpResponse.error(code, message));
	}

}
