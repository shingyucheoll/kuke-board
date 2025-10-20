package kuke.board.comment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Table(name = "comment")
@Getter
@Entity
@ToString
public class Comment {

    @Id
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId;     // shard key
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        // 부모의 ID 가 없을 경우 자신의 아이디로 설정합니다.
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    public boolean isRoot() {
        // Long 타입에서 == 연산자는 "참조 비교"를 합니다
        // longValue()를 사용하면 "값 비교"를 합니다
        // Java는 -128 ~ 127 범위의 Long 값은 캐싱하지만, 그 범위를 벗어나면 새로운 객체를 생성하기 때문에 longValue 로 변경 후 비교
        return parentCommentId.longValue() == commentId;
        // null-safe
        // return Objects.equals(parentCommentId, commentId);

        // equals 사용 ( 값 비교 )
        // return parentCommentId.equals(commentId);
    }

    public void delete() {
        deleted = true;
    }
}
