package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     *
     * @param employeeLoginDTO 前端数据
     * @return 员工信息
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     *
     * @param employeeDTO 前端数据
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     *
     * @param employeePageQuery 分页查询参数
     * @return 分页结果
     */
    PageResult page(EmployeePageQueryDTO employeePageQuery);

    /**
     * 编辑员工信息
     *
     * @param status,id 前端数据
     */
    void startOrStop(Integer status, Long id);
}
