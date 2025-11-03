package kuke.board.comment.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CommentPathTest {

    @Test
    void createChildCommentTest() {
        // 00000 생성
        createChildCommentTest(CommentPath.create(""), null, "00000");

        // 00000
        //       00000 < 생성
        createChildCommentTest(CommentPath.create("00000"), null, "0000000000");

        // 00001 < 생성
        createChildCommentTest(CommentPath.create(""), "00000", "00001");

        // 0000z
        //      abcdz
        //           zzzzz
        //                zzzzz
        //      abce0  <  생성
        createChildCommentTest(CommentPath.create("0000z"), "0000zabcdzzzzzzzzzzz", "0000zabce0");
    }

    void createChildCommentTest(CommentPath commentPath, String descendantsTopPath, String expectedChildPath) {
        CommentPath childCommentPath = commentPath.createChildCommentPath(descendantsTopPath);
        assertThat(childCommentPath.getPath()).isEqualTo(expectedChildPath);
    }

    @Test
    void createChildCommentPathIfMaxDepthTest() {
        // given & when & then : 이미 5 depth 인 상태에서 하위 댓글을 만들어야 해서 예외 발생
        assertThatThrownBy(() ->
            CommentPath.create("zzzzz".repeat(5)).createChildCommentPath(null))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createChildCommentPathIfChunkOverflowTest() {
        // given
        CommentPath commentPath = CommentPath.create("");

        // when & then  :  descendantTopPath 가 zzzzz 인 경우 chunk 가 Overflow 로 예외 발생
        assertThatThrownBy(() -> commentPath.createChildCommentPath("zzzzz"))
            .isInstanceOf(IllegalStateException.class);
    }
}