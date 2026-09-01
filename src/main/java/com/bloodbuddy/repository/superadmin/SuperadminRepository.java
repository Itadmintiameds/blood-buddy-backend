package com.bloodbuddy.repository.superadmin;

import com.bloodbuddy.entity.Superadmin.Superadmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperadminRepository extends JpaRepository<Superadmin,Long> {

}
