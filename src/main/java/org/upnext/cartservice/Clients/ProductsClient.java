package org.upnext.cartservice.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.upnext.sharedlibrary.Dtos.ProductDto;

@FeignClient(name = "product-service", url = "${api.gateway.url}/product-service")
@Profile("!dev")
public interface ProductsClient {
    @GetMapping("/product/{id}")
    ProductDto getProduct(@PathVariable Long id);
}
