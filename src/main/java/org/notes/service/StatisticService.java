package org.notes.service;

import org.notes.model.base.PageResult;
import org.notes.model.dto.statistic.StatisticQueryParam;
import org.notes.model.entity.Statistic;

import java.util.List;

public interface StatisticService {
    PageResult<List<Statistic>> getStatistic(StatisticQueryParam queryParam);
}
