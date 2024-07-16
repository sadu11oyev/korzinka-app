package org.example.exam8.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.exam8.entity.Product;
import org.example.exam8.repo.ProductRepo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final ProductRepo productRepo;

    @GetMapping("{id}")
    public synchronized void download(@PathVariable UUID id, HttpServletResponse response) throws IOException {
        Product product = productRepo.findById(id).get();
        byte[] photo = product.getPhoto();
        response.getOutputStream().write(product.getPhoto());
    }
}
