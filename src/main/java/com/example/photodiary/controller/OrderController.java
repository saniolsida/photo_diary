package com.example.photodiary.controller;

import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.model.PrintOrder;
import com.example.photodiary.repository.DiaryPostRepository;
import com.example.photodiary.repository.PrintOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequiredArgsConstructor
public class OrderController {

    // Repository 주입 (RequiredArgsConstructor가 생성자를 만들어줍니다)
    private final DiaryPostRepository diaryPostRepository;
    private final PrintOrderRepository printOrderRepository;

    // JSON 변환을 위한 객체
    private final ObjectMapper objectMapper;

    @GetMapping("/order")
    public String orderList(Model model) {
        List<PrintOrder> orders = printOrderRepository.findAll();
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @PostMapping("/order/request")
    public String createOrder(@RequestParam("shippingAddress") String address) {
        // DB에서 모든 게시글의 ID만 추출
        List<Long> postIds = diaryPostRepository.findAll().stream()
                .map(DiaryPost::getId)
                .toList();

        // ID 리스트만 JSON화 (예: "[1, 2, 3]")
        String details = JsonConverter.toJson(postIds);

        PrintOrder order = new PrintOrder();
        order.setOrderDetails(details);
        order.setShippingAddress(address);
        order.setStatus("PENDING");
        printOrderRepository.save(order);

        return "redirect:/?orderSuccess=true";
    }

    @GetMapping("/order/process/{id}")
    public String processOrder(@PathVariable Long id) {
        PrintOrder order = printOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역을 찾을 수 없습니다. id=" + id));

        // 1. 상태를 PROCESSING으로 변경
        // 이렇게 하면 목록 페이지로 돌아갔을 때 {{#isProcessing}} 블록이 실행됩니다.
        order.setStatus("PROCESSING");
        printOrderRepository.save(order);

        // 2. 다시 주문 목록 페이지로 리다이렉트
        return "redirect:/order";
    }

    @GetMapping("/order/download/{id}")
    public void downloadOrderZip(@PathVariable Long id, HttpServletResponse response) throws IOException {
        // 1. 프로세싱 체감을 위한 딜레이
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

        PrintOrder order = printOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음: " + id));

        // 응답 설정
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"order_" + id + ".zip\"");

        // try-with-resources 내부에서는 '압축 및 전송'만 수행
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("orderId", order.getId());
            exportData.put("shippingAddress", order.getShippingAddress());

            List<Map<String, Object>> postInfoList = new ArrayList<>();
            List<Long> postIds = JsonConverter.toLongList(order.getOrderDetails());

            for (Long postId : postIds) {
                diaryPostRepository.findById(postId).ifPresent(post -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", post.getId());
                    info.put("title", post.getTitle());
                    info.put("author", post.getAuthor());
                    info.put("content", post.getContent());
                    info.put("createdAt", post.getCreatedAt().toString());
                    postInfoList.add(info);

                    try {
                        URL url = new URL(post.getImageUrl());
                        byte[] imageBytes = StreamUtils.copyToByteArray(url.openStream());
                        ZipEntry imageEntry = new ZipEntry("images/post_" + post.getId() + ".jpg");
                        zos.putNextEntry(imageEntry);
                        zos.write(imageBytes);
                        zos.closeEntry();
                    } catch (Exception ignored) {}
                });
            }
            exportData.put("posts", postInfoList);

            // JSON 추가
            ZipEntry jsonEntry = new ZipEntry("full_order_info.json");
            zos.putNextEntry(jsonEntry);
            String fullJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
            zos.write(fullJson.getBytes());
            zos.closeEntry();

            // 스트림 마감
            zos.finish();
            zos.flush();
        } catch (Exception e) {
            System.err.println("압축 중 오류: " + e.getMessage());
            return;
        }

        // ★ 중요: 스트림이 완전히 닫힌(try 블록 밖) 여기서 상태 변경
        try {
            order.setStatus("COMPLETED");
            printOrderRepository.saveAndFlush(order);
        } catch (Exception e) {
            System.err.println("상태 변경 실패: " + e.getMessage());
        }
    }
}