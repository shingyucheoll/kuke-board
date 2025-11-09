package kuke.board.article.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kuke.board.article.entity.Article;
import kuke.board.article.entity.BoardArticleCount;
import kuke.board.article.repository.ArticleRepository;
import kuke.board.article.repository.BoardArticleCountRepository;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(
            Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(),
                request.getWriterId())
        );
        int result = boardArticleCountRepository.increase(request.getBoardId());
        if (result == 0) {
            boardArticleCountRepository.save(
                BoardArticleCount.init(request.getBoardId(), 1L)
            );
        }

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        return ArticleResponse.from(article);
    }

    @Transactional(readOnly = true)
    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.delete(article);
        boardArticleCountRepository.decrease(article.getBoardId());
    }

    /**
     * 게시판의 게시글 목록을 페이징하여 조회
     * @param boardId 게시판 ID
     * @param page 현재 페이지 번호 (1부터 시작)
     * @param pageSize 한 페이지당 게시글 개수
     * @return 게시글 목록 + 전체 개수를 담은 응답 객체
     */
    @Transactional
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {

        // 보여줄 페이지 개수
        final long movablePageCount = 10;

        // 1. OFFSET 계산
        // 예: page=3, pageSize=10 → offset = (3-1) * 10 = 20 (21번째 게시글부터 조회)
        Long offset = (page - 1) * pageSize;

        // 2. 게시글 목록 조회 (Native Query 사용)
        // - 커버링 인덱스로 article_id만 먼저 조회 (offset, limit 적용)
        // - 이후 JOIN으로 전체 컬럼 가져오기 (성능 최적화)
        List<Article> articles = articleRepository.findAll(boardId, offset, pageSize);

        // 3. Entity → DTO 변환
        // Article 엔티티를 ArticleResponse DTO로 매핑
        List<ArticleResponse> articleResponses = articles.stream()
            .map(ArticleResponse::from)
            .toList();

        // 4. 전체 게시글 개수 계산 (페이지네이션 UI용)
        // PageLimitCalculator: 사용자가 이동 가능한 페이지 범위 내의 최대 데이터 개수 계산
        // 예: page=30, pageSize=1, movablePageCount=10
        //     → ((30-1)/10 + 1) * 1 * 10 + 1 = 3 * 10 + 1 = 31
        //     → 최대 31개까지만 카운트 (성능 최적화: 전체 COUNT 방지)
        Long pageLimit = PageLimitCalculator.calculatePageLimit(page, pageSize, movablePageCount);
        Long totalCount = articleRepository.count(boardId, pageLimit);

        // 5. 응답 객체 생성 및 반환
        return ArticlePageResponse.of(articleResponses, totalCount);
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
            articleRepository.findAllInfiniteScroll(boardId, pageSize) :
            articleRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);

        return articles.stream().map(ArticleResponse::from).toList();
    }

    public Long count(Long boardId) {
        return boardArticleCountRepository.findById(boardId)
            .map(BoardArticleCount::getArticleCount)
            .orElse(0L);
    }
}
