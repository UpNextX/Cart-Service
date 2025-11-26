package org.upnext.cartservice.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.upnext.sharedlibrary.Dtos.ProductDto;

@FeignClient(name = "localhost:8081/")
public interface ProductsClient {
    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
}
