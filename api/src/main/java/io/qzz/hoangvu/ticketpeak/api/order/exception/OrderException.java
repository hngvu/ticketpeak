package io.qzz.hoangvu.ticketpeak.api.order.exception;

import io.qzz.hoangvu.ticketpeak.api.common.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public final class OrderException {

    private OrderException() {}

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found");
    }

    public static ApiException ownerMismatch() {
        return new ApiException(HttpStatus.FORBIDDEN, "ORDER_OWNER_MISMATCH", "You do not own this order");
    }


    /**
     * Exception to indicate that confirming the inventory holds for an order failed.
     * Note: This exception is thrown during asynchronous event processing to trigger
     * transaction rollbacks and background audit trailing; it is not directly exposed
     * to buyer REST clients.
     */
    public static ApiException inventoryConfirmFailed(String detail) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "ORDER_INVENTORY_CONFIRM_FAILED", detail);
    }
}
