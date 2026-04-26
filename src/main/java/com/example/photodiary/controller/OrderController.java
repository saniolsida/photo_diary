package com.example.photodiary.controller;

import com.example.photodiary.model.PrintOrder;
import com.example.photodiary.repository.PrintOrderRepository;
import com.example.photodiary.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipOutputStream;

import static java.lang.Thread.sleep;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PrintOrderRepository printOrderRepository;

    @GetMapping("/order")
    public String orderList(Model model) {
        List<PrintOrder> orders = printOrderRepository.findAll();
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @PostMapping("/order/request")
    public String createOrder(
            @RequestParam("shippingAddress") String address,
            @RequestParam("receiverName") String name,
            @RequestParam("phoneNumber") String number,
            @RequestParam(value = "selectedPostIds", required = false) List<Long> selectedPostIds // 이 부분 추가!
    ) {
        if (selectedPostIds == null || selectedPostIds.isEmpty()) {
            // 선택된 게 없으면 메인으로 튕기기 (혹은 에러 처리)
            return "redirect:/?error=noSelection";
        }
        orderService.saveOrderRequest(address, name, number, selectedPostIds);

        return "redirect:/?orderSuccess=true";
    }

    @GetMapping("/order/process/{id}")
    public String processOrder(@PathVariable Long id) {
        orderService.updateStatusToProcessing(id);
        return "redirect:/order";
    }

    @GetMapping("/order/download/{id}")
    public void downloadOrderZip(@PathVariable Long id, HttpServletResponse response) throws IOException, InterruptedException {
        // 1. 흐름 제어
        sleep(1000); // 프로세싱을 시각적으로 보기 위한 딜레이
        PrintOrder order = printOrderRepository.findById(id).orElseThrow();

        // 2. 헤더 설정
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"order_" + id + ".zip\"");

        // 3. 서비스 호출
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            orderService.createOrderZip(order, zos);
            zos.finish();
        }
        // 4. 상태 완료 처리
        orderService.completeOrder(order);
    }

    @PostMapping("/order/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id) {
        printOrderRepository.deleteById(id);
        return "redirect:/order"; // 다시 관리 페이지로 리다이렉트
    }
}