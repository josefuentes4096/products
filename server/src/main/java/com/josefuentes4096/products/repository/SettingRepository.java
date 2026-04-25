package com.josefuentes4096.products.repository;

import com.josefuentes4096.products.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Integer> {
    Optional<Setting> findByKey(String key);
}
