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
import org.springframework.web.bind.annotation.RestController;

import org.v31bank.core.HttpResponse;
import org.v31bank.notification.application.dto.NotificationTemplatePageQuery;
import org.v31bank.notification.application.port.in.NotificationTemplateUseCase;
import org.v31bank.notification.domain.model.NotificationTemplate;
import org.v31bank.notification.presentation.dto.NotificationTemplateRequest;
import org.v31bank.notification.presentation.dto.NotificationTemplateResponse;

/**
 * REST endpoints for managing notification templates.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@RestController
@RequestMapping(NotificationTemplateController.PATH)
public class NotificationTemplateController {

	static final String PATH = "/api/v1/notification-templates";

	private final NotificationTemplateUseCase notificationTemplateInputPort;

	public NotificationTemplateController(NotificationTemplateUseCase notificationTemplateInputPort) {
		this.notificationTemplateInputPort = notificationTemplateInputPort;
	}

	@PostMapping
	public ResponseEntity<HttpResponse<NotificationTemplateResponse>> create(
			@Valid @RequestBody NotificationTemplateRequest request) {
		HttpResponse<NotificationTemplate> result = this.notificationTemplateInputPort.create(request.code(),
				request.name(), request.channel());
		if (!result.succeeded()) {
			return toResponseEntity(result);
		}
		return ResponseEntity.created(URI.create(PATH + "/" + result.data().getId()))
			.body(result.map(NotificationTemplateResponse::from));
	}

	@GetMapping("/{id}")
	public ResponseEntity<HttpResponse<NotificationTemplateResponse>> get(@PathVariable UUID id) {
		return this.notificationTemplateInputPort.get(id)
			.map((notificationTemplate) -> ResponseEntity
				.ok(HttpResponse.ok(NotificationTemplateResponse.from(notificationTemplate))))
			.orElseGet(() -> error(HttpStatus.NOT_FOUND.value(), "No notification template exists with id " + id));
	}

	@GetMapping
	public HttpResponse<List<NotificationTemplateResponse>> page(NotificationTemplatePageQuery query) {
		return this.notificationTemplateInputPort.page(query)
			.map((records) -> records.stream().map(NotificationTemplateResponse::from).toList());
	}

	@PutMapping("/{id}")
	public ResponseEntity<HttpResponse<NotificationTemplateResponse>> update(@PathVariable UUID id,
			@Valid @RequestBody NotificationTemplateRequest request) {
		return toResponseEntity(this.notificationTemplateInputPort.update(id, request.code(), request.name(),
				request.channel(), request.status()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<HttpResponse<NotificationTemplateResponse>> delete(@PathVariable UUID id) {
		return toResponseEntity(this.notificationTemplateInputPort.delete(id));
	}

	private static ResponseEntity<HttpResponse<NotificationTemplateResponse>> toResponseEntity(
			HttpResponse<NotificationTemplate> result) {
		return ResponseEntity.status(statusOf(result)).body(result.map(NotificationTemplateResponse::from));
	}

	private static int statusOf(HttpResponse<?> result) {
		return result.code();
	}

	private static <T> ResponseEntity<HttpResponse<T>> error(int code, String message) {
		return ResponseEntity.status(code).body(HttpResponse.error(code, message));
	}

}
