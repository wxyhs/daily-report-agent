CREATE TABLE `orders` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                          `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`),
                          KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 插入一些测试数据（可选）
INSERT INTO `orders` (`amount`, `create_time`) VALUES
                                                   (100.50, DATE_SUB(NOW(), INTERVAL 1 DAY)),
                                                   (200.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
                                                   (50.75, DATE_SUB(NOW(), INTERVAL 2 DAY)),
                                                   (300.20, NOW());
