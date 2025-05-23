package com.sky.controller.user;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api("用户端订单接口")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @PostMapping("/submit")
  public Result<OrderSubmitVO>  submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
    OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
    log.info("用户下单，参数为：{}", orderSubmitVO);
    return Result.success(orderSubmitVO);
  }

  /**
   * 订单支付
   *
   * @param ordersPaymentDTO
   * @return
   */
  @PutMapping("/payment")
  @ApiOperation("订单支付")
  public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
    log.info("订单支付：{}", ordersPaymentDTO);
    OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
    log.info("生成预支付交易单：{}", orderPaymentVO);
    return Result.success(orderPaymentVO);
  }

  @GetMapping("/reminder/{id}")
  @ApiOperation("客户催单")
  public Result reminder(@PathVariable Long id){
    orderService.reminder(id);

    return Result.success();
  }
}
