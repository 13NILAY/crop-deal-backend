package com.cropdeal.profile.repository;

import com.cropdeal.profile.entity.DealerProfile;
import com.cropdeal.profile.entity.VerificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealerProfileRepository extends JpaRepository<DealerProfile, Long> {
    Optional<DealerProfile> findByUserEmail(String userEmail);
    List<DealerProfile> findByVerificationStatus(VerificationStatus status);
}
