package org.example.agent;

import org.example.agent.llm.LLMClient;
import org.example.agent.memory.ConversationMemory;
import org.example.agent.tools.Tool;
import org.example.agent.tools.ToolRegistry;

import java.util.Map;

/**
 * AI Agent 核心类
 * 负责协调 LLM、工具调用、记忆管理等
 */
public class Agent {
    private final LLMClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ConversationMemory memory;
    private final int maxIterations;

    public Agent(LLMClient llmClient, ToolRegistry toolRegistry, int maxIterations) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.memory = new ConversationMemory();
        this.maxIterations = maxIterations;
    }

    /**
     * 执行用户任务
     * 使用 ReAct (Reasoning + Acting) 模式：
     * Thought (推理) → Action (执行) → Observation (观察) → 循环
     *
     * @param userInput 用户输入
     * @return 执行结果
     */
    public String execute(String userInput) {
        memory.addUserMessage(userInput);

        int iteration = 0;
        while (iteration < maxIterations) {
            iteration++;
            System.out.println("\n=== 迭代 #" + iteration + " ===");

            // ============ Reasoning 阶段 ============
            // 让 LLM 思考当前状态和下一步行动
            String prompt = buildPrompt();
            LLMResponse response = llmClient.chat(prompt);

            String thought = response.getThought();
            if (thought != null && !thought.isEmpty()) {
                System.out.println("💭 Thought: " + thought);
                memory.addAssistantMessage("[思考] " + thought);
            }

            // ============ Action 阶段 ============
            if (response.isToolCall()) {
                ToolCall toolCall = response.getToolCall();
                System.out.println("🔧 Action: " + toolCall.getToolName() + "(" + toolCall.getParameters() + ")");
                memory.addAssistantMessage("[行动] 使用工具: " + toolCall.getToolName());

                // 执行工具
                String toolResult = executeToolCall(toolCall);

                // ============ Observation 阶段 ============
                // 将执行结果作为观察反馈给 LLM
                System.out.println("👁️  Observation: " + toolResult.substring(0, Math.min(100, toolResult.length())) + "...");
                memory.addObservation(toolCall.getToolName(), toolResult);

                // 继续循环，让 LLM 观察结果后决定下一步
                // 不在这里判断是否完成，而是让 LLM 在下一轮推理中判断

            } else if (response.isFinalAnswer()) {
                // LLM 认为任务完成，返回最终答案
                String finalAnswer = response.getContent();
                System.out.println("✅ Final Answer: " + finalAnswer);
                memory.addAssistantMessage("[最终答案] " + finalAnswer);
                return finalAnswer;

            } else {
                // LLM 返回的不是工具调用也不是最终答案
                // 可能是中间思考或需要继续
                String content = response.getContent();
                System.out.println("💬 Response: " + content);
                memory.addAssistantMessage(content);

                // 继续循环，让 LLM 有机会继续推理
            }
        }

        return "任务超过最大迭代次数: " + maxIterations;
    }

    /**
     * 构建发送给 LLM 的完整提示词
     */
    private String buildPrompt() {
        StringBuilder prompt = new StringBuilder();

        // 系统提示词
        prompt.append(getSystemPrompt()).append("\n\n");

        // 工具定义
        prompt.append(getToolDefinitions()).append("\n\n");

        // 对话历史
        prompt.append(memory.getConversationHistory()).append("\n\n");

        // 指导性提示
        prompt.append(getGuidancePrompt());

        return prompt.toString();
    }

    /**
     * 系统提示词 - 定义 Agent 的角色和能力
     */
    private String getSystemPrompt() {
        return "你是一个智能 AI Agent，能够自主完成各种任务。\n\n" +
            "你的核心能力：\n" +
            "1. 编程开发：编写、编译、运行、测试代码\n" +
            "2. 数据处理：使用 bash/shell 命令处理文本、CSV、JSON 等数据\n" +
            "3. 文件操作：读取、写入、修改文件\n" +
            "4. 系统命令：执行各种系统命令完成任务\n\n" +
            "你的工作流程：\n" +
            "1. 理解用户需求（无论是编程、数据处理还是其他任务）\n" +
            "2. 分解任务为具体步骤\n" +
            "3. 使用可用工具执行每个步骤\n" +
            "4. 验证执行结果\n" +
            "5. 如果出现错误，分析错误并修复\n" +
            "6. 重复直到任务完成\n\n" +
            "你可以使用以下工具来完成任务。当你需要使用工具时，请以 JSON 格式输出：\n" +
            "{\"tool\": \"工具名\", \"parameters\": {\"参数名\": \"参数值\"}}\n\n" +
            "重要原则：\n" +
            "- 对于编程任务：编写代码后必须编译验证\n" +
            "- 对于数据处理任务：优先使用 bash 命令（grep、awk、sed、cut 等）\n" +
            "- 对于文件操作任务：先读取文件内容再处理\n" +
            "- 始终验证最终结果是否符合用户预期\n" +
            "- 如果失败，分析错误信息并修复\n\n" +
            "任务类型识别：\n" +
            "- \"写代码\"、\"创建类\"、\"实现功能\" → 编程任务\n" +
            "- \"处理数据\"、\"统计\"、\"过滤\"、\"提取\" → 数据处理任务\n" +
            "- \"读取文件\"、\"查看内容\" → 文件操作任务\n" +
            "- \"运行命令\"、\"执行脚本\" → 系统命令任务\n";
    }

    /**
     * 工具定义提示词
     */
    private String getToolDefinitions() {
        StringBuilder definitions = new StringBuilder("可用工具列表：\n");
        for (Tool tool : toolRegistry.getAllTools()) {
            definitions.append(String.format(
                "工具名: %s\n描述: %s\n参数: %s\n\n",
                tool.getName(),
                tool.getDescription(),
                tool.getParameters()
            ));
        }
        return definitions.toString();
    }

    /**
     * 指导性提示词 - ReAct 模式引导
     */
    private String getGuidancePrompt() {
        return "\n请使用 ReAct (Reasoning + Acting) 模式进行推理和执行：\n\n" +
            "每一轮循环包含三个阶段：\n\n" +
            "1. **Thought (思考)**：\n" +
            "   - 分析当前任务状态\n" +
            "   - 回顾之前的观察结果\n" +
            "   - 决定下一步行动\n" +
            "   - 如果任务已完成，准备给出最终答案\n\n" +
            "2. **Action (行动)**：\n" +
            "   - 如果需要执行操作，使用工具\n" +
            "   - 格式: {\"tool\": \"工具名\", \"parameters\": {...}}\n" +
            "   - 如果任务完成，返回最终答案\n" +
            "   - 格式: {\"final_answer\": \"你的最终答案\"}\n\n" +
            "3. **Observation (观察)**：\n" +
            "   - 系统会自动将工具执行结果作为观察反馈给你\n" +
            "   - 在下一轮中，你将看到这个观察结果\n" +
            "   - 基于观察结果继续推理\n\n" +
            "重要规则：\n" +
            "- 每次只执行一个行动（工具调用或最终答案）\n" +
            "- 在给出最终答案前，确保所有必要步骤都已完成\n" +
            "- 如果遇到错误，在下一轮思考中分析并修复\n" +
            "- 使用 {\"final_answer\": \"...\"} 明确表示任务完成\n\n" +
            "示例格式：\n" +
            "Thought: 我需要创建一个文件\n" +
            "Action: {\"tool\": \"write_file\", \"parameters\": {...}}\n" +
            "(系统执行后)\n" +
            "Observation: 文件创建成功\n\n" +
            "Thought: 文件已创建，现在需要编译\n" +
            "Action: {\"tool\": \"run_command\", \"parameters\": {\"command\": \"javac ...\"}}\n" +
            "(系统执行后)\n" +
            "Observation: 编译成功\n\n" +
            "Thought: 所有步骤都成功完成了\n" +
            "Action: {\"final_answer\": \"✅ 任务完成！文件已创建并编译成功。\"}\n";
    }

    /**
     * 执行工具调用
     */
    private String executeToolCall(ToolCall toolCall) {
        try {
            Tool tool = toolRegistry.getTool(toolCall.getToolName());
            if (tool == null) {
                return "错误: 工具不存在 - " + toolCall.getToolName();
            }

            String result = tool.execute(toolCall.getParameters());
            return result;
        } catch (Exception e) {
            return "工具执行失败: " + e.getMessage();
        }
    }


    /**
     * LLM 响应封装 - 支持 ReAct 模式
     */
    public static class LLMResponse {
        private String thought;      // 思考过程
        private String content;       // 内容
        private ToolCall toolCall;    // 工具调用
        private String finalAnswer;   // 最终答案

        public boolean isToolCall() {
            return toolCall != null;
        }

        public boolean isFinalAnswer() {
            return finalAnswer != null && !finalAnswer.isEmpty();
        }

        public String getThought() {
            return thought;
        }

        public void setThought(String thought) {
            this.thought = thought;
        }

        public String getContent() {
            // 如果有最终答案，返回最终答案
            if (finalAnswer != null && !finalAnswer.isEmpty()) {
                return finalAnswer;
            }
            return content;
        }

        public ToolCall getToolCall() {
            return toolCall;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setToolCall(ToolCall toolCall) {
            this.toolCall = toolCall;
        }

        public void setFinalAnswer(String finalAnswer) {
            this.finalAnswer = finalAnswer;
        }
    }

    /**
     * 工具调用信息
     */
    public static class ToolCall {
        private String toolName;
        private Map<String, Object> parameters;

        public ToolCall(String toolName, Map<String, Object> parameters) {
            this.toolName = toolName;
            this.parameters = parameters;
        }

        public String getToolName() {
            return toolName;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }
    }
}

