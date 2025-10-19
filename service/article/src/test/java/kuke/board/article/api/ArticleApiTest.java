package kuke.board.article.api;


import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

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
