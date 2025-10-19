package kuke.board.article.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuke.board.article.entity.Article;
import kuke.board.common.snowflake.Snowflake;

@SpringBootTest
public class DataInitializer {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    // 분산 시스템에서 고유 ID 생성을 위한 Snowflake 알고리즘
    Snowflake snowflake = new Snowflake();

    // EXECUTE_COUNT만큼의 작업 완료를 기다리기 위한 동기화 장치
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    // 한 번의 insert() 호출당 생성할 Article 개수
    static final int BULK_INSERT_SIZE = 1000;

    // insert() 메서드를 실행할 총 횟수 (총 데이터: EXECUTE_COUNT * BULK_INSERT_SIZE = 500만 건)
    static final int EXECUTE_COUNT = 5000;

    @Test
    void initialize() throws InterruptedException {
        // 고정 크기(10개) 스레드 풀 생성
        // - 최대 10개의 스레드만 사용하며 자동으로 증가하지 않음
        // - 작업이 큐에 쌓이더라도 스레드는 10개로 고정
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // EXECUTE_COUNT(5000)번 작업 제출
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            executorService.submit(() -> {
                insert();
                latch.countDown();
                System.out.println("Thread: " + Thread.currentThread().getName()
                    + " | latch.getCount() = " + latch.getCount());
            });
        }

        // 모든 작업이 완료될 때까지 대기 (카운트가 0이 될 때까지)
        latch.await();

        // 스레드 풀 종료 (새 작업 거부, 기존 작업 완료 후 종료)
        executorService.shutdown();
    }

    void insert() {
        // 트랜잭션 내에서 실행 (ACID 보장)
        transactionTemplate.executeWithoutResult(status ->
        {
            // BULK_INSERT_SIZE(1000)개의 Article 생성 및 영속화
            for (int i = 0; i < BULK_INSERT_SIZE; i++) {
                Article article = Article.create(
                    snowflake.nextId(),
                    "title" + i,
                    "content" + i,
                    1L,
                    1L
                );
                entityManager.persist(article);

                // 100건마다 플러시 & 영속성 컨텍스트 초기화
                if ((i + 1) % 100 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        });
    }


}
