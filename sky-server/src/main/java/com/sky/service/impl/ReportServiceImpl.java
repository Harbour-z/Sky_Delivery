package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
  @Autowired
  private OrderMapper orderMapper;

  // 统计指定时间内的营业额数据
  public TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end) {

    List<LocalDate>  dateList = new ArrayList<>();

    dateList.add(start);
    while (!start.equals(end)){
      //日期计算
      start = start.plusDays(1);
      dateList.add(start);
    }

    List<Double> turnoverList = new ArrayList<>();
    for (LocalDate date : dateList) {
      //查询date日期对应的营业额数据，状态为已完成的订单金额合计
      LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
      LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

      Map map = new HashMap();
      map.put("begin", beginTime);
      map.put("end", endTime);
      map.put("status", Orders.COMPLETED);
      Double turnover = orderMapper.sumByMap(map);
      turnover = turnover ==null ? 0.0: turnover;
      turnoverList.add(turnover);
    }

    return TurnoverReportVO
        .builder()
        .dateList(StringUtils.join(dateList, ","))
        .turnoverList(StringUtils.join(turnoverList, ","))
        .build();


  }
}
