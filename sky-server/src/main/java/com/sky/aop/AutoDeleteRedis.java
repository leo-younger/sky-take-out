package com.sky.aop;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Young
 * 自动删除redis缓存数据，保证数据的一致性
 */
@Aspect
@Component
@Slf4j
public class AutoDeleteRedis {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 切入点：切Service层带有@AutoDeleteRedis注解的方法
     */
    @Pointcut("execution(* com.sky.service..*.*(..)) && @annotation(com.sky.annotation.AutoDeleteRedis)")
    public void pointCut() {
    }

    /**
     * 环绕通知：方法执行前先查旧数据，执行完再删缓存
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Set<Long> categoryIds = new HashSet<>();

        switch (methodName) {
            case "updateWithFlavor":
                // 方法执行前先查旧分类！
                DishDTO updateDTO = (DishDTO) args[0];
                Dish oldDish = dishMapper.getById(updateDTO.getId());
                if (oldDish != null) {
                    categoryIds.add(oldDish.getCategoryId());
                }
                categoryIds.add(updateDTO.getCategoryId());
                break;

            case "updateStatus":
                // 方法执行前先查分类（状态不影响分类，但先查出来避免影响其他）
                Long id = (Long) args[0];
                Dish dish = dishMapper.getById(id);
                if (dish != null) {
                    categoryIds.add(dish.getCategoryId());
                }
                break;

            default:
                break;
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        // 方法执行成功后，删除缓存
        if (!categoryIds.isEmpty()) {
            Set<String> keys = categoryIds.stream()
                    .map(cid -> "dish_" + cid)
                    .collect(Collectors.toSet());
            redisTemplate.delete(keys);
            log.info("自动删除菜品缓存：{}, 共{}个key", keys, keys.size());
        }

        return result;
    }
}