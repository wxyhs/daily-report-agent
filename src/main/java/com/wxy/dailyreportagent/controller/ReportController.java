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
                .defaultSystem("你是一个日报助手，当回答问题时，请用简洁的语言总结关键信息，避免冗长。如果数据量较大，只提取最重要的几点")
                //注册工具
                .defaultTools(reportTools)
                .build();
    }

    @GetMapping("/report")
    public String generateAndSendReport(@RequestParam String query){
        return chatClient.prompt(query).call().content();
    }



}
