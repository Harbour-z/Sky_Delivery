package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

  @Autowired
  private SetmealMapper setmealMapper;
  @Autowired
  private SetmealDishMapper setmealDishMapper;
  @Autowired
  private DishMapper dishMapper;


  @Transactional
  public void saveWithDish(SetmealDTO setmealDTO) {
    Setmeal setmeal = new Setmeal();
    BeanUtils.copyProperties(setmealDTO, setmeal);
    setmealMapper.insert(setmeal);

    Long setmealId = setmeal.getId();

    List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
    setmealDishes.forEach(setmealDish -> {
      setmealDish.setSetmealId(setmealId);
    });
    setmealDishMapper.insertBatch(setmealDishes);
  }

  //分页查询
  public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
    PageHelper.startPage(setmealPageQueryDTO.getPage(),
        setmealPageQueryDTO.getPageSize());
    Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
    return new PageResult(page.getTotal(), page.getResult());
  }

  //批量删除套餐
  public void deleteBatch(List<Long> ids) {
    ids.forEach(id -> {
      Setmeal setmeal = setmealMapper.getById(id);
      if(StatusConstant.ENABLE.equals(setmeal.getStatus())){
        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
      }
    });

    ids.forEach(setmealId -> {
      //删除套餐表中的数据
      setmealMapper.deleteById(setmealId);
      //删除套餐菜品关系表中的数据
      setmealDishMapper.deleteBySetmealId(setmealId);
    });
  }


  public SetmealVO getByIdWithDish(Long id) {
    Setmeal setmeal = setmealMapper.getById(id);
    List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

    SetmealVO setmealVO = new SetmealVO();
    BeanUtils.copyProperties(setmeal, setmealVO);
    setmealVO.setSetmealDishes(setmealDishes);

    return setmealVO;
  }


  public void update(SetmealDTO setmealDTO) {
    Setmeal setmeal = new Setmeal();
    BeanUtils.copyProperties(setmealDTO, setmeal);
    setmealMapper.update(setmeal);
    Long setmealId = setmealDTO.getId();

    setmealDishMapper.deleteBySetmealId(setmealId);
    List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
    setmealDishes.forEach(setmealDish -> {
      setmealDish.setSetmealId(setmealId);
    });
    setmealDishMapper.insertBatch(setmealDishes);

  }

  public void startOrStop(Integer status, Long id) {
    //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
    if(status == StatusConstant.ENABLE){
      //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
      List<Dish> dishList = dishMapper.getBySetmealId(id);
      if(dishList != null && dishList.size() > 0){
        dishList.forEach(dish -> {
          if(StatusConstant.DISABLE == dish.getStatus()){
            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
          }
        });
      }
    }

    Setmeal setmeal = Setmeal.builder()
        .id(id)
        .status(status)
        .build();
    setmealMapper.update(setmeal);
  }

  /**
   * 条件查询
   * @param setmeal
   * @return
   */
  public List<Setmeal> list(Setmeal setmeal) {
    List<Setmeal> list = setmealMapper.list(setmeal);
    return list;
  }

  /**
   * 根据id查询菜品选项
   * @param id
   * @return
   */
  public List<DishItemVO> getDishItemById(Long id) {
    return setmealMapper.getDishItemBySetmealId(id);
  }
}
