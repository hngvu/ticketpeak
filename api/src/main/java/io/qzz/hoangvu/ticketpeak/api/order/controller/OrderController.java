package io.qzz.hoangvu.ticketpeak.api.order.controller;

import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import io.qzz.hoangvu.ticketpeak.api.order.dto.OrderResponse;
import io.qzz.hoangvu.ticketpeak.api.order.service.OrderService;
import io.qzz.hoangvu.ticketpeak.api.security.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> listOrders(
            @AuthenticationPrincipal AuthenticatedAccount account,
            Pageable pageable) {
        Page<OrderResponse> orders = orderService.listOrders(account.accountId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable UUID id) {
        OrderResponse order = orderService.getOrder(account.accountId(), id);
        return ResponseEntity.ok(ApiResponse.success(order, "OK"));
    }
}
