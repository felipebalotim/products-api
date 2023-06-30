package com.balotim.productsapi.controllers;

import com.balotim.productsapi.dtos.ProductRecordDto;
import com.balotim.productsapi.models.ProductModel;
import com.balotim.productsapi.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ProductController {
    @Autowired
    ProductRepository productRepository;

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        List<ProductModel> productList = productRepository.findAll();
        if (!productList.isEmpty()) {
            for (ProductModel product : productList) {
                 UUID id = product.getIdProduct();
                 product.add(
                     WebMvcLinkBuilder.linkTo(
                         WebMvcLinkBuilder.methodOn(ProductController.class).getProductById(id)
                     ).withSelfRel()
                 );
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        product.get().add(
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(ProductController.class).getAllProducts()
            ).withRel("Products list")
        );
        return ResponseEntity.status(HttpStatus.OK).body(product.get());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProdutct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<Object> updateProduct(
        @PathVariable(value = "id") UUID id,
        @RequestBody @Valid ProductRecordDto productRecordDto
    ) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        var productModel = product.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        productRepository.delete(product.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
    }
}
