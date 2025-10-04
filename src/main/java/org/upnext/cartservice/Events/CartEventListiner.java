package org.upnext.cartservice.Events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.upnext.cartservice.Repositories.CartRepository;
import org.upnext.sharedlibrary.Events.OrderCreatedEvent;

//@Service
public class CartEventListiner {
    private final CartRepository cartRepository;
    public CartEventListiner(CartRepository cartRepository){
        this.cartRepository = cartRepository;
    }

    @KafkaListener(topics = "order-placed", groupId = "cart-service")
    public void handleOrderPlaced(OrderCreatedEvent orderCreatedEvent){
        Long cartId = orderCreatedEvent.getCartId();
        cartRepository.findById(cartId)
                .ifPresent(cart ->{
                    cart.getItems().clear();
                    cartRepository.save(cart);
                    System.out.println("Cart "+ cartId + " Cleared");
                });
    }
}
