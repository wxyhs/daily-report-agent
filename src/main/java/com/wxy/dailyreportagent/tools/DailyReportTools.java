package com.wxy.dailyreportagent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${baidu.search.api-key}")
    private String baiduApiKey;

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

    @Tool(name = "baiduSearch", description = "使用百度搜索引擎获取实时信息，如新闻、天气、最新事件等。当用户需要最新信息时调用。")
    public String baiduSearch(@ToolParam(description = "搜索关键词，例如'今天北京天气'") String query) {
        // 百度搜索API接口地址
        String url = "https://qianfan.baidubce.com/v2/ai_search/web_summary";

        // 构建请求体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + baiduApiKey);

        // 请求体（可以适当减少返回数量，例如 top_k=3）
        String requestBody = String.format(
                "{\"messages\":[{\"content\":\"%s\",\"role\":\"user\"}],\"search_source\":\"baidu_search_v2\",\"resource_type_filter\":[{\"type\":\"web\",\"top_k\":3}]}",
                query
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return String.format("{\"error\": true, \"message\": \"搜索失败，状态码: %s\"}", response.getStatusCode());
            }

            // 解析并精简返回内容
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            // 假设百度返回的 JSON 结构中有 references 数组，每个元素包含 title 和 snippet
            JsonNode references = root.path("references");
            StringBuilder simplified = new StringBuilder("搜索结果：\n");
            if (references.isArray()) {
                for (JsonNode ref : references) {
                    String title = ref.path("title").asText();
                    String snippet = ref.path("snippet").asText();
                    simplified.append("标题: ").append(title).append("\n")
                            .append("摘要: ").append(snippet).append("\n\n");
                }
            }
            // 如果没有找到 references，则返回原始内容的摘要（前200字符）
            if (simplified.length() == 0) {
                String raw = response.getBody();
                simplified.append(raw.substring(0, Math.min(200, raw.length())));
            }
            return simplified.toString();

        } catch (Exception e) {
            throw new RuntimeException("百度搜索失败: " + e.getMessage(), e);
        }
    }
}
