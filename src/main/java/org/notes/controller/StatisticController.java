package org.notes.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.notes.model.base.ApiResponse;
import org.notes.model.dto.statistic.StatisticQueryParam;
import org.notes.model.entity.Statistic;
import org.notes.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "统计数据")
@RestController
@RequestMapping("/api")
public class StatisticController {

    @Autowired
    StatisticService statisticService;

    @ApiOperation("获取统计数据列表")
    @GetMapping("/statistic")
    public ApiResponse<List<Statistic>> getStatistic(@Valid StatisticQueryParam queryParam) {
        return statisticService.getStatistic(queryParam);
    }
}