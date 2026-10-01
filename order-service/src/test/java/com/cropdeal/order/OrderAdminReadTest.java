package com.cropdeal.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cropdeal.order.client.CropClient;
import com.cropdeal.order.client.FarmerProfileClient;
import com.cropdeal.order.entity.CustomerOrder;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.service.OrderEventPublisher;
import com.cropdeal.order.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OrderAdminReadTest {
    @Mock OrderRepository orderRepository;
    @Mock CropClient cropClient;
    @Mock FarmerProfileClient farmerProfileClient;
    @Mock OrderEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cropClient, farmerProfileClient, eventPublisher);
    }

    @Test
    void adminSearchReturnsReadOnlyOrderResponsePage() {
        CustomerOrder order = new CustomerOrder();
        order.setCropId(12L);
        order.setFarmerId(7L);
        order.setDealerEmail("dealer@example.com");
        order.setQuantity(new BigDecimal("2.000"));
        order.setUnitPrice(new BigDecimal("10.00"));
        order.setTotalPrice(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        var result = orderService.adminSearch(OrderStatus.CREATED, "dealer@example.com", 7L,
                "dealer@example.com", PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatus.CREATED, result.getContent().get(0).status());
        assertEquals("dealer@example.com", result.getContent().get(0).dealerEmail());
        verify(orderRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void readsOrderById() {
        CustomerOrder order = order(41L, OrderStatus.CREATED);
        when(orderRepository.findById(41L)).thenReturn(Optional.of(order));

        var response = orderService.get(41L);

        assertEquals(41L, response.cropId());
        assertEquals(41L, response.cropId());
    }

    @Test
    void returnsDealerOrders() {
        when(orderRepository.findByDealerEmail("dealer@example.com"))
                .thenReturn(List.of(order(42L, OrderStatus.CREATED)));

        var orders = orderService.byDealer("dealer@example.com");

        assertEquals(1, orders.size());
        assertEquals("dealer@example.com", orders.get(0).dealerEmail());
    }

    @Test
    void returnsFarmerOrders() {
        when(orderRepository.findByFarmerId(7L)).thenReturn(List.of(order(43L, OrderStatus.DELIVERED)));

        var orders = orderService.byFarmer(7L);

        assertEquals(OrderStatus.DELIVERED, orders.get(0).status());
        verify(orderRepository).findByFarmerId(7L);
    }

    @Test
    void returnsNotFoundForUnknownOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.cropdeal.order.exception.OrderNotFoundException.class,
                () -> orderService.get(999L));
        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }

    private CustomerOrder order(Long cropId, OrderStatus status) {
        CustomerOrder order = new CustomerOrder();
        order.setCropId(cropId);
        order.setFarmerId(7L);
        order.setDealerEmail("dealer@example.com");
        order.setQuantity(new BigDecimal("2.000"));
        order.setUnitPrice(new BigDecimal("10.00"));
        order.setTotalPrice(new BigDecimal("20.00"));
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}