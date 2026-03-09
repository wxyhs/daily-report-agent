package com.wxy.dailyreportagent.controller;

import com.wxy.dailyreportagent.tools.DailyReportTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Wxy
 * @Date: 2026/3/6 15:29
 * @Description: Ai交互视图层
 */
@RequestMapping("/api")
@RestController
public class ReportController {

    private final ChatClient chatClient;

    public ReportController(ChatClient.Builder builder, DailyReportTools reportTools){
        this.chatClient = builder
                .defaultSystem("你是一个日报助手。如果工具执行返回了错误信息，请如实向用户报告失败原因，不要编造虚假数据。")
                //注册工具
                .defaultTools(reportTools)
                .build();
    }

    @GetMapping("/report")
    public String generateAndSendReport(@RequestParam String query){
        return chatClient.prompt(query).call().content();
    }



}
