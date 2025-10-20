package kuke.board.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kuke.board.comment.entity.Comment;

@SuppressWarnings("SqlResolve")
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(
        value = "select count(*) from (" +
            "select comment_id from commnet " +
            "where article_id = :articleId and parent_comment_id = :parentCommnetId " +
            "limit :limit" +
            ") t",
        nativeQuery = true
    )
    Long countBy(
        @Param("articleId") Long articleId,
        @Param("parentCommentId") Long parentCommentId,
        @Param("limit") Long limit
    );

}
