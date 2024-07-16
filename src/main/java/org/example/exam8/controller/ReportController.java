package org.example.exam8.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.exam8.dao.Profit;
import org.example.exam8.dao.ReqResidualProduct;
import org.example.exam8.dao.ReqSale;
import org.example.exam8.repo.IncomeRepo;
import org.example.exam8.repo.SaleRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final SaleRepo saleRepo;
    private final IncomeRepo incomeRepo;

    @GetMapping()
    public String get(HttpServletRequest request,
                      @RequestParam(required = false)String action,
                      Model model){
        model.addAttribute("url",request.getRequestURI());
        if (action!=null){
            if (action.equals("sales")){
                List<Object[]> results = saleRepo.findSalesData();
                List<ReqSale> saleList = new ArrayList<>();
                for (Object[] result : results) {
                    ReqSale reqSale = new ReqSale((String) result[0],
                            ((Number) result[1]).longValue(),
                            ((Number)result[2]).doubleValue());
                    saleList.add(reqSale);
                }
                model.addAttribute("saleList",saleList);
            }else if (action.equals("balance")){
                List<Object[]> results = saleRepo.findBalanceData();
                List<ReqResidualProduct> residualProducts = new ArrayList<>();
                for (Object[] result : results) {
                    ReqResidualProduct reqResidualProduct = new ReqResidualProduct(
                            (String) result[0],
                            ((Number)result[1]).intValue());
                    residualProducts.add(reqResidualProduct);
                }
                model.addAttribute("residualProducts",residualProducts);
            }else if (action.equals("profit")){
                List<Object[]> results = saleRepo.findProfits();
                List<Profit> profits = new ArrayList<>();
                for (Object[] result : results) {
                    Profit profit = new Profit(
                            (String) result[0],
                            ((Number)result[1]).longValue());
                    profits.add(profit);
                }
                model.addAttribute("profits",profits);
            }
        }
        return "report";
    }
}
