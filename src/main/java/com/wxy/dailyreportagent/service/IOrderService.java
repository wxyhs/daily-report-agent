package com.wxy.dailyreportagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wxy.dailyreportagent.entity.Order;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: Wxy
 * @Date: 2026/3/6 15:14
 * @Description: 订单业务逻辑层
 */
public interface IOrderService extends IService<Order> {

    /**
     * 根据时间查询
     * @param date 时间条件
     * @return 订单数据
     */
    List<Order> findByCreateDate(LocalDate date);
}
