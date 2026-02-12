package org.notes.service.impl;

import lombok.RequiredArgsConstructor;
import org.notes.mapper.StatisticMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.Pagination;
import org.notes.model.dto.statistic.StatisticQueryParam;
import org.notes.model.entity.Statistic;
import org.notes.service.StatisticService;
import org.notes.utils.ApiResponseUtil;
import org.notes.utils.PaginationUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final StatisticMapper statisticMapper;


    @Override
    public ApiResponse<List<Statistic>> getStatistic(StatisticQueryParam queryParam) {
        Integer page = queryParam.getPage();
        Integer pageSize = queryParam.getPageSize();
        int offset = PaginationUtils.calculateOffset(page, pageSize);
        int total = statisticMapper.countStatistic();
        Pagination pagination = new Pagination(page, pageSize, total);

        try {
            List<Statistic> statistics = statisticMapper.findByPage(pageSize, offset);
            return ApiResponseUtil.success("获取统计列表成功", statistics, pagination);
        } catch (Exception e) {
            return ApiResponseUtil.error("获取统计列表失败 " + e.getMessage());
        }

    }
}
