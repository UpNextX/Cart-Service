package org.upnext.cartservice.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upnext.cartservice.Models.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserId(Long userId);



}
