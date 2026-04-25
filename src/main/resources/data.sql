-- 첫 번째 데이터
MERGE INTO diary_post (author, title, content, image_url, created_at)
    KEY(title)
    VALUES ('admin', '나의 첫 포토 다이어리', '과제를 시작하며 남기는 첫 번째 기록입니다.', '/images/example1.jpg', NOW());

-- 두 번째 데이터
MERGE INTO diary_post (author, title, content, image_url, created_at)
    KEY(title)
    VALUES ('admin', '여행의 추억', '빨리 과제 끝내고 여행 가고 싶네요!', '/images/example2.jpeg', NOW());