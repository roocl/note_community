package org.notes.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.mapper.StatisticMapper;
import org.notes.model.base.PageResult;
import org.notes.model.dto.statistic.StatisticQueryParam;
import org.notes.model.entity.Statistic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticServiceImplTest {

    @Mock
    private StatisticMapper statisticMapper;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    @Test
    void getStatistic_returnsPagedResult() {
        StatisticQueryParam param = new StatisticQueryParam();
        param.setPage(2);
        param.setPageSize(5);
        Statistic statistic = new Statistic();
        when(statisticMapper.countStatistic()).thenReturn(11);
        when(statisticMapper.findByPage(5, 5)).thenReturn(List.of(statistic));

        PageResult<List<Statistic>> result = statisticService.getStatistic(param);

        assertEquals(1, result.getData().size());
        assertEquals(11, result.getPagination().getTotal());
        verify(statisticMapper).findByPage(5, 5);
    }
}
