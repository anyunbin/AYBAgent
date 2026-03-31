package org.example;

import org.example.agent.Agent;
import org.example.agent.llm.LLMClient;
import org.example.agent.llm.MockLLMClient;
import org.example.agent.tools.ToolRegistry;
import org.example.agent.tools.impl.ReadFileTool;
import org.example.agent.tools.impl.RunCommandTool;
import org.example.agent.tools.impl.WriteFileTool;

/**
 * 简化的 Agent 演示
 * 使用 Mock LLM 模拟真实的 Agent 工作流程
 */
public class SimpleAgentDemo {
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║          AI Agent 工作流程演示                            ║");
        System.out.println("║  展示 Agent 如何自主完成任务：分析→执行→验证→修复        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        // 1. 初始化工具注册表
        System.out.println("📦 初始化工具系统...");
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.registerTool(new WriteFileTool());
        toolRegistry.registerTool(new ReadFileTool());
        toolRegistry.registerTool(new RunCommandTool());
        System.out.println("✓ 已注册 3 个工具: write_file, read_file, run_command\n");

        // 2. 创建 Mock LLM 客户端（模拟真实 LLM 的决策过程）
        System.out.println("🤖 初始化 AI 模型...");
        LLMClient llmClient = new MockLLMClient();
        System.out.println("✓ 使用 Mock LLM (模拟真实 LLM 的决策过程)\n");

        // 3. 创建 Agent
        System.out.println("🚀 创建 Agent...");
        Agent agent = new Agent(llmClient, toolRegistry, 10);
        System.out.println("✓ Agent 已就绪，最大迭代次数: 10\n");

        // 4. 执行任务
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📝 用户任务: 创建一个计算器类并编译验证");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        String task = "创建一个计算器类并编译验证";
        String result = agent.execute(task);

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("📊 最终结果:");
        System.out.println(result);
        System.out.println("═══════════════════════════════════════════════════════════\n");

        // 5. 展示工作原理
        printWorkflowExplanation();
    }

    private static void printWorkflowExplanation() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                   工作原理解析                            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        System.out.println("🔄 Agent 执行流程：\n");

        System.out.println("1️⃣  用户输入任务");
        System.out.println("    ↓");
        System.out.println("2️⃣  构建提示词（系统提示 + 工具定义 + 对话历史）");
        System.out.println("    ↓");
        System.out.println("3️⃣  LLM 分析任务，决定使用工具");
        System.out.println("    ↓");
        System.out.println("4️⃣  执行工具（如 write_file 创建代码）");
        System.out.println("    ↓");
        System.out.println("5️⃣  将工具结果反馈给 LLM");
        System.out.println("    ↓");
        System.out.println("6️⃣  LLM 检查结果，决定下一步");
        System.out.println("    ├─ 成功 → 继续下一步");
        System.out.println("    └─ 失败 → 分析错误并修复（回到步骤 3）");
        System.out.println("    ↓");
        System.out.println("7️⃣  重复步骤 3-6 直到任务完成\n");

        System.out.println("💡 关键机制：\n");
        System.out.println("• Prompt Engineering: 通过提示词引导 LLM 行为");
        System.out.println("• Tool Calling: LLM 通过工具与外部系统交互");
        System.out.println("• Feedback Loop: 工具结果反馈给 LLM 形成闭环");
        System.out.println("• Self-Correction: LLM 根据错误信息自主修复\n");

        System.out.println("📚 核心提示词示例：\n");
        System.out.println("系统提示词:");
        System.out.println("  \"你是编程助手，能够自主完成编程任务。");
        System.out.println("   编写代码后必须编译验证，如果失败必须修复。\"\n");

        System.out.println("工具定义提示词:");
        System.out.println("  \"可用工具: write_file, run_command");
        System.out.println("   使用格式: {\\\"tool\\\": \\\"工具名\\\", \\\"parameters\\\": {...}}\"\n");

        System.out.println("思维链提示词:");
        System.out.println("  \"请思考: 1)当前状态 2)下一步行动 3)预期结果");
        System.out.println("   如果有错误，分析原因并提出修复方案。\"\n");

        System.out.println("这就是 OpenClaw、Claude Code 等 AI Agent 背后的核心原理！");
    }
}

