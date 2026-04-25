package com.example.photodiary.service;

import com.example.photodiary.controller.JsonConverter;
import com.example.photodiary.model.DiaryPost;
import com.example.photodiary.model.PostImage;
import com.example.photodiary.model.PrintOrder;
import com.example.photodiary.repository.DiaryPostRepository;
import com.example.photodiary.repository.PrintOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.time.LocalDateTime;
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
    public void saveOrderRequest(String address, List<Long> selectedPostIds) {
        PrintOrder order = new PrintOrder();
        order.setShippingAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        // 선택된 ID 리스트를 String 형태로 변환하여 저장 (예: "[1,3]")
        try {
            String details = objectMapper.writeValueAsString(selectedPostIds);
            order.setOrderDetails(details);
        } catch (JsonProcessingException e) {
            order.setOrderDetails(selectedPostIds.toString());
        }

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
                addImagesToZip(post, zos);
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

    private void addImagesToZip(DiaryPost post, ZipOutputStream zos) {
        try {
            // 1. 게시글에 속한 모든 이미지 리스트를 가져옴
            List<PostImage> postImages = post.getImages();

            if (postImages == null || postImages.isEmpty()) {
                return; // 이미지가 없으면 건너뜀
            }

            int imageIndex = 1;
            for (PostImage pi : postImages) {
                String imageUrl = pi.getImageUrl();
                byte[] imageBytes = null;

                if (imageUrl != null && imageUrl.startsWith("/images/")) {
                    // 2. 로컬 저장소(/app/uploads)에서 파일 읽기
                    String fileName = imageUrl.replace("/images/", "");
                    Path filePath = Paths.get(uploadDir).resolve(fileName);

                    if (Files.exists(filePath)) {
                        imageBytes = Files.readAllBytes(filePath);
                    } else {
                        System.err.println("⚠️ 파일을 찾을 수 없음: " + filePath);
                        continue; // 다음 이미지로 진행
                    }
                } else if (imageUrl != null) {
                    // 3. 외부 URL 처리 (하이브리드 대비)
                    try {
                        URL url = new URL(imageUrl);
                        imageBytes = StreamUtils.copyToByteArray(url.openStream());
                    } catch (Exception e) {
                        System.err.println("⚠️ 외부 이미지 로드 실패: " + imageUrl);
                        continue;
                    }
                }

                if (imageBytes != null) {
                    // 4. 압축 파일 내 경로 설정
                    // 예: images/post_10/image_1.jpg (게시글별로 폴더를 나누면 더 깔끔합니다)
                    String extension = "jpg"; // 기본값
                    if (imageUrl.contains(".")) {
                        extension = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
                    }

                    String entryName = "images/post_" + post.getId() + "/img_" + imageIndex + "." + extension;
                    ZipEntry entry = new ZipEntry(entryName);

                    zos.putNextEntry(entry);
                    zos.write(imageBytes);
                    zos.closeEntry();

                    imageIndex++;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 이미지 압축 중 오류 발생 (Post ID " + post.getId() + "): " + e.getMessage());
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