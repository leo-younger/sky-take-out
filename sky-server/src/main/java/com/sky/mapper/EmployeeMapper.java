package com.sky.mapper;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee 待插入的员工数据
     */
    @Insert("insert into employee (username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)" +
            "values " +
            "(#{username},#{name},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);

    /**
     * 分页查询
     * @param employeePageQuery 分页查询参数
     * @return 分页查询结果
     */
    List<Employee> page(EmployeePageQueryDTO employeePageQuery);

    /**
     * 根据id查询员工
     * @param emp 员工信息
     * @return
     */
    void update(Employee emp);

    /**
     * 根据id查询员工
     *
     * @param id 员工id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
