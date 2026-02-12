package org.notes.service;

import org.notes.model.base.ApiResponse;
import org.notes.model.dto.statistic.StatisticQueryParam;
import org.notes.model.entity.Statistic;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface StatisticService {
    ApiResponse<List<Statistic>> getStatistic(StatisticQueryParam queryParam);
}
