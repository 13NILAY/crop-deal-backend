package com.cropdeal.order.service;

import com.cropdeal.order.dto.CreateOrderRequest;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.entity.CustomerOrder;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.client.CropClient;
import com.cropdeal.order.client.FarmerProfileClient;
import com.cropdeal.order.exception.OrderNotFoundException;
import com.cropdeal.order.exception.CropUnavailableException;
import com.cropdeal.order.dto.CropResponse;
import com.cropdeal.order.event.OrderCreatedEvent;
import com.cropdeal.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CropClient cropClient;
    private final FarmerProfileClient farmerProfileClient;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, CropClient cropClient, FarmerProfileClient farmerProfileClient,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.cropClient = cropClient;
        this.farmerProfileClient = farmerProfileClient;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse create(String dealerEmail, String authorization, CreateOrderRequest request) {
        CropResponse crop;
        try {
            crop = cropClient.reserve(request.cropId(), request.farmerId(), request.quantity(), authorization);
        } catch (FeignException ex) {
            throw new CropUnavailableException("Crop Service could not reserve crop " + request.cropId());
        }
        if (crop == null) {
            throw new CropUnavailableException("Crop is unavailable or has insufficient quantity");
        }
        CustomerOrder order = new CustomerOrder();
        order.setCropId(request.cropId());
        order.setFarmerId(request.farmerId());
        order.setDealerEmail(dealerEmail);
        order.setQuantity(request.quantity());
        order.setUnitPrice(crop.price());
        order.setTotalPrice(request.quantity().multiply(crop.price()));
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        CustomerOrder saved = orderRepository.save(order);
        eventPublisher.publish(new OrderCreatedEvent(saved.getId(), saved.getCropId(), saved.getFarmerId(),
            saved.getDealerEmail(), saved.getQuantity(), saved.getTotalPrice()));
        return toResponse(saved);
    }

    public OrderResponse get(Long id) { return toResponse(find(id)); }

    public OrderResponse getForDealer(Long id, String dealerEmail) {
        CustomerOrder order = find(id);
        verifyDealerOwner(order, dealerEmail);
        return toResponse(order);
    }
    public Page<OrderResponse> adminSearch(OrderStatus status, String search, Long farmerId,
                                           String dealerEmail, Pageable pageable) {
        Specification<CustomerOrder> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (status != null) predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (farmerId != null) predicates.add(criteriaBuilder.equal(root.get("farmerId"), farmerId));
            if (dealerEmail != null && !dealerEmail.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("dealerEmail")),
                        "%" + dealerEmail.trim().toLowerCase(java.util.Locale.ROOT) + "%"));
            }
            if (search != null && !search.isBlank()) {
                String normalizedSearch = search.trim().toLowerCase(java.util.Locale.ROOT);
                var searchPredicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
                searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("dealerEmail")),
                        "%" + normalizedSearch + "%"));
                try {
                    Long orderId = Long.valueOf(normalizedSearch);
                    searchPredicates.add(criteriaBuilder.equal(root.get("id"), orderId));
                } catch (NumberFormatException ignored) {
                    // Non-numeric searches match dealer email only.
                }
                predicates.add(criteriaBuilder.or(searchPredicates.toArray(jakarta.persistence.criteria.Predicate[]::new)));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Pageable boundedPageable = PageRequest.of(Math.max(0, pageable.getPageNumber()),
                Math.max(1, Math.min(pageable.getPageSize(), 100)), pageable.getSort());
        return orderRepository.findAll(specification, boundedPageable).map(this::toResponse);
    }
    public List<OrderResponse> byDealer(String email) {
        return orderRepository.findByDealerEmail(email).stream().map(this::toResponse).toList();
    }
    public List<OrderResponse> byFarmer(Long farmerId) {
        return orderRepository.findByFarmerId(farmerId).stream().map(this::toResponse).toList();
    }
    public List<OrderResponse> byFarmerForUser(Long farmerId, String authorization) {
        Long authenticatedFarmerId = farmerProfileClient.currentFarmer(authorization).id();
        if (!farmerId.equals(authenticatedFarmerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Farmer orders belong to another user");
        }
        return byFarmer(farmerId);
    }

    public OrderResponse updateStatus(Long id, OrderStatus status, String dealerEmail) {
        CustomerOrder order = find(id);
        verifyDealerOwner(order, dealerEmail);
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updateStatus(Long id, OrderStatus status) {
        CustomerOrder order = find(id);
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    private void verifyDealerOwner(CustomerOrder order, String dealerEmail) {
        if (!order.getDealerEmail().equalsIgnoreCase(dealerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Order belongs to another dealer");
        }
    }
    private CustomerOrder find(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getCropId(), order.getFarmerId(), order.getDealerEmail(),
                order.getQuantity(), order.getUnitPrice(), order.getTotalPrice(), order.getStatus(), order.getCreatedAt());
    }
}