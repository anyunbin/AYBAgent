package org.example.agent.llm;

import org.example.agent.Agent.LLMResponse;
import org.example.agent.Agent.ToolCall;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock LLM 客户端 - 用于演示 Agent 工作流程
 * 模拟 LLM 的决策过程
 */
public class MockLLMClient implements LLMClient {
    private int callCount = 0;
    private String currentTask = "";

    @Override
    public LLMResponse chat(String prompt) {
        callCount++;

        System.out.println("\n=== LLM 调用 #" + callCount + " ===");
        System.out.println("收到提示词长度: " + prompt.length() + " 字符");

        // 从提示词中提取用户任务
        if (prompt.contains("[user]:") && currentTask.isEmpty()) {
            int userStart = prompt.indexOf("[user]:");
            int nextBracket = prompt.indexOf("[", userStart + 7);
            if (nextBracket > 0) {
                currentTask = prompt.substring(userStart + 7, nextBracket).trim();
            } else {
                currentTask = prompt.substring(userStart + 7).trim();
            }
        }

        LLMResponse response = new LLMResponse();

        // 模拟 ReAct 模式的决策过程
        if (callCount == 1) {
            // 第一次调用：Thought + Action (创建文件)
            response.setThought("用户要创建计算器类，我需要先写代码文件");
            response.setToolCall(createWriteFileToolCall());

        } else if (callCount == 2) {
            // 第二次调用：Thought + Action (编译代码)
            response.setThought("文件已创建成功。现在需要编译验证代码是否正确。");
            response.setToolCall(createCompileToolCall());

        } else if (callCount == 3) {
            // 第三次调用：观察编译结果
            if (prompt.contains("命令执行失败") || prompt.contains("error")) {
                response.setThought("编译失败了！错误信息显示第3行缺少分号。我需要修复代码。");
                response.setToolCall(createFixCodeToolCall());
            } else {
                response.setThought("编译成功！所有步骤都已完成，任务达成。");
                response.setFinalAnswer("✅ 任务完成！Calculator.java 已成功创建并编译。");
            }

        } else if (callCount == 4) {
            // 第四次调用：重新编译修复后的代码
            response.setThought("代码已修复（添加了分号）。现在重新编译验证。");
            response.setToolCall(createCompileToolCall());

        } else {
            // 最终响应
            response.setThought("编译成功！所有步骤完成：创建文件、发现错误、修复错误、验证成功。");
            response.setFinalAnswer("✅ 任务完成！代码已创建、修复并成功编译。");
        }

        return response;
    }

    /**
     * 创建写文件的工具调用
     */
    private ToolCall createWriteFileToolCall() {
        Map<String, Object> params = new HashMap<>();
        params.put("path", "Calculator.java");
        params.put("content",
            "public class Calculator {\n" +
            "    public int add(int a, int b) {\n" +
            "        return a + b\n" +  // 故意缺少分号，模拟错误
            "    }\n" +
            "}\n"
        );
        return new ToolCall("write_file", params);
    }

    /**
     * 创建编译命令的工具调用
     */
    private ToolCall createCompileToolCall() {
        Map<String, Object> params = new HashMap<>();
        params.put("command", "javac Calculator.java");
        return new ToolCall("run_command", params);
    }

    /**
     * 创建修复代码的工具调用
     */
    private ToolCall createFixCodeToolCall() {
        Map<String, Object> params = new HashMap<>();
        params.put("path", "Calculator.java");
        params.put("content",
            "public class Calculator {\n" +
            "    public int add(int a, int b) {\n" +
            "        return a + b;  // 修复：添加分号\n" +
            "    }\n" +
            "}\n"
        );
        return new ToolCall("write_file", params);
    }
}

