-- 1. 게시글: tag_list 컬럼을 제거하고 인서트
INSERT INTO diary_post (author, title, content, mood, tag, created_at)
SELECT 'admin', '나의 첫 포토 다이어리', '과제를 시작하며 남기는 첫 번째 기록입니다.', '😊', '일상', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM diary_post WHERE title = '나의 첫 포토 다이어리');

INSERT INTO diary_post (author, title, content, mood, tag, created_at)
SELECT 'admin', '여행의 추억', '빨리 과제 끝내고 여행 가고 싶네요!', '✈️', '여행',NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM diary_post WHERE title = '여행의 추억');

-- 2. 이미지 연결 (이전과 동일)
INSERT INTO post_image (post_id, image_url)
SELECT (SELECT id FROM diary_post WHERE title = '나의 첫 포토 다이어리' LIMIT 1), '/images/sample1.jpg'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM post_image WHERE image_url = '/images/sample1.jpg');

INSERT INTO post_image (post_id, image_url)
SELECT (SELECT id FROM diary_post WHERE title = '여행의 추억' LIMIT 1), '/images/sample3.jpeg'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM post_image WHERE image_url = '/images/sample3.jpeg');

INSERT INTO post_image (post_id, image_url)
SELECT (SELECT id FROM diary_post WHERE title = '여행의 추억' LIMIT 1), '/images/sample2.jpeg'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM post_image WHERE image_url = '/images/sample2.jpeg');