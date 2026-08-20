package com.bloodbuddy.services.Dashboard;

import com.bloodbuddy.dto.Dashboard.BloodGroupResponse;
import com.bloodbuddy.dto.Dashboard.DashboardResponse;
import com.bloodbuddy.entity.Dashboard.BloodAvailability;
import com.bloodbuddy.repository.Dashboard.BloodAvailabilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloodAvailabilityService {

    private final BloodAvailabilityRepository repository;

    // Units <= 3 are considered low stock
    private static final int LOW_STOCK_LIMIT = 3;

    public BloodAvailabilityService(
            BloodAvailabilityRepository repository) {
        this.repository = repository;
    }

    public DashboardResponse getDashboard() {

        List<BloodAvailability> records =
                repository.findAll();

        long bloodGroupsListed = records.size();

        int totalUnits = records.stream()
                .mapToInt(BloodAvailability::getUnits)
                .sum();

        long lowStockAlerts = records.stream()
                .filter(b -> b.getUnits() <= LOW_STOCK_LIMIT)
                .count();

        List<BloodGroupResponse> availability =
                records.stream()
                        .map(b -> new BloodGroupResponse(
                                b.getBloodGroup(),
                                b.getUnits()))
                        .toList();

        return new DashboardResponse(
                bloodGroupsListed,
                totalUnits,
                lowStockAlerts,
                availability
        );
    }

    public List<BloodAvailability> getAll() {
        return repository.findAll();
    }

    public BloodAvailability add(BloodAvailability blood) {

        if (blood.getUnits() < 0) {
            throw new IllegalArgumentException(
                    "Units cannot be negative");
        }

        return repository.save(blood);
    }

    public BloodAvailability update(
            Long id,
            BloodAvailability request) {

        BloodAvailability existing =
                repository.findById(Math.toIntExact(id))
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Blood group not found"));

        existing.setBloodGroup(request.getBloodGroup());
        existing.setUnits(request.getUnits());

        return repository.save(existing);
    }

    public void delete(Long id) {

        if (!repository.existsById(Math.toIntExact(id))) {
            throw new RuntimeException(
                    "Blood group not found");
        }

        repository.deleteById(Math.toIntExact(id));
    }
}
