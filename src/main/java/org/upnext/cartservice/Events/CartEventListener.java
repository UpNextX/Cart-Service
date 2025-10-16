package org.upnext.cartservice.Events;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.upnext.cartservice.Services.CartService;
import org.upnext.sharedlibrary.Dtos.SuccessfulPaymentEvent;

import static org.upnext.cartservice.Configurations.RabbitMqConfig.CART_CLEAR_QUEUE;

@Service
@RequiredArgsConstructor
public class CartEventListener {
    private final CartService cartService;
    @RabbitListener(queues = CART_CLEAR_QUEUE)
    public void handleClearCart(SuccessfulPaymentEvent successfulPaymentEvent) {
        System.out.println("Clearing cart...");
        System.out.println(successfulPaymentEvent);
        Long userId = successfulPaymentEvent.getUserId();
        cartService.clearCart(userId);
    }
}
