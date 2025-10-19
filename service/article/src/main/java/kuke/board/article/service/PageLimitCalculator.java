package kuke.board.article.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PageLimitCalculator {

    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return ((page - 1) / movablePageCount + 1) * pageSize * movablePageCount + 1;
    }

}
