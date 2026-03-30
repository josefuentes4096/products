package com.josefuentes4096.products.service;

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

    public List<Product> listar() {
        return repository.findAll();
    }

    public Product obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product guardar(Product product) {
        return repository.save(product);
    }

    public Product actualizar(Integer id, Product nuevo) {
        Product product = obtenerPorId(id);

        product.setNombre(nuevo.getNombre());
        product.setDescripcion(nuevo.getDescripcion());
        product.setPrecio(nuevo.getPrecio());
        product.setCategoria(nuevo.getCategoria());
        product.setImagenUrl(nuevo.getImagenUrl());
        product.setStock(nuevo.getStock());

        return repository.save(product);
    }

    public void eliminar(Integer id) {
		obtenerPorId(id);
        repository.deleteById(id);
    }
}