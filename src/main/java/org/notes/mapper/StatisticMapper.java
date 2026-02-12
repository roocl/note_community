package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.entity.Statistic;

import java.util.List;

@Mapper
public interface StatisticMapper {

    int insert(Statistic statistic);

    int countStatistic();

    List<Statistic> findByPage(@Param("limit") Integer limit, @Param("offset") Integer offset);
}
