package com.cropdeal.subscription.service;

import com.cropdeal.subscription.entity.CropSubscription;
import com.cropdeal.subscription.repository.CropSubscriptionRepository;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {
    private final CropSubscriptionRepository repository;

    public SubscriptionService(CropSubscriptionRepository repository) {
        this.repository = repository;
    }

    public CropSubscription subscribe(String dealerEmail, Long farmerId) {
        return repository.findByDealerEmailAndFarmerId(dealerEmail, farmerId)
                .orElseGet(() -> {
                    CropSubscription subscription = new CropSubscription();
                    subscription.setDealerEmail(dealerEmail);
                    subscription.setFarmerId(farmerId);
                    subscription.setCreatedAt(LocalDateTime.now());
                    return repository.save(subscription);
                });
    }

    public void unsubscribe(String dealerEmail, Long farmerId) {
        repository.findByDealerEmailAndFarmerId(dealerEmail, farmerId)
                .ifPresent(repository::delete);
    }

    public List<CropSubscription> mine(String dealerEmail) {
        return repository.findByDealerEmailOrderByCreatedAtDesc(dealerEmail);
    }

    public List<String> dealerEmails(Long farmerId) {
        return repository.findByFarmerId(farmerId).stream()
                .map(CropSubscription::getDealerEmail)
                .distinct()
                .toList();
    }

    public Page<CropSubscription> adminSearch(Long farmerId, String dealerEmail, String status,
                                               Pageable pageable) {
        if (status != null && !status.isBlank() && !"ACTIVE".equals(status.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only ACTIVE subscriptions exist in the current model");
        }
        Specification<CropSubscription> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (farmerId != null) predicates.add(criteriaBuilder.equal(root.get("farmerId"), farmerId));
            if (dealerEmail != null && !dealerEmail.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("dealerEmail")),
                        "%" + dealerEmail.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Pageable boundedPageable = PageRequest.of(Math.max(0, pageable.getPageNumber()),
                Math.max(1, Math.min(pageable.getPageSize(), 100)), pageable.getSort());
        return repository.findAll(specification, boundedPageable);
    }

    public CropSubscription adminGet(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Subscription not found"));
    }
}
