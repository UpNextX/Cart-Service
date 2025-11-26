package org.upnext.cartservice.Repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.upnext.cartservice.Models.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserId(Long userId);



}
