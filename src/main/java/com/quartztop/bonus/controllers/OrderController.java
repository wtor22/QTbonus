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

        if(!agentRepository.findAllByInn(inn).isEmpty())
            return ResponseEntity.ok("valid");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Контрагент с учетным номером " + inn
                + " не найден");
    }
    @GetMapping("/validate-agent-by-name")
    @ResponseBody
    public ResponseEntity<String> validateAgentByName(@RequestParam("fullNameAgent") String fullNameAgent) {

        List<Agent> agentsList = agentRepository.findAllByName(fullNameAgent);
        if(!agentsList.isEmpty()) {
            return ResponseEntity.ok("valid");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Контрагент: " + fullNameAgent
                + " не найден");
    }

    @GetMapping("/validate-invoice")
    @ResponseBody
    public ResponseEntity<String> validateInvoice(
            @RequestParam("agentInn") String inn,
            @RequestParam("agentFullName") String agentFullName,
            @RequestParam("invoiceNumber") String invoiceNumber,
            @RequestParam("invoiceDate") LocalDate invoiceDate) {

        LocalDateTime startDate = invoiceDate.atStartOfDay();
        LocalDateTime endDate = invoiceDate.atTime(LocalTime.MAX);

        List<Invoices> invoicesList;

        if(agentFullName.equals("null")) {
            invoicesList = invoicesRepository.findByNameAndAgentInnAndMomentBetween(invoiceNumber, inn,
                    startDate,endDate);
        } else {
            List<Integer> agentsId = agentRepository.findAgentIdsByFullName(agentFullName);
            invoicesList = invoicesRepository.findByAgentsIdAndMomentBetween(agentsId,startDate,endDate);
        }

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
        //log.info("Search by STRING " + query);
        List<Product> products = productRepositories.searchByDescriptionOrArticleAndType(query,"product");
        List<ProductDto> productDtoList = products.stream()
                .filter(product -> product.getPathName().startsWith("Кварцевый Агломерат") ||
                        product.getPathName().startsWith("Керамогранит"))
                .map(product -> new ProductDto(product.getProductId(), product.getExternalId(), product.getName()))
                .toList();
        //products.forEach(p->log.info(p.getName()));
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

        log.info("PRODUCT EXTERNAL ID " + productExternalId);
        log.info("PRODUCT QUANTITY " + productQuantity);
        log.info("INVOICE EXTERNAL ID " + invoiceExternalId);

        List<InvoicePosition> positions = positionInvoiceRepository.findAllByExternalInvoiceId(invoiceExternalId);

        double sumForProduct = 0;
        double quantityProduct = 0;

        for (InvoicePosition position : positions) {

            if (!position.getExternalProductId().equals(productExternalId) && position.getProductType().equals("product")) continue;

            if (position.getProductType().equals("product")) {
                // Количество товара
                quantityProduct = quantityProduct + position.getQuantity();
                // Стоимость товара
                if (position.getDiscount() > 0) {
                    sumForProduct = (sumForProduct + position.getPrice() * (1 - position.getDiscount() / 100))
                            * position.getQuantity();
                } else {
                    sumForProduct = (sumForProduct + position.getPrice())
                            * position.getQuantity();
                }
            } else if (position.getProductType().equals("bundle")) {

                Bundle bundle = bundleRepository.findByExternalId(position.getExternalProductId());
                Product product = bundle.getProductParent();
                if (!product.getExternalId().equals(productExternalId)) continue;


                // Количество товара
                quantityProduct = quantityProduct + bundle.getQuantity() * position.getQuantity();
                // Стоимость товара
                if (position.getDiscount() > 0) {
                    sumForProduct = (sumForProduct + position.getPrice() * (1 - position.getDiscount() / 100))
                            * position.getQuantity();
                } else {
                    sumForProduct = (sumForProduct + position.getPrice())
                            * position.getQuantity();
                }
            }

            // Количество товара в предыдущих ордерах
            List<Order> orders = orderRepository.getOrdersByInvoiceExternalIdAndProductExternalId(invoiceExternalId, productExternalId);
            double quantityFromOldOrders = 0;
            if (!orders.isEmpty()) {
                quantityFromOldOrders = quantityFromOldOrders + orders.stream().mapToDouble(Order::getProductQuantity).sum();
            }

            // Стоимость товара по средней цене за товар
            int price = 0;
            double bonus = 3.00; // 3% Бонус
            if (quantityProduct - quantityFromOldOrders > 0) {

                price = (int) (sumForProduct / quantityProduct / 100 * productQuantity * bonus / 100);
            }

            String priceToString = String.valueOf(price);
            if (productQuantity <= quantityProduct - quantityFromOldOrders) {
                return ResponseEntity.status(HttpStatus.OK).body(priceToString);
            }

        }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Товары в счете не найдены");
    }
}
