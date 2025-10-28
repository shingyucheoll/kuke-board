package kuke.board.comment.service;

import static java.util.function.Predicate.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentPageResponse;
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
                // 하위 댓글이 있을 경우
                if (hasChildren(comment)) {
                    // 논리 삭제만 진행
                    comment.delete();
                } else {
                    // 하위 댓글이 없을 경우 물리적 삭제
                    delete(comment);
                }
            });
    }

    private boolean hasChildren(Comment comment) {
        // parentCommentId 를 2개만 조회하고 만약 2개가 조회됐다면 자식을 가진 댓글이므로 true 를 반환합니다.
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        // 해당 댓글이 루트 댓글이 아닌 경우 ( commentId 와 parentCommentId 가 다를 경우 )
        if (!comment.isRoot()) {
            // parentCommentId 로 조회
            commentRepository.findById(comment.getParentCommentId())
                // 이미 삭제된 댓글이라면 ( 기존 자식 댓글이 존재해서 삭제되지 않았던 상태인 경우 )
                .filter(Comment::getDeleted)
                // 자식 댓글이 없는지 확인 후 ( 2개 미만 조회 )
                .filter(not(this::hasChildren))
                // 해당 Comment 를 삭제합니다.
                .ifPresent(this::delete);
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
            commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                .map(CommentResponse::from)
                .toList(),
            commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
            commentRepository.findAllInfiniteScroll(articleId, limit) :
            commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);
        return comments.stream()
            .map(CommentResponse::from)
            .toList();
    }


}
