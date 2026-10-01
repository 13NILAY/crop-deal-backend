package com.cropdeal.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.cropdeal.subscription.entity.CropSubscription;
import com.cropdeal.subscription.repository.CropSubscriptionRepository;
import com.cropdeal.subscription.service.SubscriptionService;
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
class AdminSubscriptionReadTest {
    @Mock CropSubscriptionRepository repository;

    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(repository);
    }

    @Test
    void filtersSubscriptionsByFarmerAndDealerEmail() {
        CropSubscription subscription = new CropSubscription();
        subscription.setFarmerId(33L);
        subscription.setDealerEmail("dealer@example.com");
        subscription.setCreatedAt(LocalDateTime.now());
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(subscription)));

        var result = service.adminSearch(33L, "dealer@example.com", "ACTIVE",
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertEquals(1, result.getTotalElements());
        assertEquals(33L, result.getContent().get(0).getFarmerId());
        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void rejectsUnsupportedSubscriptionStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> service.adminSearch(null, null, "INACTIVE", PageRequest.of(0, 20)));
    }

    @Test
    void returnsExistingSubscriptionInsteadOfCreatingDuplicate() {
        CropSubscription existing = new CropSubscription();
        existing.setFarmerId(33L);
        existing.setDealerEmail("dealer@example.com");
        when(repository.findByDealerEmailAndFarmerId("dealer@example.com", 33L))
                .thenReturn(Optional.of(existing));

        assertEquals(existing, service.subscribe("dealer@example.com", 33L));
        verify(repository, never()).save(any(CropSubscription.class));
    }

    @Test
    void createsSubscriptionWhenNoExistingRecordExists() {
        when(repository.findByDealerEmailAndFarmerId("dealer@example.com", 34L))
                .thenReturn(Optional.empty());
        when(repository.save(any(CropSubscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CropSubscription saved = service.subscribe("dealer@example.com", 34L);

        assertEquals("dealer@example.com", saved.getDealerEmail());
        assertEquals(34L, saved.getFarmerId());
        verify(repository).save(any(CropSubscription.class));
    }

    @Test
    void unsubscribeDeletesOnlyMatchingExistingRecord() {
        CropSubscription existing = new CropSubscription();
        when(repository.findByDealerEmailAndFarmerId("dealer@example.com", 33L))
                .thenReturn(Optional.of(existing));

        service.unsubscribe("dealer@example.com", 33L);

        verify(repository).delete(existing);
    }
}
