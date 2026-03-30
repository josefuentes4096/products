package com.josefuentes4096.products.service;

import com.josefuentes4096.products.dto.ProductRequestDTO;
import com.josefuentes4096.products.dto.ProductResponseDTO;
import com.josefuentes4096.products.entity.Product;
import com.josefuentes4096.products.exception.ProductNotFoundException;
import com.josefuentes4096.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public List<ProductResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ProductResponseDTO obtenerPorId(Integer id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }

    public ProductResponseDTO guardar(ProductRequestDTO dto) {
        Product product = toEntity(dto);
        return toDTO(repository.save(product));
    }

    public ProductResponseDTO actualizar(Integer id, ProductRequestDTO dto) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setNombre(dto.getNombre());
        product.setDescripcion(dto.getDescripcion());
        product.setPrecio(dto.getPrecio());
        product.setCategoria(dto.getCategoria());
        product.setImagenUrl(dto.getImagenUrl());
        product.setStock(dto.getStock());

        return toDTO(repository.save(product));
    }

    public void eliminar(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        repository.delete(product);
    }

    public ProductResponseDTO buscarPorNombre(String nombre) {
        return repository.findByNombre(nombre)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public List<ProductResponseDTO> filtrarPorCategoria(String categoria) {
        return repository.findByCategoriaIgnoreCase(categoria)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProductResponseDTO> stockMinimo(Integer min) {
        return repository.findByStockLessThanEqual(min)
                .stream()
                .map(this::toDTO)
                .toList();
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
