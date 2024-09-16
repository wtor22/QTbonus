package com.quartztop.bonus.controllers;

import com.quartztop.bonus.crm.*;
import com.quartztop.bonus.orders.Order;
import com.quartztop.bonus.repositoriesBonus.OrderRepository;
import com.quartztop.bonus.repositoriesCrm.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequestMapping("/order")
@AllArgsConstructor
@Slf4j
public class OrderController {

    private InvoicesRepository invoicesRepository;
    private ProductRepositories productRepositories;
    private OrderRepository orderRepository;
    private PositionInvoiceRepository positionInvoiceRepository;
    private BundleRepository bundleRepository;
    private AgentRepository agentRepository;

    @GetMapping("/validate-agent")
    @ResponseBody
    public ResponseEntity<String> validateAgent(@RequestParam("innNumber") String inn) {

        if(agentRepository.findByInn(inn).isPresent())
            return ResponseEntity.ok("valid");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Контрагент с учетным номером " + inn
                + " не найден");
    }

    @GetMapping("/validate-invoice")
    @ResponseBody
    public ResponseEntity<String> validateInvoice(
            @RequestParam("agentInn") String inn,
            @RequestParam("invoiceNumber") String invoiceNumber,
            @RequestParam("invoiceDate") LocalDate invoiceDate) {

        LocalDateTime startDate = invoiceDate.atStartOfDay();
        LocalDateTime endDate = invoiceDate.atTime(LocalTime.MAX);

//        List<Invoices> invoicesList = invoicesRepository.findByNameAndMomentBetween(invoiceNumber,
//                startDate, endDate);

        List<Invoices> invoicesList = invoicesRepository.findByNameAndAgentInnAndMomentBetween(invoiceNumber, inn,
                startDate,endDate);


        invoicesList.forEach(o -> log.info(o.getName()));

        if (invoicesList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Счет с номером " + invoiceNumber
                    + " от " + invoiceDate + " не найден");
        } else if (invoicesList.size() > 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Найдено более одного счёта");
        } else {
            Invoices invoices = invoicesList.get(0);
            String externalInvoiceId = invoices.getExternalId();
            return ResponseEntity.ok(externalInvoiceId);
        }
    }

    @GetMapping("/search-products")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam("query") String query) {
        List<Product> products = productRepositories.findByDescriptionContainingIgnoreCaseAndTypeProduct(query,"product");

        List<ProductDto> productDtoList = products.stream()
                .map(product -> new ProductDto(product.getProductId(), product.getExternalId(), product.getName()))
                .toList();

        List<String> productsName = products.stream().map(Product::getName).toList();
        return ResponseEntity.ok(productDtoList);
    }
    @GetMapping("/search-products-in-invoice")
    @ResponseBody
    public ResponseEntity<String> searchProductsInInvoice(@RequestParam("productName") String productName,
                                                                    @RequestParam("invoiceExternalId") String invoiceExternalId) {

        List<InvoicePosition> positions = positionInvoiceRepository.findAllByExternalInvoiceId(invoiceExternalId);

        if (positions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Указанный товар в счете не найден");
        }

        Set<String> listProductsNames = new HashSet<>();

        for(InvoicePosition position: positions) {
            String productExternalId = position.getExternalProductId();
            Product product = productRepositories.findByExternalId(productExternalId);

            if(position.getProductType().equals("product")) {
                listProductsNames.add(product.getName());
            }

            if(position.getProductType().equals("bundle")) {
                Bundle bundle = bundleRepository.findByProduct(product);
                Product parentProduct = bundle.getProductParent();
                listProductsNames.add(parentProduct.getName());
            }
        }

        if(listProductsNames.contains(productName)){
            return ResponseEntity.ok().body("OK");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Товары в счете не найдены");
    }


    @GetMapping("/search-quantity-products-in-invoice")
    @ResponseBody
    public ResponseEntity<String> searchProductsQuantityInInvoice(@RequestParam("productExternalId") String productExternalId,
                                                          @RequestParam("productQuantity") Double productQuantity,
                                                          @RequestParam("invoiceExternalId") String invoiceExternalId) {

        List<InvoicePosition> positions = positionInvoiceRepository.findAllByExternalInvoiceId(invoiceExternalId);

        Map<String, Double> mapPositions = new HashMap<>();

        // TODO Удалить потом
        if(positions.stream().anyMatch(p -> p.getExternalProductId().equals(productExternalId))) {
            log.error("Содержится элемент в счете");
        }

        double sumForProduct = 0;
        double quantityProduct = 0;

        for(InvoicePosition position: positions) {

            if(position.getProductType().equals("product") && position.getExternalProductId().equals(productExternalId)) {
                // Количество товара
                quantityProduct = quantityProduct + position.getQuantity();
                // Стоимость товара
                if(position.getDiscount() > 0) {
                    sumForProduct = sumForProduct + position.getPrice() * ( 1 -  position.getDiscount() / 100);
                } else {
                    sumForProduct = sumForProduct + position.getPrice();
                }
            } else if (position.getProductType().equals("bundle")) {

                Bundle bundle = bundleRepository.findByExternalId(position.getExternalProductId());
                Product product = bundle.getProductParent();
                if(product.getExternalId().equals(productExternalId)) {
                    // Количество товара
                    quantityProduct = quantityProduct + bundle.getQuantity() * position.getQuantity();
                    // Стоимость товара
                    if(position.getDiscount() > 0) {
                        sumForProduct = sumForProduct + position.getPrice() * ( 1 -  position.getDiscount() / 100);
                    } else {
                        sumForProduct = sumForProduct + position.getPrice();
                    }
                }
            }
        }

        // Количество товара в предыдущих ордерах
        List<Order> orders = orderRepository.getOrdersByInvoiceExternalId(invoiceExternalId);
        double quantityFromOldOrders = 0;
        if(!orders.isEmpty()) {
            quantityFromOldOrders = quantityFromOldOrders + orders.stream().mapToDouble(Order::getProductQuantity).sum();
        }


        // Стоимость товара по средней цене за товар
        double price = 0;
        double bonus = 3.00; // 3% Бонус
        if(quantityProduct - quantityFromOldOrders > 0) {
            price = sumForProduct / quantityProduct / 100 * productQuantity * bonus / 100;
        }

        String priceToString = String.valueOf(price);
        log.info("OLD ORDERS QUANTITY   " + quantityFromOldOrders);
        log.info("PRICE  " + price);
        if(productQuantity <= quantityProduct - quantityFromOldOrders) {
            return ResponseEntity.status(HttpStatus.OK).body(priceToString);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Товары в счете не найдены");
    }
}
