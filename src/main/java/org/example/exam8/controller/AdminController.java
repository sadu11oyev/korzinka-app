package org.example.exam8.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.exam8.dao.BasketProduct;
import org.example.exam8.entity.Category;
import org.example.exam8.entity.Product;
import org.example.exam8.repo.CategoryRepo;
import org.example.exam8.repo.ProductRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("admin")
public class AdminController {
    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;

    @GetMapping
    public String getAdminPage(HttpServletRequest request,
                               @RequestParam(required = false)String categoryName,
                               @RequestParam(required = false) UUID categoryId,
                               @RequestParam(required = false) String productName,
                               @RequestParam(required = false) UUID productId,

                                Model model){
        model.addAttribute("url",request.getRequestURI());
        List<Category> categories;
        if(categoryName!=null){
            categories=categoryRepo.findAllByName(categoryName);
            model.addAttribute("categoryName",categoryName);
        }else {
            categories=categoryRepo.findAllOrderByName();
        }
        model.addAttribute("categories",categories);

        if (categoryId!=null){
            List<Product> products;
            if (productName!=null){
                model.addAttribute("productName",productName);
                products = productRepo.findAllByCategoryIdAndNameContainingIgnoreCase(categoryId,productName);
            }else {
                products = productRepo.findAllByCategoryId(categoryId);
            }

            if (productId!=null){
                Product currentProduct = productRepo.findById(productId).get();
                model.addAttribute("currentProduct",currentProduct);
                model.addAttribute("productId",productId);
            }
            model.addAttribute("categoryId",categoryId);
            model.addAttribute("products",products);
        }
        return "admin/admin";
    }

    @GetMapping("/category")
    public String getAddCategoryPage(){
        return "admin/addCategory";
    }

    @PostMapping("/category")
    public String addCategory(@RequestParam String name){
        Category category = Category.builder()
                .name(name)
                .build();
        categoryRepo.save(category);
        return "admin/admin";
    }

    @GetMapping("/product")
    public String getAddProductPage(Model model){
        model.addAttribute("categories",categoryRepo.findAll());
        return "admin/addProduct";
    }
    @PostMapping("/product")
    public String addProduct(@RequestParam String categoryId,
                             @RequestParam String name,
                             @RequestParam Integer sell,
                             @RequestParam(name = "photo") MultipartFile file
                             ) throws IOException {
        Product product = Product.builder()
                .name(name)
                .category(categoryRepo.findById(UUID.fromString(categoryId)).get())
                .selling(sell)
                .photo(file.getBytes())
                .build();
        productRepo.save(product);
        return "admin/admin";
    }

    @PostMapping("/editProduct")
    public String editProduct(@RequestParam(required = false)UUID currentProductId,
                              @RequestParam(required = false)String name,
                              @RequestParam(required = false)Integer sell,
                              @RequestParam(name = "photo") MultipartFile file)throws IOException{
        Product product = productRepo.findById(currentProductId).get();
        product.setName(name);
        product.setSelling(sell);
        if (!file.isEmpty()){
            product.setPhoto(file.getBytes());
        }
        productRepo.save(product);
        return "redirect:/admin";
    }

}
