package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceimpl implements ShoppingCartService {

  @Autowired
  private ShoppingCartMapper shoppingCartMapper;
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private SetmealMapper setmealMapper;
  @Autowired
  private ShoppingCartService shoppingCartService;

  public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    //判断商品是否存在
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
    shoppingCart.setUserId(BaseContext.getCurrentId());

    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
    //存在，数量加1
    if(list != null && list.size() > 0) {
      ShoppingCart cart = list.get(0);
      cart.setNumber(cart.getNumber()+1);
      shoppingCartMapper.updateNumberById(cart);
    }else {

      //不存在，insert

      //判断本次添加是菜品还是套餐
      Long dishId = shoppingCart.getDishId();
      if(dishId != null) {
        Dish dish = dishMapper.getById(dishId);
        shoppingCart.setName(dish.getName());
        shoppingCart.setImage(dish.getImage());
        shoppingCart.setAmount(dish.getPrice());
      }else {
        Long setmealId = shoppingCart.getSetmealId();

        Setmeal setmeal = setmealMapper.getById(setmealId);
        shoppingCart.setName(setmeal.getName());
        shoppingCart.setImage(setmeal.getImage());
        shoppingCart.setAmount(setmeal.getPrice());
      }
      shoppingCart.setNumber(1);
      shoppingCart.setCreateTime(LocalDateTime.now());
      shoppingCartMapper.insert(shoppingCart);

    }

  }


  public List<ShoppingCart> showShoppingCart() {
    Long userId = BaseContext.getCurrentId();
    ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
    return list;
  }

  //清空购物车
  public void cleanShoppingCart() {
    Long userId = BaseContext.getCurrentId();
    shoppingCartMapper.deletByUserId(userId);
  }

  public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
    //设置查询条件，查询当前用户的购物车数据
    shoppingCart.setUserId(BaseContext.getCurrentId());

    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

    if(list != null) {
      shoppingCart = list.get(0);
      Integer number = shoppingCart.getNumber();
      if(number ==1) shoppingCartMapper.deleteById(shoppingCart.getId());
      else {
        shoppingCart.setNumber(--number);
        shoppingCartMapper.updateNumberById(shoppingCart);
      }


    }
  }
}
