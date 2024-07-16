package org.example.exam8.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.exam8.entity.Category;
import org.example.exam8.entity.Income;
import org.example.exam8.entity.Product;
import org.example.exam8.entity.Sale;
import org.example.exam8.repo.CategoryRepo;
import org.example.exam8.repo.IncomeRepo;
import org.example.exam8.repo.ProductRepo;
import org.example.exam8.repo.SaleRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class PageController {
    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;
    private final IncomeRepo incomeRepo;
    private final SaleRepo saleRepo;

    @GetMapping
    public String get(@RequestParam(required = false)String categoryName,
                      @RequestParam(required = false) UUID categoryId,
                      @RequestParam(required = false) String productName,
                      @RequestParam(required = false) UUID productId,
                      @RequestParam(required = false) String income,
                      @RequestParam(required = false)String sale,
                      @RequestParam(required = false)String info,
                      @RequestParam(required = false)String add,
                      HttpServletRequest request,
                      Model model){
        model.addAttribute("url",request.getRequestURI());
        List<Category> categories;
        if(categoryName!=null){
            model.addAttribute("categoryName",categoryName);
            categories=categoryRepo.findAllByName(categoryName);
        }else {
            categories=categoryRepo.findAllOrderByName();
        }
        model.addAttribute("categories",categories);

        if (categoryId!=null){
            model.addAttribute("categoryId",categoryId);
            List<Product> products;
            if (productName!=null){
                model.addAttribute("productName",productName);
                products = productRepo.findAllByCategoryIdAndNameContainingIgnoreCase(categoryId,productName);
            }else {
                products = productRepo.findAllByCategoryId(categoryId);
            }
            if (productId!=null){
                model.addAttribute("productId",productId);
                if (income!=null){
                    List<Income> incomes = incomeRepo.findAllByProductId(productId);
                    model.addAttribute("incomes",incomes);
                } else if (add!=null) {
                    model.addAttribute("addProduct",productRepo.findById(productId).get());
                } else if (sale!=null){
                    List<Sale> sales = saleRepo.findAllByProductId(productId);
                    model.addAttribute("sales",sales);
                } else if (info!=null) {
                    model.addAttribute("product",productRepo.findById(productId).get());
                }
            }
            model.addAttribute("products",products);
        }
        return "main";
    }

    @PostMapping
    public String addIncome(@RequestParam(name = "productId") UUID productId,
                            @RequestParam(name = "categoryId") UUID categoryId,
                            @RequestParam(name = "price")Integer price,
                            @RequestParam(name = "amount")Integer amount
                            ){
        Product product = productRepo.findById(productId).get();
        Income income = Income.builder()
                .product(product)
                .price(price)
                .amount(amount)
                .build();
        incomeRepo.save(income);
        return "redirect:/?categoryId="+categoryId+"&productId="+productId+"&income=1";
    }

}
