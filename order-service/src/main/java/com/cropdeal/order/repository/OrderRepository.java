package com.cropdeal.order.repository;

import com.cropdeal.order.entity.CustomerOrder;
import com.cropdeal.order.entity.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long>, JpaSpecificationExecutor<CustomerOrder> {
    List<CustomerOrder> findByDealerEmail(String dealerEmail);
    List<CustomerOrder> findByFarmerId(Long farmerId);

    long countByStatus(OrderStatus status);
}