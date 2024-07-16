package org.example.exam8.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.exam8.dao.Basket;
import org.example.exam8.dao.BasketProduct;
import org.example.exam8.entity.Category;
import org.example.exam8.entity.Product;
import org.example.exam8.entity.Sale;
import org.example.exam8.repo.CategoryRepo;
import org.example.exam8.repo.IncomeRepo;
import org.example.exam8.repo.ProductRepo;
import org.example.exam8.repo.SaleRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("sale")
public class SaleController {
    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;
    private final IncomeRepo incomeRepo;
    private final SaleRepo saleRepo;

    @GetMapping
    public String get(@RequestParam(required = false)String categoryName,
                      @RequestParam(required = false) UUID categoryId,
                      @RequestParam(required = false) String productName,
                      @RequestParam(required = false) UUID productId,
                      @RequestParam(required = false)UUID currentProductId,
                      @RequestParam(required = false)Integer amount,
                      HttpServletRequest request, HttpSession session,
                      Model model){
        model.addAttribute("url",request.getRequestURI());

        Basket basket = Objects.requireNonNullElse((Basket) session.getAttribute("basket"),new Basket());
        List<BasketProduct> basketProducts = basket.getBasketProducts();
        double sum = basketProducts.stream().mapToDouble(item -> item.getProduct().getSelling() * item.getAmount()).sum();
        model.addAttribute("basketProducts", basketProducts);
        model.addAttribute("sum",sum);

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
                BasketProduct currentProduct;
                if(currentProductId==null){
                    currentProduct = BasketProduct.builder()
                            .product(productRepo.findById(productId).get())
                            .amount(1)
                            .build();
                }else {
                    currentProduct = BasketProduct.builder()
                            .product(productRepo.findById(productId).get())
                            .amount(amount)
                            .build();
                }
                int allAmount;
                if (!basketProducts.isEmpty()){
                    Optional<BasketProduct> first = basketProducts.stream().filter(item -> item.getProduct().getId().equals(productId)).findFirst();
                    if(first.isPresent()){
                        int basketAmount = first.get().getAmount();
                        int baseAmount = incomeRepo.findAllAmountProduct(productId) - saleRepo.findAllAmountByProductId(productId);
                        allAmount = baseAmount-basketAmount;
                    }else {
                        allAmount = incomeRepo.findAllAmountProduct(productId) - saleRepo.findAllAmountByProductId(productId);
                    }

                }else {
                    allAmount = incomeRepo.findAllAmountProduct(productId) - saleRepo.findAllAmountByProductId(productId);
                }
                model.addAttribute("allAmount",allAmount);
                model.addAttribute("currentProduct",currentProduct);
                model.addAttribute("productId",productId);
                model.addAttribute("currentProductId",productId);

            }
            model.addAttribute("categoryId",categoryId);
            model.addAttribute("products",products);
        }
        return "sale";
    }
    @PostMapping("count")
    public String  action(@RequestParam(name = "action") String action,
                          @RequestParam(required = false)UUID productId,
                          @RequestParam(required = false)UUID categoryId,
                          @RequestParam(required = false)Integer amount,
                          @RequestParam(required = false)UUID currentProductId,
                          Model model){
        BasketProduct basketProduct = BasketProduct.builder()
                .product(productRepo.findById(productId).get())
                .amount(amount)
                .build();
        if (action.equals("plus")){
            basketProduct.setAmount(basketProduct.getAmount()+1);
        }else {
            basketProduct.setAmount(basketProduct.getAmount()-1);
        }
        model.addAttribute("currentProduct",basketProduct);
        model.addAttribute("currentProductId",currentProductId);
        model.addAttribute("productId",productId);
        model.addAttribute("categoryId",categoryId);
        return "redirect:/sale?categoryId="+categoryId+"&productId="+productId+"&currentProductId="+currentProductId+"&amount="+(basketProduct.getAmount());
    }

    @PostMapping("addBasket")
    public String addBasket(@RequestParam(required = false)Integer amount,
                            @RequestParam(required = false)UUID currentProductId,
                            HttpSession session){
        Object object = session.getAttribute("basket");
        Basket basket;
        if (object==null){
            basket=new Basket();
            List<BasketProduct> basketProducts=new ArrayList<>();
            basketProducts.add(new BasketProduct(productRepo.findById(currentProductId).get(),amount));
            basket.setBasketProducts(basketProducts);
        }else {
            basket=(Basket) object;
            basket.getBasketProducts().add(new BasketProduct(productRepo.findById(currentProductId).get(),amount));
        }
        session.setAttribute("basket",basket);
        return "redirect:/sale";
    }

    @PostMapping("delete")
    public String delete(@RequestParam(required = false)UUID basketId,HttpSession session,HttpServletResponse response,Model model) throws IOException {
        Basket basket = (Basket) session.getAttribute("basket");
        List<BasketProduct> basketProducts = basket.getBasketProducts();
        List<BasketProduct> newBasketProducts = new ArrayList<>();
        for (BasketProduct basketProduct : basketProducts) {
            if (!basketProduct.getProduct().getId().equals(basketId)){
                newBasketProducts.add(basketProduct);
            }
        }
        Basket basket1 = new Basket();
        basket1.setBasketProducts(newBasketProducts);
        session.removeAttribute("basket");
        session.setAttribute("basket",basket1);
        model.addAttribute("basketProducts",newBasketProducts);
        return "redirect:/sale";
    }

    @PostMapping("makeOrder")
    public String makeOrder(HttpSession session,HttpServletResponse response) throws IOException {
        Basket basket = (Basket)session.getAttribute("basket");
        List<BasketProduct> basketProducts = basket.getBasketProducts();
        List<Sale> sales = new ArrayList<>();

        for (BasketProduct basketProduct : basketProducts) {
            Sale sale = Sale.builder()
                    .product(basketProduct.getProduct())
                    .amount(basketProduct.getAmount())
                    .price(basketProduct.getProduct().getSelling())
                    .build();
            saleRepo.save(sale);
            sales.add(sale);
        }

        double sum = basketProducts.stream().mapToDouble(item -> item.getProduct().getSelling() * item.getAmount()).sum();
        createReceipt(response,sum,basketProducts);
        session.removeAttribute("basket");
        return "redirect:/sale";
    }

    public void createReceipt(HttpServletResponse response, double sum, List<BasketProduct> basketProducts) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=receipt.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        document.setMargins(20, 20, 20, 20);



        document.add(new Paragraph("C H E C K")
                .setBold()
                .setFontSize(20)
                .setMarginBottom(10));

        String logoPath = "/Users/Бахтиёр Садуллоев/Desktop/Java_Projects/modul_8/exam8/src/main/resources/photo/pdp.jpg"; // Logotip faylini haqiqiy yo'lini kiriting
        ImageData logoData = ImageDataFactory.create(logoPath);
        Image logo = new Image(logoData);
        logo.setHorizontalAlignment(HorizontalAlignment.CENTER); // Gorizontal o'rtaga joylash
        document.add(logo);

        float[] columnWidths = {1, 5, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("№", true));
        table.addHeaderCell(createCell("Mahsulot", true));
        table.addHeaderCell(createCell("Miqdor", true));
        table.addHeaderCell(createCell("Narx", true));

        int row = 1;
        for (BasketProduct basketProduct : basketProducts) {
            String summa = String.valueOf(basketProduct.getAmount()*basketProduct.getProduct().getSelling());
            table.addCell(createCell(String.valueOf(row++), false));
            table.addCell(createCell(basketProduct.getProduct().getName(), false));
            table.addCell(createCell(basketProduct.getAmount().toString(), false));
            table.addCell(createCell(summa, false));

        }
        table.addCell(createCell("", false));
        table.addCell(createCell("", false));
        table.addCell(createCell("All summa: ", true));
        table.addCell(createCell(String.valueOf(sum), false));

        document.add(table);

        // QR kod yaratish
        String qrContent = "Bu QR kod mazmuni";
        BufferedImage qrImage = generateQRCodeImage(qrContent, 100, 100);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        ImageData qrImageData = ImageDataFactory.create(baos.toByteArray());
        Image qrCode = new Image(qrImageData);

        document.add(new Paragraph("QR Kod:"));
        qrCode.setHorizontalAlignment(HorizontalAlignment.CENTER); // Gorizontal o'rtaga joylash
        document.add(qrCode);
        document.add(new Paragraph("Data: " + LocalDate.now())
                .setFontSize(12)
                .setMarginBottom(20));

        document.close();
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell();
        cell.add(new Paragraph(content));
        if (isHeader) {
            cell.setBackgroundColor(new DeviceGray(0.75f));
            cell.setBold();
        }
        return cell;
    }

    private BufferedImage generateQRCodeImage(String content, int width, int height) throws IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int black = 0xFF000000;
            int white = 0xFFFFFFFF;

            var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? black : white);
                }
            }
            return bufferedImage;
        } catch (Exception e) {
            throw new IOException("QR kod yaratishda xatolik yuz berdi", e);
        }
    }

}
