package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

        /*
         response1.getPath() = 00000
         response1.getCommentId() = 243734548847407104
                response2.getPath() = 0000000000
                response2.getCommentId() = 243734549753376768
                        response3.getPath() = 000000000000000
                        response3.getCommentId() = 243734549824679936
         */
    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
            .uri("/v2/comments")
            .body(request)
            .retrieve()
            .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
            .uri("/v2/comments/{commentId}", 243734548847407104L)
            .retrieve()
            .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
            .uri("/v2/comments/{commentId}", 243734548847407104L)
            .retrieve()
            .toBodilessEntity();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
            .uri("/v2/comments?articleId=1&pageSize=10&page=50000")
            .retrieve()
            .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         comment.getCommentId() = 243734548847407104
         comment.getCommentId() = 243734549753376768
         comment.getCommentId() = 243734549824679936
         comment.getCommentId() = 243736588225085443
         comment.getCommentId() = 243736588304777216
         comment.getCommentId() = 243736588304777226
         comment.getCommentId() = 243736588304777235
         comment.getCommentId() = 243736588304777244
         comment.getCommentId() = 243736588304777253
         comment.getCommentId() = 243736588304777260
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> responses1 = restClient.get()
            .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });

        System.out.println("firstPage");
        for (CommentResponse response : responses1) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }

        // lastPath
        String lastPath = responses1.getLast().getPath();

        List<CommentResponse> responses2 = restClient.get()
            .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });

        System.out.println("secondPage");
        for (CommentResponse response : responses2) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }

        // response.getCommentId() = 243734548847407104
        // response.getCommentId() = 243734549753376768
        // response.getCommentId() = 243734549824679936
        // response.getCommentId() = 243736588225085443
        // response.getCommentId() = 243736588304777216
        // secondPage
        // response.getCommentId() = 243736588304777226
        // response.getCommentId() = 243736588304777235
        // response.getCommentId() = 243736588304777244
        // response.getCommentId() = 243736588304777253
        // response.getCommentId() = 243736588304777260
    }

    @Test
    void countTest() {
        CommentResponse commentResponse = create(new CommentCreateRequestV2(2L, "my comment1", null, 1L));

        Long count1 = restClient.get()
            .uri("/v2/comments/articles/{articleId}/count", 2L)
            .retrieve()
            .body(Long.class);
        System.out.println("count1 = " + count1); // 1

        restClient.delete()
            .uri("/v2/comments/{commentId}", commentResponse.getCommentId())
            .retrieve()
            .toBodilessEntity();

        Long count2 = restClient.get()
            .uri("/v2/comments/articles/{articleId}/count", 2L)
            .retrieve()
            .body(Long.class);
        System.out.println("count2 = " + count2); // 0
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
