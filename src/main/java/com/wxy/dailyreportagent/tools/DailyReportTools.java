package com.wxy.dailyreportagent.tools;

import com.wxy.dailyreportagent.entity.Order;
import com.wxy.dailyreportagent.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: Wxy
 * @Date: 2026/3/6 15:17
 * @Description: Ai工具类
 */
@Slf4j
@Component
public class DailyReportTools {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${MAIL_USERNAME}")
    private String MAIL_USERNAME;


    /**
     * 查询指定日期的订单数据
     */
    @Tool(name = "querySalesDate",description = "根据指定日期查询订单总数和总额")
    public String querySalesDate(@ToolParam(description = "查询日期，格式必须为 yyyy-MM-dd，列如 2025-03-06") String date){

        // 1. 参数校验
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "{\"error\": true, \"message\": \"日期参数格式错误，应为yyyy-MM-dd\"}";
        }

        try{
            LocalDate queryDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<Order> orderList = orderService.findByCreateDate(queryDate);
            BigDecimal totalAmount = orderList.stream()
                    .map(Order::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long orderCount = orderList.size();

            return String.format("{\"date\": \"%s\", \"orderCount\": %d, \"totalAmount\": %s}",
                    date, orderCount, totalAmount);
        }catch (Exception e){
            log.error("查询数据失败，日期: {}", date, e);
            // 失败时返回明确的错误信息
            return String.format("{\"error\": true, \"message\": \"查询销售数据失败: %s\"}", e.getMessage());
        }
    }

    @Tool(name = "getCurrentDate", description = "获取当前日期，返回格式为 yyyy-MM-dd")
    public String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * 发送邮箱
     */
    @Tool(name = "sendEmail",description = "发送邮件")
    public String sendEmail(@ToolParam(description = "收件人邮箱") String toEmail,
                            @ToolParam(description = "邮件主题") String subject,
                            @ToolParam(description = "邮件内容") String content){
        try {
            // 创建邮件消息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(MAIL_USERNAME);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            // 发送邮件
            mailSender.send(message);
            return "邮件已成功发送给 " + toEmail;
        }catch (Exception e){
            log.error("发送邮件失败，收件人: {}", toEmail, e);
            return String.format("{\"error\": true, \"message\": \"邮件发送失败: %s\"}", e.getMessage());
        }

    }
}
