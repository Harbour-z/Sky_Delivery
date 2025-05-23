package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
  void insert(Orders orders);
  /**
   * 根据订单号查询订单
   * @param orderNumber
   */
  @Select("select * from orders where number = #{orderNumber}")
  Orders getByNumber(String orderNumber);

  /**
   * 修改订单信息
   * @param orders
   */
  void update(Orders orders);

  @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{checkOutTime} " +
      "where number = #{orderNumber}")
  void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, String orderNumber);


  //根据订单状态和下单时间查询
  @Select("select * from orders where status = #{status} and order_time < #{orderTime};")
  List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

  @Select("select * from orders where id=#{id}")
  Orders getById(Long id);

  Double sumByMap(Map map);

  Integer countByMap(Map map);

  List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
