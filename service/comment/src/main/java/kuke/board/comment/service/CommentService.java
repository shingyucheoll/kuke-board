package kuke.board.comment.service;

import static java.util.function.Predicate.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
            Comment.create(
                snowflake.nextId(),
                request.getContent(),
                // 댓글을 생성할 때 부모의 id 가 존재할 경우 parentId 를 설정합니다.
                parent == null ? null : parent.getCommentId(),
                request.getArticleId(),
                request.getWriterId()
            )
        );
        return CommentResponse.from(comment);

    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return null;
        }
        return commentRepository.findById(parentCommentId)
            // 삭제되지 않은 댓글만 필터링 (deleted = false 통과)
            .filter(not(Comment::getDeleted))
            // 최상위 댓글(루트)만 필터링 (대댓글에는 대댓글 작성 불가)
            .filter(Comment::isRoot)
            // 3-3. 조건을 만족하지 않으면 예외 발생
            .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(commentRepository.findById(commentId).orElseThrow());
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
            .filter(not(Comment::getDeleted))
            .ifPresent(comment -> {
                if (hasChildren(comment)) {
                    comment.delete();
                } else {
                    delete(comment);
                }
            });
    }

    private boolean hasChildren(Comment comment) {
        return false;
    }

    private void delete(Comment comment) {

    }

}
