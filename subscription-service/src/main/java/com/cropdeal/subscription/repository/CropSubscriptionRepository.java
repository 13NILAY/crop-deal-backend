package com.cropdeal.subscription.repository;

import com.cropdeal.subscription.entity.CropSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CropSubscriptionRepository extends JpaRepository<CropSubscription, Long>, JpaSpecificationExecutor<CropSubscription> {
    List<CropSubscription> findByFarmerId(Long farmerId);
    List<CropSubscription> findByDealerEmailOrderByCreatedAtDesc(String dealerEmail);
    Optional<CropSubscription> findByDealerEmailAndFarmerId(String dealerEmail, Long farmerId);
}
