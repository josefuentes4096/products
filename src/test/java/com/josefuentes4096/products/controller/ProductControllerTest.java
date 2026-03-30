package com.josefuentes4096.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@WithMockUser
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean ProductService service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ProductResponseDTO stratocasterResponse() {
        return new ProductResponseDTO(1, "Fender Stratocaster American Pro II",
                "Guitarra eléctrica con pastillas V-Mod II",
                1500.00, "Guitarras", "fender_strat.jpg", 8);
    }

    private ProductResponseDTO tubeScreamerResponse() {
        return new ProductResponseDTO(2, "Ibanez Tube Screamer TS9",
                "Pedal de overdrive clásico",
                120.00, "Pedales", "ts9.jpg", 15);
    }

    private ProductResponseDTO marshallResponse() {
        return new ProductResponseDTO(3, "Marshall DSL40CR",
                "Amplificador valvular de 40W",
                1200.00, "Amplificadores", "marshall_dsl40.jpg", 3);
    }

    private ProductRequestDTO stratocasterRequest() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setNombre("Fender Stratocaster American Pro II");
        dto.setDescripcion("Guitarra eléctrica con pastillas V-Mod II");
        dto.setPrecio(1500.00);
        dto.setCategoria("Guitarras");
        dto.setImagenUrl("fender_strat.jpg");
        dto.setStock(8);
        return dto;
    }

    // -------------------------------------------------------------------------
    // GET /api/products
    // -------------------------------------------------------------------------

    @Test
    void GET_listar_retorna200ConTodosLosInstrumentos() throws Exception {
        when(service.listar(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(stratocasterResponse(), tubeScreamerResponse(), marshallResponse())));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].nombre").value("Fender Stratocaster American Pro II"))
                .andExpect(jsonPath("$.content[1].nombre").value("Ibanez Tube Screamer TS9"))
                .andExpect(jsonPath("$.content[2].nombre").value("Marshall DSL40CR"));
    }

    @Test
    void GET_listar_retorna200ConListaVaciaSiNoHayInstrumentos() throws Exception {
        when(service.listar(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void GET_obtener_retorna200ConGuitarraCuandoExiste() throws Exception {
        when(service.obtenerPorId(1)).thenReturn(stratocasterResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Fender Stratocaster American Pro II"))
                .andExpect(jsonPath("$.precio").value(1500.00))
                .andExpect(jsonPath("$.categoria").value("Guitarras"));
    }

    @Test
    void GET_obtener_retorna200ConPedalCuandoExiste() throws Exception {
        when(service.obtenerPorId(2)).thenReturn(tubeScreamerResponse());

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ibanez Tube Screamer TS9"))
                .andExpect(jsonPath("$.categoria").value("Pedales"));
    }

    @Test
    void GET_obtener_retorna404CuandoInstrumentoNoExiste() throws Exception {
        when(service.obtenerPorId(99)).thenThrow(new ProductNotFoundException(99));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/products
    // -------------------------------------------------------------------------

    @Test
    void POST_crear_retorna200AlCrearAmplificadorValvular() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Vox AC30");
        request.setDescripcion("Amplificador valvular 30W icono del sonido británico");
        request.setPrecio(2200.00);
        request.setCategoria("Amplificadores");
        request.setImagenUrl("vox_ac30.jpg");
        request.setStock(2);

        ProductResponseDTO response = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular 30W icono del sonido británico",
                2200.00, "Amplificadores", "vox_ac30.jpg", 2);
        when(service.guardar(any())).thenReturn(response);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Vox AC30"))
                .andExpect(jsonPath("$.precio").value(2200.00))
                .andExpect(jsonPath("$.categoria").value("Amplificadores"));
    }

    @Test
    void POST_crear_retorna200AlCrearPedalDeEfecto() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Boss DS-1 Distortion");
        request.setDescripcion("Pedal de distorsión compacto más vendido de la historia");
        request.setPrecio(80.00);
        request.setCategoria("Pedales");
        request.setImagenUrl("boss_ds1.jpg");
        request.setStock(25);

        ProductResponseDTO response = new ProductResponseDTO(5, "Boss DS-1 Distortion",
                "Pedal de distorsión compacto más vendido de la historia",
                80.00, "Pedales", "boss_ds1.jpg", 25);
        when(service.guardar(any())).thenReturn(response);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Boss DS-1 Distortion"))
                .andExpect(jsonPath("$.stock").value(25));
    }

    @Test
    void POST_crear_retorna400SiNombreDeInstrumentoEsBlanco() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("");
        request.setPrecio(500.00);
        request.setStock(3);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_crear_retorna400SiPrecioEsCero() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Gibson Les Paul Standard");
        request.setPrecio(0.0);
        request.setStock(1);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_crear_retorna400SiPrecioEsNegativo() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Pedal genérico");
        request.setPrecio(-50.0);
        request.setStock(5);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_crear_retorna400SiStockEsNegativo() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setNombre("Fender Telecaster");
        request.setPrecio(1100.00);
        request.setStock(-1);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void PUT_actualizar_retorna200AlActualizarGuitarra() throws Exception {
        ProductRequestDTO request = stratocasterRequest();
        request.setPrecio(1650.00);

        ProductResponseDTO response = new ProductResponseDTO(1,
                "Fender Stratocaster American Pro II",
                "Guitarra eléctrica con pastillas V-Mod II",
                1650.00, "Guitarras", "fender_strat.jpg", 8);
        when(service.actualizar(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/products/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(1650.00));
    }

    @Test
    void PUT_actualizar_retorna404SiInstrumentoNoExiste() throws Exception {
        when(service.actualizar(eq(99), any())).thenThrow(new ProductNotFoundException(99));

        mockMvc.perform(put("/api/products/99").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stratocasterRequest())))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void DELETE_eliminar_retorna200AlEliminarPedal() throws Exception {
        doNothing().when(service).eliminar(2);

        mockMvc.perform(delete("/api/products/2").with(csrf()))
                .andExpect(status().isOk());

        verify(service).eliminar(2);
    }

    @Test
    void DELETE_eliminar_retorna404SiAmplificadorNoExiste() throws Exception {
        doThrow(new ProductNotFoundException(99)).when(service).eliminar(99);

        mockMvc.perform(delete("/api/products/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/products/buscar
    // -------------------------------------------------------------------------

    @Test
    void GET_buscar_retornaGuitarraPorNombre() throws Exception {
        when(service.buscarPorNombre("Fender Stratocaster American Pro II"))
                .thenReturn(stratocasterResponse());

        mockMvc.perform(get("/api/products/buscar")
                .param("nombre", "Fender Stratocaster American Pro II"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Fender Stratocaster American Pro II"));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/categoria/{categoria}
    // -------------------------------------------------------------------------

    @Test
    void GET_categoria_retornaAmplificadoresValvulares() throws Exception {
        ProductResponseDTO voxResponse = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular 30W", 2200.00, "Amplificadores", "vox_ac30.jpg", 1);

        when(service.filtrarPorCategoria(eq("Amplificadores"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(marshallResponse(), voxResponse)));

        mockMvc.perform(get("/api/products/categoria/Amplificadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].categoria").value("Amplificadores"))
                .andExpect(jsonPath("$.content[1].nombre").value("Vox AC30"));
    }

    @Test
    void GET_categoria_retornaPedalesDeEfecto() throws Exception {
        ProductResponseDTO hallOfFameResponse = new ProductResponseDTO(7,
                "TC Electronic Hall of Fame 2",
                "Pedal de reverb con TonePrint",
                150.00, "Pedales", "hof2.jpg", 12);

        when(service.filtrarPorCategoria(eq("Pedales"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tubeScreamerResponse(), hallOfFameResponse)));

        mockMvc.perform(get("/api/products/categoria/Pedales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nombre").value("Ibanez Tube Screamer TS9"))
                .andExpect(jsonPath("$.content[1].nombre").value("TC Electronic Hall of Fame 2"));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/stock-minimo
    // -------------------------------------------------------------------------

    @Test
    void GET_stockMinimo_usaDefault5YRetornaAmplificadoresConBajoStock() throws Exception {
        when(service.stockMinimo(eq(5), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(marshallResponse())));

        mockMvc.perform(get("/api/products/stock-minimo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Marshall DSL40CR"))
                .andExpect(jsonPath("$.content[0].stock").value(3));

        verify(service).stockMinimo(eq(5), any(Pageable.class));
    }

    @Test
    void GET_stockMinimo_usaParametroPersonalizadoParaInstrumentosEscasos() throws Exception {
        ProductResponseDTO voxResponse = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular 30W", 2200.00, "Amplificadores", "vox_ac30.jpg", 1);

        when(service.stockMinimo(eq(2), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(voxResponse)));

        mockMvc.perform(get("/api/products/stock-minimo").param("min", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Vox AC30"));

        verify(service).stockMinimo(eq(2), any(Pageable.class));
    }

    @Test
    void GET_stockMinimo_retornaListaVaciaSiTodosLosInstrumentosTienenStock() throws Exception {
        when(service.stockMinimo(eq(5), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/products/stock-minimo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
