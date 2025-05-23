package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j

public class DishServiceImpl implements DishService {

  @Autowired
  private DishMapper dishMapper;

  @Autowired
  private DishFlavorMapper dishFlavorMapper;
  @Autowired
  private SetmealMapper setmealMapper;
  @Autowired
  private SetmealDishMapper setmealDishMapper;


  /**
   * 新增菜品和对应的口味
   * @param dishDTO
   */
  @Transactional
  public void saveWithFlavor(DishDTO dishDTO) {

    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);

    dishMapper.insert(dish);

    //获取insert生成的主键值
    Long dishId = dish.getId();

    List<DishFlavor> flavors = dishDTO.getFlavors();
    if(flavors != null && flavors.size() > 0) {
      flavors.forEach(dishFlavor -> {
        dishFlavor.setDishId(dishId);
      });
      //向口味表插入n条
      dishFlavorMapper.insertBatch(flavors);
    }

  }

  //菜品分页查询
  public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
    PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
    Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
    return new PageResult(page.getTotal(), page.getResult());
  }

  //批量删除
  public void deleteBatch(List<Long> ids) {
    for(Long id : ids) {
      Dish dish = dishMapper.getById(id);
      if(dish.getStatus()== StatusConstant.ENABLE){
        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
      }
    }

    List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
    if(setmealIds != null && setmealIds.size() > 0) {
      throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
    }
    for (Long id : ids) {
      dishMapper.deleteById(id);
      dishFlavorMapper.deleteByDishId(id);
    }
  }

  //查询id对应的菜品喝口味数据
  public DishVO getByIdWithFlavor(Long id) {
    Dish dish = dishMapper.getById(id);
    List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
    DishVO dishVO = new DishVO();
    BeanUtils.copyProperties(dish, dishVO);
    dishVO.setFlavors(dishFlavors);
    return dishVO;
  }


  public void updateWithFlavor(DishDTO dishDTO) {
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);
    //修改菜品表基本信息

    dishMapper.update(dish);
    //删除原来的口味
    dishFlavorMapper.deleteByDishId(dishDTO.getId());
    //重新插入

    List<DishFlavor> flavors = dishDTO.getFlavors();
    if(flavors != null && flavors.size() > 0) {
      flavors.forEach(dishFlavor -> {dishFlavor.setDishId(dishDTO.getId());});
      dishFlavorMapper.insertBatch(flavors);
    }
  }


  public void startOrStop(Integer status, Long id) {
    Dish dish = Dish.builder()
        .id(id)
        .status(status)
        .build();
    dishMapper.update(dish);

    //TODO 如果是停售操作，还需要讲包含当前菜品的套餐也停掉
    if(status == StatusConstant.DISABLE) {
      List<Long> dishIds = new ArrayList<>();
      dishIds.add(id);
      List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
      if(setmealIds != null && setmealIds.size() > 0) {
        for(Long setmealId : setmealIds) {
          Setmeal setmeal = Setmeal.builder().id(setmealId).status(StatusConstant
              .DISABLE).build();
          setmealMapper.update(setmeal);
        }
      }

    }
  }

  //根据分类id查询该分类下的所有菜品
  public List<Dish> list(Long categoryId) {
    Dish dish = Dish.builder().categoryId(categoryId)
        .status(StatusConstant.ENABLE).build();

    return dishMapper.list(dish);
  }

  /**
   * 条件查询菜品和口味
   * @param dish
   * @return
   */
  public List<DishVO> listWithFlavor(Dish dish) {
    List<Dish> dishList = dishMapper.list(dish);

    List<DishVO> dishVOList = new ArrayList<>();

    for (Dish d : dishList) {
      DishVO dishVO = new DishVO();
      BeanUtils.copyProperties(d,dishVO);

      //根据菜品id查询对应的口味
      List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

      dishVO.setFlavors(flavors);
      dishVOList.add(dishVO);
    }

    return dishVOList;
  }
}
