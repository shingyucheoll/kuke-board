package kuke.board.comment.service.response;

import java.util.List;

import lombok.Getter;

@Getter
public class CommentPageResponse {

    private List<CommentResponse> comments;
    private Long commentCount;

    public static CommentPageResponse of(List <CommentResponse> comments, Long commentCount) {
        CommentPageResponse response = new CommentPageResponse();
        response.comments = comments;
        response.commentCount = commentCount;
        return response;
    }
}
