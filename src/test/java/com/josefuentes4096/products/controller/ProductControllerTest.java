package com.josefuentes4096.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
        dto.setName("Fender Stratocaster American Pro II");
        dto.setDescription("Guitarra eléctrica con pastillas V-Mod II");
        dto.setPrice(1500.00);
        dto.setCategory("Guitarras");
        dto.setImageUrl("fender_strat.jpg");
        dto.setStock(8);
        return dto;
    }

    // -------------------------------------------------------------------------
    // GET /api/products
    // -------------------------------------------------------------------------

    @Test
    void GET_findAll_retorna200ConTodosLosInstrumentos() throws Exception {
        when(service.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(stratocasterResponse(), tubeScreamerResponse(), marshallResponse())));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].name").value("Fender Stratocaster American Pro II"))
                .andExpect(jsonPath("$.content[1].name").value("Ibanez Tube Screamer TS9"))
                .andExpect(jsonPath("$.content[2].name").value("Marshall DSL40CR"));
    }

    @Test
    void GET_findAll_retorna200ConListaVaciaSiNoHayInstrumentos() throws Exception {
        when(service.findAll(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void GET_findById_retorna200ConGuitarraCuandoExiste() throws Exception {
        when(service.findById(1)).thenReturn(stratocasterResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fender Stratocaster American Pro II"))
                .andExpect(jsonPath("$.price").value(1500.00))
                .andExpect(jsonPath("$.category").value("Guitarras"));
    }

    @Test
    void GET_findById_retorna200ConPedalCuandoExiste() throws Exception {
        when(service.findById(2)).thenReturn(tubeScreamerResponse());

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ibanez Tube Screamer TS9"))
                .andExpect(jsonPath("$.category").value("Pedales"));
    }

    @Test
    void GET_findById_retorna404CuandoInstrumentoNoExiste() throws Exception {
        when(service.findById(99)).thenThrow(new ProductNotFoundException(99));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/products
    // -------------------------------------------------------------------------

    @Test
    void POST_create_retorna200AlCrearAmplificadorValvular() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Vox AC30");
        request.setDescription("Amplificador valvular 30W icono del sonido británico");
        request.setPrice(2200.00);
        request.setCategory("Amplificadores");
        request.setImageUrl("vox_ac30.jpg");
        request.setStock(2);

        ProductResponseDTO response = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular 30W icono del sonido británico",
                2200.00, "Amplificadores", "vox_ac30.jpg", 2);
        when(service.save(any())).thenReturn(response);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vox AC30"))
                .andExpect(jsonPath("$.price").value(2200.00))
                .andExpect(jsonPath("$.category").value("Amplificadores"));
    }

    @Test
    void POST_create_retorna200AlCrearPedalDeEfecto() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Boss DS-1 Distortion");
        request.setDescription("Pedal de distorsión compacto más vendido de la historia");
        request.setPrice(80.00);
        request.setCategory("Pedales");
        request.setImageUrl("boss_ds1.jpg");
        request.setStock(25);

        ProductResponseDTO response = new ProductResponseDTO(5, "Boss DS-1 Distortion",
                "Pedal de distorsión compacto más vendido de la historia",
                80.00, "Pedales", "boss_ds1.jpg", 25);
        when(service.save(any())).thenReturn(response);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Boss DS-1 Distortion"))
                .andExpect(jsonPath("$.stock").value(25));
    }

    @Test
    void POST_create_retorna400SiNombreDeInstrumentoEsBlanco() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("");
        request.setPrice(500.00);
        request.setStock(3);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna400SiPrecioEsCero() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Gibson Les Paul Standard");
        request.setPrice(0.0);
        request.setStock(1);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna400SiPrecioEsNegativo() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Pedal genérico");
        request.setPrice(-50.0);
        request.setStock(5);

        mockMvc.perform(post("/api/products").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_create_retorna400SiStockEsNegativo() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Fender Telecaster");
        request.setPrice(1100.00);
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
    void PUT_update_retorna200AlActualizarGuitarra() throws Exception {
        ProductRequestDTO request = stratocasterRequest();
        request.setPrice(1650.00);

        ProductResponseDTO response = new ProductResponseDTO(1,
                "Fender Stratocaster American Pro II",
                "Guitarra eléctrica con pastillas V-Mod II",
                1650.00, "Guitarras", "fender_strat.jpg", 8);
        when(service.update(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/products/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(1650.00));
    }

    @Test
    void PUT_update_retorna404SiInstrumentoNoExiste() throws Exception {
        when(service.update(eq(99), any())).thenThrow(new ProductNotFoundException(99));

        mockMvc.perform(put("/api/products/99").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stratocasterRequest())))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    void DELETE_delete_retorna200AlEliminarPedal() throws Exception {
        doNothing().when(service).delete(2);

        mockMvc.perform(delete("/api/products/2").with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(2);
    }

    @Test
    void DELETE_delete_retorna404SiAmplificadorNoExiste() throws Exception {
        doThrow(new ProductNotFoundException(99)).when(service).delete(99);

        mockMvc.perform(delete("/api/products/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/products/search
    // -------------------------------------------------------------------------

    @Test
    void GET_findByName_retornaGuitarraPorNombre() throws Exception {
        when(service.findByName("Fender Stratocaster American Pro II"))
                .thenReturn(stratocasterResponse());

        mockMvc.perform(get("/api/products/search")
                .param("name", "Fender Stratocaster American Pro II"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fender Stratocaster American Pro II"));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/category/{category}
    // -------------------------------------------------------------------------

    @Test
    void GET_filterByCategory_retornaAmplificadoresValvulares() throws Exception {
        ProductResponseDTO voxResponse = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular 30W", 2200.00, "Amplificadores", "vox_ac30.jpg", 1);

        when(service.filterByCategory(eq("Amplificadores"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(marshallResponse(), voxResponse)));

        mockMvc.perform(get("/api/products/category/Amplificadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].category").value("Amplificadores"))
                .andExpect(jsonPath("$.content[1].name").value("Vox AC30"));
    }

    @Test
    void GET_filterByCategory_retornaPedalesDeEfecto() throws Exception {
        ProductResponseDTO hallOfFameResponse = new ProductResponseDTO(7,
                "TC Electronic Hall of Fame 2",
                "Pedal de reverb con TonePrint",
                150.00, "Pedales", "hof2.jpg", 12);

        when(service.filterByCategory(eq("Pedales"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tubeScreamerResponse(), hallOfFameResponse)));

        mockMvc.perform(get("/api/products/category/Pedales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Ibanez Tube Screamer TS9"))
                .andExpect(jsonPath("$.content[1].name").value("TC Electronic Hall of Fame 2"));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/low-stock
    // -------------------------------------------------------------------------

    @Test
    void GET_findLowStock_usaDefault5YRetornaAmplificadoresConBajoStock() throws Exception {
        when(service.findLowStock(eq(5), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(marshallResponse())));

        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Marshall DSL40CR"))
                .andExpect(jsonPath("$.content[0].stock").value(3));

        verify(service).findLowStock(eq(5), any(Pageable.class));
    }

    @Test
    void GET_findLowStock_usaParametroPersonalizadoParaInstrumentosEscasos() throws Exception {
        ProductResponseDTO voxResponse = new ProductResponseDTO(4, "Vox AC30",
                "Amplificador valvular 30W", 2200.00, "Amplificadores", "vox_ac30.jpg", 1);

        when(service.findLowStock(eq(2), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(voxResponse)));

        mockMvc.perform(get("/api/products/low-stock").param("min", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Vox AC30"));

        verify(service).findLowStock(eq(2), any(Pageable.class));
    }

    @Test
    void GET_findLowStock_retornaListaVaciaSiTodosLosInstrumentosTienenStock() throws Exception {
        when(service.findLowStock(eq(5), any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
