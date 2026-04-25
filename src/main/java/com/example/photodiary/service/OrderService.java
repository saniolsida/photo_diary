package com.example.photodiary.service;

import com.example.photodiary.controller.JsonConverter;
import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.model.PrintOrder;
import com.example.photodiary.repository.DiaryPostRepository;
import com.example.photodiary.repository.PrintOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final DiaryPostRepository diaryPostRepository;
    private final PrintOrderRepository printOrderRepository;
    private final ObjectMapper objectMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public void saveOrderRequest(String address) {
        // 1. 비즈니스 로직: 게시글 ID 추출 및 변환
        List<Long> postIds = diaryPostRepository.findAll().stream()
                .map(DiaryPost::getId)
                .toList();
        String details = JsonConverter.toJson(postIds);

        // 2. 엔티티 생성 및 저장
        PrintOrder order = new PrintOrder();
        order.setOrderDetails(details);
        order.setShippingAddress(address);
        order.setStatus("PENDING");
        printOrderRepository.save(order);
    }

    @Transactional
    public void updateStatusToProcessing(Long id) {
        PrintOrder order = printOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역 없음: " + id));

        order.setStatus("PROCESSING");
        printOrderRepository.save(order);
    }

    @Transactional
    public void createOrderZip(PrintOrder order, ZipOutputStream zos) throws IOException {
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("orderId", order.getId());
        exportData.put("shippingAddress", order.getShippingAddress());

        List<Map<String, Object>> postInfoList = new ArrayList<>();
        List<Long> postIds = JsonConverter.toLongList(order.getOrderDetails());

        for (Long postId : postIds) {
            diaryPostRepository.findById(postId).ifPresent(post -> {
                // 1. 데이터 매핑 로직 분리
                postInfoList.add(convertToMap(post));
                // 2. 이미지 압축 로직 분리
                addThumbnailToZip(post, zos);
            });
        }
        exportData.put("posts", postInfoList);

        // 3. JSON 파일 생성 로직 분리
        addJsonToZip(exportData, zos);
    }

    private Map<String, Object> convertToMap(DiaryPost post) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", post.getId());
        info.put("title", post.getTitle());
        info.put("author", post.getAuthor());
        info.put("content", post.getContent());
        info.put("createdAt", post.getCreatedAt().toString());
        return info;
    }

    private void addThumbnailToZip(DiaryPost post, ZipOutputStream zos) {
        try {
            String imageUrl = post.getImageUrl();
            byte[] imageBytes;

            if (imageUrl.startsWith("/images/")) {
                // 1. 로컬 저장소(/app/uploads)에서 파일을 읽는 경우
                String fileName = imageUrl.replace("/images/", "");
                Path filePath = Paths.get(uploadDir).resolve(fileName);

                if (Files.exists(filePath)) {
                    imageBytes = Files.readAllBytes(filePath);
                } else {
                    System.err.println("⚠️ 파일을 찾을 수 없음: " + filePath);
                    return;
                }
            } else {
                // 2. 만약 기존의 외부 URL 데이터가 남아있을 경우를 대비한 하이브리드 처리
                URL url = new URL(imageUrl);
                imageBytes = StreamUtils.copyToByteArray(url.openStream());
            }

            // 압축 파일 내부에 이미지 저장
            ZipEntry entry = new ZipEntry("images/post_" + post.getId() + ".jpg");
            zos.putNextEntry(entry);
            zos.write(imageBytes);
            zos.closeEntry();

        } catch (Exception e) {
            System.err.println("❌ 이미지 압축 중 오류 발생 (ID " + post.getId() + "): " + e.getMessage());
        }
    }

    private void addJsonToZip(Object data, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry("full_order_info.json");
        zos.putNextEntry(entry);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        zos.write(json.getBytes());
        zos.closeEntry();
    }

    @Transactional
    public void completeOrder(PrintOrder order) {
        try {
            order.setStatus("COMPLETED");
            printOrderRepository.saveAndFlush(order);
            System.out.println("주문 상태 변경 완료: ID " + order.getId());
        } catch (Exception e) {
            System.err.println("상태 변경 중 오류 발생: " + e.getMessage());
            throw e;
        }
    }
}