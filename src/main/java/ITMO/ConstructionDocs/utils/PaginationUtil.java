package ITMO.ConstructionDocs.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PaginationUtil {
    public static PageRequest getPageRequest(Integer page, Integer sizePerPage, String sort, Sort.Direction order) {
        if (page == null) {
            page = 0;
        } else if (page > 0) {
            page = page - 1;
        }

        if (sizePerPage == null) {
            sizePerPage = 10;
        }

        if (sort == null || order == null) {
            return PageRequest.of(page, sizePerPage);
        } else {
            return PageRequest.of(page, sizePerPage, Sort.by(order, sort));
        }
    }
}
