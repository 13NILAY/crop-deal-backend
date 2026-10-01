package com.cropdeal.profile.repository;

import com.cropdeal.profile.entity.FarmerProfile;
import com.cropdeal.profile.entity.VerificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Long> {
	Optional<FarmerProfile> findByUserEmail(String userEmail);
	List<FarmerProfile> findByVerificationStatus(VerificationStatus status);
}
