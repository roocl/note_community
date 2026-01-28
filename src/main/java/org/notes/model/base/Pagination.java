package org.notes.model.base;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pagination {
    private Integer page;
    private Integer pageSize;
    private Integer total;
}
