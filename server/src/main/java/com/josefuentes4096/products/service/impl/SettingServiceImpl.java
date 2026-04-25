package com.josefuentes4096.products.service.impl;

import com.josefuentes4096.products.repository.SettingRepository;
import com.josefuentes4096.products.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private static final String MINIMUM_STOCK_KEY = "minimum_stock";
    private static final int MINIMUM_STOCK_FALLBACK = 5;

    private final SettingRepository settingRepository;

    @Override
    @Cacheable(SettingService.MINIMUM_STOCK_CACHE)
    @Transactional(readOnly = true)
    public int getMinimumStock() {
        return settingRepository.findByKey(MINIMUM_STOCK_KEY)
                .map(s -> {
                    try {
                        return Integer.parseInt(s.getValue());
                    } catch (NumberFormatException e) {
                        log.warn("Valor inválido para '{}': '{}'. Usando fallback {}",
                                MINIMUM_STOCK_KEY, s.getValue(), MINIMUM_STOCK_FALLBACK);
                        return MINIMUM_STOCK_FALLBACK;
                    }
                })
                .orElseGet(() -> {
                    log.warn("Configuración '{}' no encontrada. Usando fallback {}",
                            MINIMUM_STOCK_KEY, MINIMUM_STOCK_FALLBACK);
                    return MINIMUM_STOCK_FALLBACK;
                });
    }
}
