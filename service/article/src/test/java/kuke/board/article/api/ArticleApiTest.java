package kuke.board.article.api;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        ArticleResponse response = create(
            new ArticleCreateRequest("hi", "Test content", 1L, 1L)
        );
        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
            .uri("/v1/articles")
            .body(request)
            .retrieve()
            .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(238153465126817792L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
            .uri("/v1/articles/{articleId}", articleId)
            .retrieve()
            .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(238153465126817792L);

        ArticleResponse read = read(238153465126817792L);
        System.out.println("read = " + read);
    }

    ArticleResponse update(Long articleId) {
        return restClient.put()
            .uri("/v1/articles/{articleId}", articleId)
            .body(new ArticleUpdateRequest("hi 33", "Update content 2"))
            .retrieve()
            .body(ArticleResponse.class);
    }

    @Test
    void deleteTest() {
        restClient.delete()
            .uri("/v1/articles/{articleId}", 238153465126817792L)
            .retrieve()
            .toBodilessEntity();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
            .uri("/v1/articles?boardId=1&page=30&pageSize=1")
            .retrieve()
            .body(ArticlePageResponse.class);

        System.out.println("response = " + response.getArticleCount());

        for (ArticleResponse article : response.getArticles()) {
            System.out.println("article = " + article.getArticleId());
        }
    }

    /**
     * ParameterizedTypeReference 사용 이유:
     * Java의 Type Erasure로 인해 제네릭 타입 정보가 런타임에 사라지는 문제를 해결
     * ❌ 불가능:
     *   - List<ArticleResponse>.class  (컴파일 에러)
     *   - .body(List.class)  (List<LinkedHashMap>으로 역직렬화됨)
     * ✅ 해결:
     *   - new ParameterizedTypeReference<List<ArticleResponse>>(){}
     *   - 익명 클래스 생성 → 슈퍼클래스의 제네릭 타입 정보가 바이트코드에 보존됨
     *   - 리플렉션으로 타입 정보 추출 → Jackson에게 정확한 타입 전달
     * 사용 예:
     *   - List<T>, Map<K,V>, Set<T> 등 제네릭 컬렉션
     *   - 구체적 클래스(ArticlePageResponse 등)는 .class 사용 가능
     */
    @Test
    void readAllInfiniteScrollTest() {
        // ParameterizedTypeReference: 제네릭 타입 정보 보존
        List<ArticleResponse> articles1 = restClient.get()
            .uri("/v1/articles/infinite-scroll?boardId=1&page=30&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                // 익명 클래스: 슈퍼클래스 제네릭 정보가 바이트코드에 남음
            });
        System.out.println("firstPage");
        for (ArticleResponse articleResponse : articles1) {
            System.out.println("article = " + articleResponse.getArticleId());
        }

        Long lastArticleId = articles1.getLast().getArticleId();

        List<ArticleResponse> articles2 = restClient.get()
            .uri("/v1/articles/infinite-scroll?boardId=1&page=30&pageSize=5&lastArticleId=%s".formatted(lastArticleId))
            .retrieve()
            .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
            });
        System.out.println("secondPage");

        for (ArticleResponse articleResponse : articles2) {
            System.out.println("article = " + articleResponse.getArticleId());
        }
    }

    @Test
    void countTest() {
        ArticleResponse response = create(new ArticleCreateRequest("hi", "Test content", 1L, 2L));

        Long count1 = restClient.get()
            .uri("/v1/articles/boards/{boardId}/count", 2L)
            .retrieve()
            .body(Long.class);

        System.out.println("count1 = " + count1);

        restClient.delete()
            .uri("/v1/articles/{articleId}", response.getArticleId())
            .retrieve()
            .toBodilessEntity();

        Long count2 = restClient.get()
            .uri("/v1/articles/boards/{boardId}/count", 2L)
            .retrieve()
            .body(Long.class);

        System.out.println("count2 = " + count2);




    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
