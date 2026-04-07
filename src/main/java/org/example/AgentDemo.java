package org.example;

import org.example.agent.Agent;
import org.example.agent.llm.LLMClient;
import org.example.agent.llm.OpenAILLMClient;
import org.example.agent.tools.ToolRegistry;
import org.example.agent.tools.impl.ReadFileTool;
import org.example.agent.tools.impl.RunCommandTool;
import org.example.agent.tools.impl.WriteFileTool;

/**
 * Agent 示例演示
 */
public class AgentDemo {
    public static void main(String[] args) {
        // 1. 初始化工具注册表
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.registerTool(new WriteFileTool());
        toolRegistry.registerTool(new ReadFileTool());
        toolRegistry.registerTool(new RunCommandTool());

        // 2. 初始化 LLM 客户端（使用 FRIDAY 平台）
        // App ID 从环境变量获取，或者直接写在这里
        String appId = System.getenv("FRIDAY_APP_ID");
        if (appId == null || appId.isEmpty()) {
            appId = "1894654060091985986";  // 替换为你的 App ID
        }
        String apiUrl = "";
        String model = "gpt-4o-mini";  // 推荐使用性价比高的模型

        LLMClient llmClient = new OpenAILLMClient(appId, apiUrl, model);

        // 3. 创建 Agent
        Agent agent = new Agent(llmClient, toolRegistry, 100);

        // 4. 执行任务示例
        System.out.println("=== Agent 任务执行演示 ===\n");

        // 示例 1: 创建一个简单的 Java 类
        String task1 = "请创建一个 Calculator.java 文件，包含加减乘除四个方法";
        System.out.println("任务: " + task1);
        String result1 = agent.execute(task1);
        System.out.println("结果: " + result1);
        System.out.println("\n---\n");
    }
}

