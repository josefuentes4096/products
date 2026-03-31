package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.entity.Setting;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.mapper.ProductMapper;
import com.josefuentes4096.products.repository.ProductRepository;
import com.josefuentes4096.products.repository.SettingRepository;
import com.josefuentes4096.products.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private SettingRepository settingRepository;

    @Spy
    private ProductMapper mapper = new ProductMapper();

    private ProductServiceImpl service;

    private Product stratocaster;
    private Product tubeScreamer;
    private Product marshallDsl40;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository, mapper, settingRepository);

        stratocaster = new Product(1, "Fender Stratocaster American Pro II",
                "Guitarra eléctrica de cuerpo sólido con pastillas V-Mod II",
                BigDecimal.valueOf(1500.00), "Guitarras", "fender_strat.jpg", 8, null, null);

        tubeScreamer = new Product(2, "Ibanez Tube Screamer TS9",
                "Pedal de overdrive clásico, el favorito de Stevie Ray Vaughan",
                BigDecimal.valueOf(120.00), "Pedales", "ts9.jpg", 15, null, null);

        marshallDsl40 = new Product(3, "Marshall DSL40CR",
                "Amplificador valvular de 40W con dos canales y reverb",
                BigDecimal.valueOf(1200.00), "Amplificadores", "marshall_dsl40.jpg", 3, null, null);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_retornaTodosLosInstrumentos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(stratocaster, tubeScreamer, marshallDsl40)));

        Page<ProductResponseDTO> resultado = service.findAll(pageable);

        assertThat(resultado.getContent()).hasSize(3);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getName)
                .containsExactly(
                        "Fender Stratocaster American Pro II",
                        "Ibanez Tube Screamer TS9",
                        "Marshall DSL40CR");
    }

    @Test
    void findAll_retornaListaVaciaSiNoHayProductos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(Page.empty());

        assertThat(service.findAll(pageable).getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_retornaGuitarraCuandoExiste() {
        when(repository.findById(1)).thenReturn(Optional.of(stratocaster));

        ProductResponseDTO resultado = service.findById(1);

        assertThat(resultado.getId()).isEqualTo(1);
        assertThat(resultado.getName()).isEqualTo("Fender Stratocaster American Pro II");
        assertThat(resultado.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
        assertThat(resultado.getCategory()).isEqualTo("Guitarras");
    }

    @Test
    void findById_retornaPedalCuandoExiste() {
        when(repository.findById(2)).thenReturn(Optional.of(tubeScreamer));

        ProductResponseDTO resultado = service.findById(2);

        assertThat(resultado.getName()).isEqualTo("Ibanez Tube Screamer TS9");
        assertThat(resultado.getCategory()).isEqualTo("Pedales");
    }

    @Test
    void findById_lanzaExcepcionCuandoInstrumentoNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    void save_creaAmplificadorValvularCorrectamente() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Vox AC30");
        dto.setDescription("Amplificador valvular de 30W, icono del sonido británico");
        dto.setPrice(BigDecimal.valueOf(2200.00));
        dto.setCategory("Amplificadores");
        dto.setImageUrl("vox_ac30.jpg");
        dto.setStock(2);

        Product guardado = new Product(4, "Vox AC30",
                "Amplificador valvular de 30W, icono del sonido británico",
                BigDecimal.valueOf(2200.00), "Amplificadores", "vox_ac30.jpg", 2, null, null);
        when(repository.save(any())).thenReturn(guardado);

        ProductResponseDTO resultado = service.save(dto);

        assertThat(resultado.getName()).isEqualTo("Vox AC30");
        assertThat(resultado.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(2200.00));
        assertThat(resultado.getStock()).isEqualTo(2);
        verify(repository).save(any(Product.class));
    }

    @Test
    void save_creaPedalDeEfectoCorrectamente() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Boss DS-1 Distortion");
        dto.setDescription("Pedal de distorsión compacto, el más vendido en la historia");
        dto.setPrice(BigDecimal.valueOf(80.00));
        dto.setCategory("Pedales");
        dto.setImageUrl("boss_ds1.jpg");
        dto.setStock(20);

        Product guardado = new Product(5, "Boss DS-1 Distortion",
                "Pedal de distorsión compacto, el más vendido en la historia",
                BigDecimal.valueOf(80.00), "Pedales", "boss_ds1.jpg", 20, null, null);
        when(repository.save(any())).thenReturn(guardado);

        ProductResponseDTO resultado = service.save(dto);

        assertThat(resultado.getName()).isEqualTo("Boss DS-1 Distortion");
        assertThat(resultado.getCategory()).isEqualTo("Pedales");
        verify(repository).save(any(Product.class));
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_modificaPrecioYStockDeGuitarra() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Fender Stratocaster American Pro II");
        dto.setDescription("Guitarra eléctrica de cuerpo sólido con pastillas V-Mod II");
        dto.setPrice(BigDecimal.valueOf(1650.00));
        dto.setCategory("Guitarras");
        dto.setImageUrl("fender_strat_v2.jpg");
        dto.setStock(5);

        when(repository.findById(1)).thenReturn(Optional.of(stratocaster));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO resultado = service.update(1, dto);

        assertThat(resultado.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1650.00));
        assertThat(resultado.getStock()).isEqualTo(5);
        assertThat(resultado.getImageUrl()).isEqualTo("fender_strat_v2.jpg");
    }

    @Test
    void update_cambiaDescripcionDeAmplificador() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Marshall DSL40CR");
        dto.setDescription("Amplificador valvular 40W - edición 2024 con nueva reverb digital");
        dto.setPrice(BigDecimal.valueOf(1250.00));
        dto.setCategory("Amplificadores");
        dto.setImageUrl("marshall_dsl40_2024.jpg");
        dto.setStock(3);

        when(repository.findById(3)).thenReturn(Optional.of(marshallDsl40));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO resultado = service.update(3, dto);

        assertThat(resultado.getDescription()).contains("2024");
        assertThat(resultado.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1250.00));
    }

    @Test
    void update_lanzaExcepcionSiInstrumentoNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99, new ProductRequestDTO()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_eliminaPedalExistente() {
        when(repository.findById(2)).thenReturn(Optional.of(tubeScreamer));

        service.delete(2);

        verify(repository).delete(tubeScreamer);
    }

    @Test
    void delete_lanzaExcepcionSiInstrumentoNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(ProductNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // findByName
    // -------------------------------------------------------------------------

    @Test
    void findByName_encuentraAmplificadorPorNombreExacto() {
        when(repository.findByName("Marshall DSL40CR")).thenReturn(Optional.of(marshallDsl40));

        ProductResponseDTO resultado = service.findByName("Marshall DSL40CR");

        assertThat(resultado.getName()).isEqualTo("Marshall DSL40CR");
        assertThat(resultado.getCategory()).isEqualTo("Amplificadores");
    }

    @Test
    void findByName_lanzaExcepcionSiNombreNoExiste() {
        when(repository.findByName("Gibson Les Paul Custom")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByName("Gibson Les Paul Custom"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Gibson Les Paul Custom");
    }

    // -------------------------------------------------------------------------
    // filterByCategory
    // -------------------------------------------------------------------------

    @Test
    void filterByCategory_retornaGuitarrasDeLaCategoria() {
        Pageable pageable = PageRequest.of(0, 10);
        Product lesPaul = new Product(6, "Gibson Les Paul Standard",
                "Guitarra eléctrica con cuerpo de caoba y tapa de arce flameado",
                BigDecimal.valueOf(2800.00), "Guitarras", "gibson_lp.jpg", 2, null, null);

        when(repository.findByCategoryIgnoreCase("guitarras", pageable))
                .thenReturn(new PageImpl<>(List.of(stratocaster, lesPaul)));

        Page<ProductResponseDTO> resultado = service.filterByCategory("guitarras", pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getCategory)
                .containsOnly("Guitarras");
    }

    @Test
    void filterByCategory_retornaPedalesIgnorandoMayusculas() {
        Pageable pageable = PageRequest.of(0, 10);
        Product hallOfFame = new Product(7, "TC Electronic Hall of Fame 2",
                "Pedal de reverb con TonePrint y algoritmos de sala",
                BigDecimal.valueOf(150.00), "Pedales", "hof2.jpg", 12, null, null);

        when(repository.findByCategoryIgnoreCase("PEDALES", pageable))
                .thenReturn(new PageImpl<>(List.of(tubeScreamer, hallOfFame)));

        Page<ProductResponseDTO> resultado = service.filterByCategory("PEDALES", pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getName)
                .contains("Ibanez Tube Screamer TS9", "TC Electronic Hall of Fame 2");
    }

    @Test
    void filterByCategory_retornaListaVaciaSiNoHayProductosEnCategoria() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByCategoryIgnoreCase("Baterías", pageable)).thenReturn(Page.empty());

        assertThat(service.filterByCategory("Baterías", pageable).getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findLowStock
    // -------------------------------------------------------------------------

    @Test
    void findLowStock_retornaAmplificadoresConStockBajo() {
        Pageable pageable = PageRequest.of(0, 10);
        Product voxAC30 = new Product(4, "Vox AC30",
                "Amplificador valvular de 30W", BigDecimal.valueOf(2200.00), "Amplificadores", "vox_ac30.jpg", 1, null, null);

        when(repository.findByStockLessThanEqual(5, pageable))
                .thenReturn(new PageImpl<>(List.of(marshallDsl40, voxAC30)));

        Page<ProductResponseDTO> resultado = service.findLowStock(5, pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getStock)
                .allMatch(stock -> stock <= 5);
    }

    @Test
    void findLowStock_retornaListaVaciaSiTodosLosProductosTienenStockSuficiente() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByStockLessThanEqual(0, pageable)).thenReturn(Page.empty());

        assertThat(service.findLowStock(0, pageable).getContent()).isEmpty();
    }

    @Test
    void findLowStock_usaUmbralDeBDCuandoMinEsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        when(settingRepository.findByKey("minimum_stock"))
                .thenReturn(Optional.of(new Setting(1, "minimum_stock", "5")));
        when(repository.findByStockLessThanEqual(5, pageable))
                .thenReturn(new PageImpl<>(List.of(marshallDsl40)));

        Page<ProductResponseDTO> resultado = service.findLowStock(null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository).findByStockLessThanEqual(5, pageable);
    }

    @Test
    void findLowStock_usaFallbackCuandoNoExisteConfiguracion() {
        Pageable pageable = PageRequest.of(0, 10);
        when(settingRepository.findByKey("minimum_stock")).thenReturn(Optional.empty());
        when(repository.findByStockLessThanEqual(5, pageable))
                .thenReturn(new PageImpl<>(List.of(marshallDsl40)));

        Page<ProductResponseDTO> resultado = service.findLowStock(null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(repository).findByStockLessThanEqual(5, pageable);
    }

    @Test
    void findLowStock_usaElValorMinimoPasadoComoParametro() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByStockLessThanEqual(3, pageable))
                .thenReturn(new PageImpl<>(List.of(marshallDsl40)));

        Page<ProductResponseDTO> resultado = service.findLowStock(3, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getName()).isEqualTo("Marshall DSL40CR");
        verify(repository).findByStockLessThanEqual(3, pageable);
    }
}
