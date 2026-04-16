-- =====================================================
-- 덕후감 (Deokhugam) Database Schema
-- PostgreSQL
-- =====================================================

-- =====================================================
-- 사용자 (Users)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
                       id          UUID            NOT NULL DEFAULT gen_random_uuid(),
                       email       VARCHAR(255)    NOT NULL,
                       nickname    VARCHAR(20)     NOT NULL,
                       password    VARCHAR(255)    NOT NULL,
                       created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at  TIMESTAMP       NULL,

                       CONSTRAINT pk_users
                           PRIMARY KEY (id),
                       CONSTRAINT uq_users_email
                           UNIQUE (email),
                       CONSTRAINT chk_users_nickname_length
                           CHECK (char_length(nickname) >= 2)
);

-- =====================================================
-- 도서 (Books)
-- =====================================================
CREATE TABLE IF NOT EXISTS books (
                       id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                       title           VARCHAR(500)    NOT NULL,
                       author          VARCHAR(255)    NOT NULL,
                       description     TEXT            NOT NULL,
                       publisher       VARCHAR(255)    NOT NULL,
                       published_date  DATE            NOT NULL,
                       isbn            VARCHAR(20)     NULL,           -- optional, 13자리 ISBN
                       thumbnail_url   VARCHAR(2048)   NULL,
                       review_count    INT             NOT NULL DEFAULT 0,
                       rating          DECIMAL(3, 2)   NOT NULL DEFAULT 0.0,  -- 리뷰의 평점의 평균으로
                       created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at      TIMESTAMP       NULL,

                       CONSTRAINT pk_books
                           PRIMARY KEY (id),
                       CONSTRAINT uq_books_isbn
                           UNIQUE (isbn),                              -- ISBN 중복 불가 (NULL은 중복 허용)
                       CONSTRAINT chk_books_rating
                           CHECK (rating >= 0.0 AND rating <= 5.0),
                       CONSTRAINT chk_books_review_count
                           CHECK (review_count >= 0)
);

-- =====================================================
-- 리뷰 (Reviews)
-- =====================================================
CREATE TABLE IF NOT EXISTS reviews (
                         id              UUID        NOT NULL DEFAULT gen_random_uuid(),
                         book_id         UUID        NOT NULL,
                         user_id         UUID        NOT NULL,
                         content         TEXT        NOT NULL,
                         rating          INT         NOT NULL,
                         like_count      INT         NOT NULL DEFAULT 0,
                         comment_count   INT         NOT NULL DEFAULT 0,
                         created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at      TIMESTAMP   NULL,

                         CONSTRAINT pk_reviews
                             PRIMARY KEY (id),
                         CONSTRAINT fk_reviews_book
                             FOREIGN KEY (book_id) REFERENCES books (id),
                         CONSTRAINT fk_reviews_user
                             FOREIGN KEY (user_id) REFERENCES users (id),
                         CONSTRAINT uq_reviews_book_user
                             UNIQUE (book_id, user_id),                  -- 유니크 설정으로 도서당 1개의 리뷰 제한
                         CONSTRAINT chk_reviews_rating
                             CHECK (rating >= 1 AND rating <= 5),
                         CONSTRAINT chk_reviews_like_count
                             CHECK (like_count >= 0),
                         CONSTRAINT chk_reviews_comment_count
                             CHECK (comment_count >= 0)
);

-- =====================================================
-- 리뷰 좋아요 (Review Likes)
-- =====================================================
CREATE TABLE IF NOT EXISTS review_likes (
                              id          UUID        NOT NULL DEFAULT gen_random_uuid(),
                              review_id   UUID        NOT NULL,
                              user_id     UUID        NOT NULL,
                              created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT pk_review_likes
                                  PRIMARY KEY (id),
                              CONSTRAINT fk_review_likes_review
                                  FOREIGN KEY (review_id) REFERENCES reviews (id),
                              CONSTRAINT fk_review_likes_user
                                  FOREIGN KEY (user_id) REFERENCES users (id),
                              CONSTRAINT uq_review_likes_review_user
                                  UNIQUE (review_id, user_id)                 -- 유니크로 리뷰당 사용자 1회 좋아요
);

-- =====================================================
-- 댓글 (Comments) (리뷰의 댓글)
-- =====================================================
CREATE TABLE IF NOT EXISTS comments (
                          id          UUID        NOT NULL DEFAULT gen_random_uuid(),
                          review_id   UUID        NOT NULL,
                          user_id     UUID        NOT NULL,
                          content     TEXT        NOT NULL,
                          created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at  TIMESTAMP   NULL,

                          CONSTRAINT pk_comments
                              PRIMARY KEY (id),
                          CONSTRAINT fk_comments_review
                              FOREIGN KEY (review_id) REFERENCES reviews (id),
                          CONSTRAINT fk_comments_user
                              FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =====================================================
-- 알림 (Notifications)
-- =====================================================
-- type: LIKE(좋아요), COMMENT(댓글), RANKING(인기 리뷰 순위 진입)
-- sender_id: 좋아요/댓글 발송자 ID, 랭킹 알림은 NULL
-- user_id: 알림 수신자 (리뷰 작성자)
CREATE TABLE IF NOT EXISTS notifications (
                               id              UUID        NOT NULL DEFAULT gen_random_uuid(),
                               user_id         UUID        NOT NULL,
                               sender_id       UUID        NULL,               -- 랭킹 알림은 NULL
                               review_id       UUID        NOT NULL,
                               type            VARCHAR(10) NOT NULL,           -- LIKE | COMMENT | RANKING
                               confirmed       BOOLEAN     NOT NULL DEFAULT FALSE,
                               created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT pk_notifications
                                   PRIMARY KEY (id),
                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id) REFERENCES users (id)
                                   ON DELETE CASCADE,
                               CONSTRAINT fk_notifications_sender
                                   FOREIGN KEY (sender_id) REFERENCES users (id)
                                   ON DELETE SET NULL,
                               CONSTRAINT fk_notifications_review
                                   FOREIGN KEY (review_id) REFERENCES reviews (id)
                                   ON DELETE CASCADE,
                               CONSTRAINT chk_notifications_type
                                   CHECK (type IN ('LIKE', 'COMMENT', 'RANKING'))
);

-- =====================================================
-- 인기 도서 (Popular Books) - 배치 결과 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS popular_books (
                               id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                               book_id         UUID            NOT NULL,
                               period          VARCHAR(10)     NOT NULL,       -- DAILY | WEEKLY | MONTHLY | ALL_TIME
                               rank            BIGINT          NOT NULL,
                               score           DECIMAL(10, 4)  NOT NULL,
                               review_count    BIGINT          NOT NULL DEFAULT 0,
                               rating          DECIMAL(3, 2)   NOT NULL DEFAULT 0.0,
                               created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT pk_popular_books
                                   PRIMARY KEY (id),
                               CONSTRAINT fk_popular_books_book
                                   FOREIGN KEY (book_id) REFERENCES books (id),
                               CONSTRAINT chk_popular_books_period
                                   CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
                               CONSTRAINT chk_popular_books_rank
                                   CHECK (rank > 0),
                               CONSTRAINT chk_popular_books_rating
                                   CHECK (rating >= 0.0 AND rating <= 5.0)
);

-- =====================================================
-- 인기 리뷰 (Popular Reviews) - 배치 결과 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS popular_reviews (
                                 id              UUID            NOT NULL DEFAULT gen_random_uuid(),
                                 review_id       UUID            NOT NULL,
                                 period          VARCHAR(10)     NOT NULL,       -- DAILY | WEEKLY | MONTHLY | ALL_TIME
                                 rank            BIGINT          NOT NULL,
                                 score           DECIMAL(10, 4)  NOT NULL,
                                 like_count      BIGINT          NOT NULL DEFAULT 0,
                                 comment_count   BIGINT          NOT NULL DEFAULT 0,
                                 created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT pk_popular_reviews
                                     PRIMARY KEY (id),
                                 CONSTRAINT fk_popular_reviews_review
                                     FOREIGN KEY (review_id) REFERENCES reviews (id),
                                 CONSTRAINT chk_popular_reviews_period
                                     CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
                                 CONSTRAINT chk_popular_reviews_rank
                                     CHECK (rank > 0)
);

-- =====================================================
-- 파워 유저 (Power Users) - 배치 결과 저장
-- =====================================================
CREATE TABLE IF NOT EXISTS power_users (
                             id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
                             user_id             UUID            NOT NULL,
                             period              VARCHAR(10)     NOT NULL,   -- DAILY | WEEKLY | MONTHLY | ALL_TIME
                             rank                BIGINT          NOT NULL,
                             score               DECIMAL(10, 4)  NOT NULL,
                             review_score_sum    DECIMAL(10, 4)  NOT NULL DEFAULT 0.0,
                             like_count          BIGINT          NOT NULL DEFAULT 0,
                             comment_count       BIGINT          NOT NULL DEFAULT 0,
                             created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT pk_power_users
                                 PRIMARY KEY (id),
                             CONSTRAINT fk_power_users_user
                                 FOREIGN KEY (user_id) REFERENCES users (id),
                             CONSTRAINT chk_power_users_period
                                 CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
                             CONSTRAINT chk_power_users_rank
                                 CHECK (rank > 0)
);

-- =====================================================
-- 인덱스 (Indexes)
-- =====================================================

-- users
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email); -- 로그인 시에 이메일로 조회
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users (deleted_at) WHERE deleted_at IS NOT NULL;

-- books
CREATE INDEX IF NOT EXISTS idx_books_isbn ON books (isbn) WHERE isbn IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_books_title ON books (title);
CREATE INDEX IF NOT EXISTS idx_books_deleted_at ON books (deleted_at) WHERE deleted_at IS NOT NULL;

-- reviews
CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews (book_id); -- 특정 도서의 리뷰 목록 조회
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews (user_id); -- 특정 사용자의 리뷰 목록 조회
CREATE INDEX IF NOT EXISTS idx_reviews_deleted_at ON reviews (deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews (created_at); -- 커서 페이지네이션용
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews (rating);

-- review_likes
CREATE INDEX IF NOT EXISTS idx_review_likes_review_id ON review_likes (review_id);
CREATE INDEX IF NOT EXISTS idx_review_likes_user_id ON review_likes (user_id);

-- comments
CREATE INDEX IF NOT EXISTS idx_comments_review_id ON comments (review_id); --특정 리뷰의 댓글 목록 조회
CREATE INDEX IF NOT EXISTS idx_comments_deleted_at ON comments (deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments (created_at); -- 커서 페이지네이션용

-- notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id); -- 본인 알림 목록 조회
CREATE INDEX IF NOT EXISTS idx_notifications_confirmed ON notifications (confirmed);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications (created_at);

-- popular_books
CREATE INDEX IF NOT EXISTS idx_popular_books_period_rank ON popular_books (period, rank);  -- 커서페이지네이션용
CREATE INDEX IF NOT EXISTS idx_popular_books_created_at ON popular_books (created_at);

-- popular_reviews
CREATE INDEX IF NOT EXISTS idx_popular_reviews_period_rank ON popular_reviews (period, rank); -- 커서페이지네이션용
CREATE INDEX IF NOT EXISTS idx_popular_reviews_created_at ON popular_reviews (created_at);

-- power_users
CREATE INDEX IF NOT EXISTS idx_power_users_period_rank ON power_users (period, rank); -- 커서페이지네이션용
CREATE INDEX IF NOT EXISTS idx_power_users_created_at ON power_users (created_at);
