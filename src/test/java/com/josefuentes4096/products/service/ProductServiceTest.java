package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product stratocaster;
    private Product tubeScreamer;
    private Product marshallDsl40;

    @BeforeEach
    void setUp() {
        stratocaster = new Product(1, "Fender Stratocaster American Pro II",
                "Guitarra eléctrica de cuerpo sólido con pastillas V-Mod II",
                1500.00, "Guitarras", "fender_strat.jpg", 8, null, null);

        tubeScreamer = new Product(2, "Ibanez Tube Screamer TS9",
                "Pedal de overdrive clásico, el favorito de Stevie Ray Vaughan",
                120.00, "Pedales", "ts9.jpg", 15, null, null);

        marshallDsl40 = new Product(3, "Marshall DSL40CR",
                "Amplificador valvular de 40W con dos canales y reverb",
                1200.00, "Amplificadores", "marshall_dsl40.jpg", 3, null, null);
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    void listar_retornaTodosLosInstrumentos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(stratocaster, tubeScreamer, marshallDsl40)));

        Page<ProductResponseDTO> resultado = service.listar(pageable);

        assertThat(resultado.getContent()).hasSize(3);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getNombre)
                .containsExactly(
                        "Fender Stratocaster American Pro II",
                        "Ibanez Tube Screamer TS9",
                        "Marshall DSL40CR");
    }

    @Test
    void listar_retornaListaVaciaSiNoHayProductos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(Page.empty());

        assertThat(service.listar(pageable).getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // obtenerPorId
    // -------------------------------------------------------------------------

    @Test
    void obtenerPorId_retornaGuitarraCuandoExiste() {
        when(repository.findById(1)).thenReturn(Optional.of(stratocaster));

        ProductResponseDTO resultado = service.obtenerPorId(1);

        assertThat(resultado.getId()).isEqualTo(1);
        assertThat(resultado.getNombre()).isEqualTo("Fender Stratocaster American Pro II");
        assertThat(resultado.getPrecio()).isEqualTo(1500.00);
        assertThat(resultado.getCategoria()).isEqualTo("Guitarras");
    }

    @Test
    void obtenerPorId_retornaPedalCuandoExiste() {
        when(repository.findById(2)).thenReturn(Optional.of(tubeScreamer));

        ProductResponseDTO resultado = service.obtenerPorId(2);

        assertThat(resultado.getNombre()).isEqualTo("Ibanez Tube Screamer TS9");
        assertThat(resultado.getCategoria()).isEqualTo("Pedales");
    }

    @Test
    void obtenerPorId_lanzaExcepcionCuandoInstrumentoNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // guardar
    // -------------------------------------------------------------------------

    @Test
    void guardar_creaAmplificadorValvularCorrectamente() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setNombre("Vox AC30");
        dto.setDescripcion("Amplificador valvular de 30W, icono del sonido británico");
        dto.setPrecio(2200.00);
        dto.setCategoria("Amplificadores");
        dto.setImagenUrl("vox_ac30.jpg");
        dto.setStock(2);

        Product guardado = new Product(4, "Vox AC30",
                "Amplificador valvular de 30W, icono del sonido británico",
                2200.00, "Amplificadores", "vox_ac30.jpg", 2, null, null);
        when(repository.save(any())).thenReturn(guardado);

        ProductResponseDTO resultado = service.guardar(dto);

        assertThat(resultado.getNombre()).isEqualTo("Vox AC30");
        assertThat(resultado.getPrecio()).isEqualTo(2200.00);
        assertThat(resultado.getStock()).isEqualTo(2);
        verify(repository).save(any(Product.class));
    }

    @Test
    void guardar_creaPedalDeEfectoCorrectamente() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setNombre("Boss DS-1 Distortion");
        dto.setDescripcion("Pedal de distorsión compacto, el más vendido en la historia");
        dto.setPrecio(80.00);
        dto.setCategoria("Pedales");
        dto.setImagenUrl("boss_ds1.jpg");
        dto.setStock(20);

        Product guardado = new Product(5, "Boss DS-1 Distortion",
                "Pedal de distorsión compacto, el más vendido en la historia",
                80.00, "Pedales", "boss_ds1.jpg", 20, null, null);
        when(repository.save(any())).thenReturn(guardado);

        ProductResponseDTO resultado = service.guardar(dto);

        assertThat(resultado.getNombre()).isEqualTo("Boss DS-1 Distortion");
        assertThat(resultado.getCategoria()).isEqualTo("Pedales");
        verify(repository).save(any(Product.class));
    }

    // -------------------------------------------------------------------------
    // actualizar
    // -------------------------------------------------------------------------

    @Test
    void actualizar_modificaPrecioYStockDeGuitarra() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setNombre("Fender Stratocaster American Pro II");
        dto.setDescripcion("Guitarra eléctrica de cuerpo sólido con pastillas V-Mod II");
        dto.setPrecio(1650.00);
        dto.setCategoria("Guitarras");
        dto.setImagenUrl("fender_strat_v2.jpg");
        dto.setStock(5);

        when(repository.findById(1)).thenReturn(Optional.of(stratocaster));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO resultado = service.actualizar(1, dto);

        assertThat(resultado.getPrecio()).isEqualTo(1650.00);
        assertThat(resultado.getStock()).isEqualTo(5);
        assertThat(resultado.getImagenUrl()).isEqualTo("fender_strat_v2.jpg");
    }

    @Test
    void actualizar_cambiaDescripcionDeAmplificador() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setNombre("Marshall DSL40CR");
        dto.setDescripcion("Amplificador valvular 40W - edición 2024 con nueva reverb digital");
        dto.setPrecio(1250.00);
        dto.setCategoria("Amplificadores");
        dto.setImagenUrl("marshall_dsl40_2024.jpg");
        dto.setStock(3);

        when(repository.findById(3)).thenReturn(Optional.of(marshallDsl40));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO resultado = service.actualizar(3, dto);

        assertThat(resultado.getDescripcion()).contains("2024");
        assertThat(resultado.getPrecio()).isEqualTo(1250.00);
    }

    @Test
    void actualizar_lanzaExcepcionSiInstrumentoNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99, new ProductRequestDTO()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // eliminar
    // -------------------------------------------------------------------------

    @Test
    void eliminar_eliminaPedalExistente() {
        when(repository.findById(2)).thenReturn(Optional.of(tubeScreamer));

        service.eliminar(2);

        verify(repository).delete(tubeScreamer);
    }

    @Test
    void eliminar_lanzaExcepcionSiInstrumentoNoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99))
                .isInstanceOf(ProductNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // buscarPorNombre
    // -------------------------------------------------------------------------

    @Test
    void buscarPorNombre_encuentraAmplificadorPorNombreExacto() {
        when(repository.findByNombre("Marshall DSL40CR")).thenReturn(Optional.of(marshallDsl40));

        ProductResponseDTO resultado = service.buscarPorNombre("Marshall DSL40CR");

        assertThat(resultado.getNombre()).isEqualTo("Marshall DSL40CR");
        assertThat(resultado.getCategoria()).isEqualTo("Amplificadores");
    }

    @Test
    void buscarPorNombre_lanzaExcepcionSiNombreNoExiste() {
        when(repository.findByNombre("Gibson Les Paul Custom")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorNombre("Gibson Les Paul Custom"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Gibson Les Paul Custom");
    }

    // -------------------------------------------------------------------------
    // filtrarPorCategoria
    // -------------------------------------------------------------------------

    @Test
    void filtrarPorCategoria_retornaGuitarrasDeLaCategoria() {
        Pageable pageable = PageRequest.of(0, 10);
        Product lesPaul = new Product(6, "Gibson Les Paul Standard",
                "Guitarra eléctrica con cuerpo de caoba y tapa de arce flameado",
                2800.00, "Guitarras", "gibson_lp.jpg", 2, null, null);

        when(repository.findByCategoriaIgnoreCase("guitarras", pageable))
                .thenReturn(new PageImpl<>(List.of(stratocaster, lesPaul)));

        Page<ProductResponseDTO> resultado = service.filtrarPorCategoria("guitarras", pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getCategoria)
                .containsOnly("Guitarras");
    }

    @Test
    void filtrarPorCategoria_retornaPedalesIgnorandoMayusculas() {
        Pageable pageable = PageRequest.of(0, 10);
        Product hallOfFame = new Product(7, "TC Electronic Hall of Fame 2",
                "Pedal de reverb con TonePrint y algoritmos de sala",
                150.00, "Pedales", "hof2.jpg", 12, null, null);

        when(repository.findByCategoriaIgnoreCase("PEDALES", pageable))
                .thenReturn(new PageImpl<>(List.of(tubeScreamer, hallOfFame)));

        Page<ProductResponseDTO> resultado = service.filtrarPorCategoria("PEDALES", pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getNombre)
                .contains("Ibanez Tube Screamer TS9", "TC Electronic Hall of Fame 2");
    }

    @Test
    void filtrarPorCategoria_retornaListaVaciaSiNoHayProductosEnCategoria() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByCategoriaIgnoreCase("Baterías", pageable)).thenReturn(Page.empty());

        assertThat(service.filtrarPorCategoria("Baterías", pageable).getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // stockMinimo
    // -------------------------------------------------------------------------

    @Test
    void stockMinimo_retornaAmplificadoresConStockBajo() {
        Pageable pageable = PageRequest.of(0, 10);
        Product voxAC30 = new Product(4, "Vox AC30",
                "Amplificador valvular de 30W", 2200.00, "Amplificadores", "vox_ac30.jpg", 1, null, null);

        when(repository.findByStockLessThanEqual(5, pageable))
                .thenReturn(new PageImpl<>(List.of(marshallDsl40, voxAC30)));

        Page<ProductResponseDTO> resultado = service.stockMinimo(5, pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent()).extracting(ProductResponseDTO::getStock)
                .allMatch(stock -> stock <= 5);
    }

    @Test
    void stockMinimo_retornaListaVaciaSiTodosLosProductosTienenStockSuficiente() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByStockLessThanEqual(0, pageable)).thenReturn(Page.empty());

        assertThat(service.stockMinimo(0, pageable).getContent()).isEmpty();
    }

    @Test
    void stockMinimo_usaElValorMinimoPasadoComoParametro() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByStockLessThanEqual(3, pageable))
                .thenReturn(new PageImpl<>(List.of(marshallDsl40)));

        Page<ProductResponseDTO> resultado = service.stockMinimo(3, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Marshall DSL40CR");
        verify(repository).findByStockLessThanEqual(3, pageable);
    }
}
