package com.josefuentes4096.products.service;

import com.josefuentes4096.products.entity.Setting;
import com.josefuentes4096.products.repository.SettingRepository;
import com.josefuentes4096.products.service.impl.SettingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingServiceTest {

    @Mock
    private SettingRepository settingRepository;

    private SettingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SettingServiceImpl(settingRepository);
    }

    // -------------------------------------------------------------------------
    // getMinimumStock
    // -------------------------------------------------------------------------

    @Test
    void getMinimumStock_retornaValorNumericoAlmacenadoEnLaBD() {
        when(settingRepository.findByKey("minimum_stock"))
                .thenReturn(Optional.of(new Setting(1, "minimum_stock", "10")));

        assertThat(service.getMinimumStock()).isEqualTo(10);
    }

    @Test
    void getMinimumStock_retornaFallbackCuandoElValorNoEsNumerico() {
        when(settingRepository.findByKey("minimum_stock"))
                .thenReturn(Optional.of(new Setting(1, "minimum_stock", "no-es-numero")));

        assertThat(service.getMinimumStock()).isEqualTo(5);
    }

    @Test
    void getMinimumStock_retornaFallbackCuandoLaClaveNoExisteEnLaBD() {
        when(settingRepository.findByKey("minimum_stock")).thenReturn(Optional.empty());

        assertThat(service.getMinimumStock()).isEqualTo(5);
    }
}
