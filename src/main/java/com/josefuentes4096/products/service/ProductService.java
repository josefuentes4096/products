package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Page<ProductResponseDTO> listar(Pageable pageable) {
        log.info("Listando productos - página {}, tamaño {}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(this::toDTO);
    }

    public ProductResponseDTO obtenerPorId(Integer id) {
        log.info("Buscando producto con id: {}", id);
        return toDTO(repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                }));
    }

    @Transactional
    public ProductResponseDTO guardar(ProductRequestDTO dto) {
        log.info("Creando producto: {}", dto.getNombre());
        Product product = toEntity(dto);
        ProductResponseDTO resultado = toDTO(repository.save(product));
        log.info("Producto creado con id: {}", resultado.getId());
        return resultado;
    }

    @Transactional
    public ProductResponseDTO actualizar(Integer id, ProductRequestDTO dto) {
        log.info("Actualizando producto con id: {}", id);
        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                });

        product.setNombre(dto.getNombre());
        product.setDescripcion(dto.getDescripcion());
        product.setPrecio(dto.getPrecio());
        product.setCategoria(dto.getCategoria());
        product.setImagenUrl(dto.getImagenUrl());
        product.setStock(dto.getStock());

        return toDTO(repository.save(product));
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando producto con id: {}", id);
        Product product = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con id: {}", id);
                    return new ProductNotFoundException(id);
                });
        repository.delete(product);
        log.info("Producto con id: {} eliminado", id);
    }

    public ProductResponseDTO buscarPorNombre(String nombre) {
        log.info("Buscando producto con nombre: {}", nombre);
        return repository.findByNombre(nombre)
                .map(this::toDTO)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado con nombre: {}", nombre);
                    return new ProductNotFoundException(nombre);
                });
    }

    public Page<ProductResponseDTO> filtrarPorCategoria(String categoria, Pageable pageable) {
        log.info("Filtrando productos por categoría: {}", categoria);
        return repository.findByCategoriaIgnoreCase(categoria, pageable).map(this::toDTO);
    }

    public Page<ProductResponseDTO> stockMinimo(Integer min, Pageable pageable) {
        log.info("Buscando productos con stock <= {}", min);
        return repository.findByStockLessThanEqual(min, pageable).map(this::toDTO);
    }

    private ProductResponseDTO toDTO(Product p) {
        return new ProductResponseDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getCategoria(),
                p.getImagenUrl(),
                p.getStock()
        );
    }

    private Product toEntity(ProductRequestDTO dto) {
        Product p = new Product();
        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecio(dto.getPrecio());
        p.setCategoria(dto.getCategoria());
        p.setImagenUrl(dto.getImagenUrl());
        p.setStock(dto.getStock());
        return p;
    }
}
