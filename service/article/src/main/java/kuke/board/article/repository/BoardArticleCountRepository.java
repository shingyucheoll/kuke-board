package kuke.board.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kuke.board.article.entity.BoardArticleCount;

@Repository
public interface BoardArticleCountRepository extends JpaRepository<BoardArticleCount, Long> {

    @Query(
        value = "update board_article_count set article_count = article_count + 1 where board_id = :boardId",
        nativeQuery = true
    )
    @Modifying
        // Update 쿼리 실행을 위해 추가
    int increase(@Param("boardId")Long boardId);

    @Query(
        value = "update board_article_count set article_count = article_count - 1 where board_id = :boardId",
        nativeQuery = true
    )
    @Modifying  // Update 쿼리 실행을 위해 추가
    void decrease(@Param("boardId")Long boardId);

}
