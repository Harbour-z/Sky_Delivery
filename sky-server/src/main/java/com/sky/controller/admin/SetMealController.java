package com.sky.controller.admin;

import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api("套餐相关接口")
public class SetMealController {

  @Autowired
  private SetmealService setmealService;

  @PostMapping
  @ApiOperation("新增套餐")
  @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.id")
  public Result save(@RequestBody SetmealDTO setmealDTO) {
    log.info("新增套餐：{}", setmealDTO);
    setmealService.saveWithDish(setmealDTO);
    return Result.success();
  }

  @GetMapping("/page")
  public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
    PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
    return Result.success(pageResult);
  }

  @DeleteMapping
  @ApiOperation("批量删除套餐")
  @CacheEvict(cacheNames = "setmealCache", allEntries = true)
  public Result delete(@RequestParam List<Long> ids){
    setmealService.deleteBatch(ids);
    return Result.success();
  }

  // 根据id查询套餐
  @GetMapping("/{id}")
  public Result<SetmealVO> getById(@PathVariable Long id){
    SetmealVO setmealVO = setmealService.getByIdWithDish(id);
    return Result.success(setmealVO);
  }

  /**
   * 修改套餐
   *
   * @param setmealDTO
   * @return
   */
  @PutMapping
  @ApiOperation("修改套餐")
  @CacheEvict(cacheNames = "setmealCache", allEntries = true)
  public Result update(@RequestBody SetmealDTO setmealDTO) {
    setmealService.update(setmealDTO);
    return Result.success();
  }

  @PostMapping("/status/{status}")
  @CacheEvict(cacheNames = "setmealCache", allEntries = true)
  public Result startOrStop(@PathVariable Integer status, Long id){
    setmealService.startOrStop(status,id);
    return Result.success();
  }

}
