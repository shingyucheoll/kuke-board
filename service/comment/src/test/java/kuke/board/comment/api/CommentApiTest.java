package kuke.board.comment.api;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
        CommentResponse response2 = createComment(
            new CommentCreateRequest(1L, "my comment1", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(
            new CommentCreateRequest(1L, "my comment1", response1.getCommentId(), 1L));

        System.out.println("commentId = " + response1.getCommentId());
        System.out.println("commentId = " + response2.getCommentId());
        System.out.println("commentId = " + response3.getCommentId());

    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
            .uri("/v1/comments")
            .body(request)
            .retrieve()
            .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
            .uri("/v1/comments/{commentId}", 241544141500362752L)
            .retrieve()
            .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        // commentId = 241544141500362752
        // commentId = 241544142163062784
        // commentId = 241544142221783040
        restClient.delete()
            .uri("/v1/comments/{commentId}", 241544142221783040L)
            .retrieve()
            .toBodilessEntity();    // 명시.. 할 것 !
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
            .uri("/v1/comments?articleId=1&page=1&pageSize=10")
            .retrieve()
            .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommnetId() = " + comment.getCommentId());
        }

        /** 
         * 1번 페이지 수행 결과
         * comment.getCommnetId() = 241548782571515904
         * 	comment.getCommnetId() = 241548782626041856
         * comment.getCommnetId() = 241548782571515905
         * 	comment.getCommnetId() = 241548782626041865
         * comment.getCommnetId() = 241548782571515906
         * 	comment.getCommnetId() = 241548782626041863
         * comment.getCommnetId() = 241548782571515907
         * 	comment.getCommnetId() = 241548782626041858
         * comment.getCommnetId() = 241548782571515908
         * 	comment.getCommnetId() = 241548782626041859
         */
        
    }
    
    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> responses1 = restClient.get()
            .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
        System.out.println("firstPage");

        for (CommentResponse comment : responses1) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommnetId() = " + comment.getCommentId());
        }


        Long lastParentCommentId = responses1.getLast().getParentCommentId();
        Long lastCommentId = responses1.getLast().getCommentId();


        List<CommentResponse> responses2 = restClient.get()
            .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=" + lastParentCommentId
            + "&lastCommentId=" + lastCommentId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });

        System.out.println("secondPage");
        for (CommentResponse comment : responses2) {
            if(!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommnetId() = " + comment.getCommentId());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}