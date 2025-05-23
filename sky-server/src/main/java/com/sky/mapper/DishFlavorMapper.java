package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
  //批量插入口味数据

  void insertBatch(List<DishFlavor> flavors);

  @Delete("delete from sky_take_out.dish_flavor where dish_id = #{idshId};")
  void deleteByDishId(Long dishId);

  void deleteByDishIds(List<Long> dishIds);

  @Select("select * from sky_take_out.dish_flavor where dish_id = #{dishId};")
  List<DishFlavor> getByDishId(Long dishId);
}
