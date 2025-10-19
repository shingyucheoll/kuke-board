package kuke.board.article.service.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor  // Jackson 역직렬화용 기본 생성자
@AllArgsConstructor // Jackson이 필드 주입할 수 있도록
public class ArticlePageResponse {

    private List<ArticleResponse> articles;
    private Long articleCount;

    public static ArticlePageResponse of(List<ArticleResponse> articles, Long articleCount) {
        return new ArticlePageResponse(articles, articleCount);
    }
}
