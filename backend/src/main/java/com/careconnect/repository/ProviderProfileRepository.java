package com.careconnect.repository;

import com.careconnect.model.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    List<ProviderProfile> findByActiveTrueOrderByDepartmentAscSpecializationAsc();
    Optional<ProviderProfile> findByUserId(Long userId);
}
