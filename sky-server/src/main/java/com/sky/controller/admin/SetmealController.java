package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO 新增套餐信息
     * @return 新增结果
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO)
        {
            log.info("新增套餐：{}", setmealDTO);
            setmealService.save(setmealDTO);
            return Result.success();
        }

        /**
         * 套餐分页查询
         * @param setmealPageQueryDTO 分页查询参数
         * @return 分页结果
         */
        @GetMapping("/page")
        @ApiOperation("套餐分页查询")
        public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO)
            {
            log.info("分页查询：{}", setmealPageQueryDTO);
            PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
            return Result.success(pageResult);
        }

        /**
         * 根据套餐id查询套餐详情
         * @param id 套餐id
         * @return 套餐详情
         */
        @GetMapping("/{id}")
        @ApiOperation("根据套餐id查询套餐详情")
        public Result<SetmealVO> detail(@PathVariable Long id)
            {
            log.info("根据套餐id查询套餐详情：{}", id);
            SetmealVO setmealVO = setmealService.getById(id);
            return Result.success(setmealVO);
        }

        /**
         * 批量删除套餐
         * @param ids 套餐id列表
         * @return 删除结果
         */
        @DeleteMapping
        @ApiOperation("批量删除套餐")
        public Result delete(@RequestParam List<Long> ids)
            {
            log.info("批量删除套餐：{}", ids);
            setmealService.delete(ids);
            return Result.success();
        }

        /**
         * 修改套餐
         * @param setmealDTO 修改套餐信息
         * @return 修改结果
         */
        @PutMapping
        @ApiOperation("修改套餐")
        public Result update(@RequestBody SetmealDTO setmealDTO)
            {
            log.info("修改套餐：{}", setmealDTO);
            setmealService.update(setmealDTO);
            return Result.success();
        }

        /**
         * 修改套餐起售停售状态
         * @param status 状态码
         * @return 修改结果
         */
        @PostMapping("/status/{status}")
        @ApiOperation("修改套餐起售停售状态")
        public Result updateStatus(@PathVariable Integer status,Long id)
            {
            log.info("修改套餐起售停售状态：{},{}",id,status);
            setmealService.updateStatus(status,id);
            return Result.success();
        }

}
