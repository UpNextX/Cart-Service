package org.upnext.cartservice.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;
import org.upnext.cartservice.Clients.ProductsClient;
import org.upnext.cartservice.Mapppers.CartMapper;
import org.upnext.cartservice.Models.Cart;
import org.upnext.cartservice.Models.CartItem;
import org.upnext.cartservice.Repositories.CartRepository;
import org.upnext.cartservice.Services.Implementation.CartServiceImpl;
import org.upnext.sharedlibrary.Dtos.CartDto;
import org.upnext.sharedlibrary.Dtos.ProductDto;
import org.upnext.cartservice.Dtos.CartItemRequest;
import org.upnext.sharedlibrary.Errors.Result;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductsClient productsClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private CartDto testCartDto;
    private ProductDto testProductDto;
    private CartItemRequest testCartItemRequest;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(100L);
        testCart.setItems(new ArrayList<>());

        testCartDto = new CartDto();
        testCartDto.setId(1L);
        testCartDto.setUserId(100L);

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setPrice(29.99);

        testCartItemRequest = new CartItemRequest();
        testCartItemRequest.setProductId(1L);
        testCartItemRequest.setQuantity(2);
    }

    @Test
    void getAllCarts_ShouldReturnAllCarts() {
        List<Cart> carts = List.of(testCart);
        when(cartRepository.findAll()).thenReturn(carts);
        when(cartMapper.toCartDto(any(Cart.class))).thenReturn(testCartDto);

        Result<List<CartDto>> result = cartService.getAllCarts();

        assertTrue(result.isSuccess());
        assertEquals(1, result.getValue().size());
        verify(cartRepository).findAll();
        verify(cartMapper).toCartDto(testCart);
    }

    @Test
    void getCartById_ShouldReturnCart_WhenCartExists() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartMapper.toCartDto(testCart)).thenReturn(testCartDto);

        Result<CartDto> result = cartService.getCartById(1L);

        assertTrue(result.isSuccess());
        assertEquals(testCartDto, result.getValue());
        verify(cartRepository).findById(1L);
    }

    @Test
    void getCartById_ShouldReturnFailure_WhenCartDoesNotExist() {
        when(cartRepository.findById(999L)).thenReturn(Optional.empty());

        Result<CartDto> result = cartService.getCartById(999L);

        assertTrue(result.getIsFailure());
        verify(cartRepository).findById(999L);
        verify(cartMapper, never()).toCartDto(any());
    }
    @Test
    void getCartByUserId_ShouldReturnExistingCart() {
        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(cartMapper.toCartDto(testCart)).thenReturn(testCartDto);

        Result<CartDto> result = cartService.getCartByUserId(100L);

        assertTrue(result.isSuccess());
        assertEquals(testCartDto, result.getValue());
        verify(cartRepository).findByUserId(100L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getCartByUserId_ShouldCreateNewCart_WhenCartDoesNotExist() {
        when(cartRepository.findByUserId(200L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toCartDto(any(Cart.class))).thenReturn(testCartDto);

        Result<CartDto> result = cartService.getCartByUserId(200L);

        assertTrue(result.isSuccess());
        verify(cartRepository).findByUserId(200L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ShouldAddNewItem_WhenItemDoesNotExist() {
        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(productsClient.getProduct(1L)).thenReturn(testProductDto);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        UriComponentsBuilder urb = UriComponentsBuilder.newInstance();

        Result<URI> result = cartService.addItemToCart(100L, testCartItemRequest, urb);

        assertTrue(result.isSuccess());
        assertEquals(1, testCart.getItems().size());
        verify(cartRepository).save(testCart);
        verify(productsClient).getProduct(1L);
    }

    @Test
    void addItemToCart_ShouldReturnFailure_WhenItemAlreadyExists() {
        CartItem existingItem = new CartItem();
        existingItem.setProductId(1L);
        existingItem.setQuantity(1);
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(productsClient.getProduct(1L)).thenReturn(testProductDto);
        UriComponentsBuilder urb = UriComponentsBuilder.newInstance();

        Result<URI> result = cartService.addItemToCart(100L, testCartItemRequest, urb);


        assertTrue(result.getIsFailure());
        assertEquals("ALREADY.EXISTS", result.getError().getCode());
        verify(cartRepository, never()).save(any());
    }
    @Test
    void updateItemCart_ShouldUpdateQuantity_WhenItemExists() {
        CartItem existingItem = new CartItem();
        existingItem.setProductId(1L);
        existingItem.setQuantity(1);
        testCart.getItems().add(existingItem);

        testCartItemRequest.setQuantity(5);

        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(productsClient.getProduct(1L)).thenReturn(testProductDto);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Result<Void> result = cartService.updateItemCart(100L, testCartItemRequest);

        assertTrue(result.isSuccess());
        assertEquals(5, existingItem.getQuantity());
        verify(cartRepository).save(testCart);
    }

    @Test
    void updateItemCart_ShouldReturnFailure_WhenItemDoesNotExist() {
        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(productsClient.getProduct(1L)).thenReturn(testProductDto);

        Result<Void> result = cartService.updateItemCart(100L, testCartItemRequest);

        assertTrue(result.getIsFailure());
        assertEquals("Product_NOT_FOUND", result.getError().getCode());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteItemFromCart_ShouldRemoveItem_WhenItemExists() {
        CartItem existingItem = new CartItem();
        existingItem.setProductId(1L);
        existingItem.setQuantity(2);
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Result<Void> result = cartService.deleteItemFromCart(100L, 1L);

        assertTrue(result.isSuccess());
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository).save(testCart);
    }

    @Test
    void deleteItemFromCart_ShouldReturnFailure_WhenItemDoesNotExist() {
        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));

        Result<Void> result = cartService.deleteItemFromCart(100L, 999L);

        assertTrue(result.getIsFailure());
        assertEquals("Product_NOT_FOUND", result.getError().getCode());
        verify(cartRepository, never()).save(any());
    }
    @Test
    void clearCart_ShouldRemoveAllItems() {
        CartItem item1 = new CartItem();
        item1.setProductId(1L);
        CartItem item2 = new CartItem();
        item2.setProductId(2L);
        testCart.getItems().add(item1);
        testCart.getItems().add(item2);

        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Result<Void> result = cartService.clearCart(100L);

        assertTrue(result.isSuccess());
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository).save(testCart);
    }

    @Test
    void clearCart_ShouldSucceed_WhenCartIsAlreadyEmpty() {
        when(cartRepository.findByUserId(100L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Result<Void> result = cartService.clearCart(100L);

        assertTrue(result.isSuccess());
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository).save(testCart);
    }

    @Test
    void getTotalCost_ShouldReturnZero_WhenCartIsEmpty() {
        Double totalCost = cartService.getTotalCost(testCart);

        assertEquals(0.0, totalCost);
        verify(productsClient, never()).getProduct(anyLong());
    }

    @Test
    void getCartObjectByUserId_ShouldCreateCart_WhenNotFound() {
        Cart newCart = new Cart();
        newCart.setUserId(200L);
        newCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(200L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        Cart result = cartService.getCartObjectByUserId(200L);

        assertNotNull(result);
        assertEquals(200L, result.getUserId());
        verify(cartRepository).save(any(Cart.class));
    }
}
