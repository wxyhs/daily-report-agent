package com.wxy.dailyreportagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxy.dailyreportagent.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Wxy
 * @Date: 2026/3/6 15:13
 * @Description: 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
