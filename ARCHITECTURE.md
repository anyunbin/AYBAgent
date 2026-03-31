# AI Agent 架构设计文档

## 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          用户层                                  │
│                      User Input / Output                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Agent 核心层                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Agent.execute()                                         │  │
│  │  • 接收用户输入                                          │  │
│  │  • 管理执行循环                                          │  │
│  │  • 协调各个组件                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└───┬──────────────────┬──────────────────┬──────────────────────┘
    │                  │                  │
    ▼                  ▼                  ▼
┌─────────┐    ┌──────────────┐    ┌─────────────┐
│  LLM    │    │     Tool     │    │   Memory    │
│ Client  │    │   Registry   │    │             │
└─────────┘    └──────────────┘    └─────────────┘
    │                  │                  │
    ▼                  ▼                  ▼
┌─────────┐    ┌──────────────┐    ┌─────────────┐
│ OpenAI  │    │  write_file  │    │ Conversation│
│ Claude  │    │  read_file   │    │   History   │
│ Gemini  │    │ run_command  │    │             │
└─────────┘    └──────────────┘    └─────────────┘
```

## 执行流程图

```
开始
 │
 ├─→ 用户输入任务
 │
 ├─→ 初始化 Agent
 │    • 配置 LLM 客户端
 │    • 注册工具
 │    • 设置最大迭代次数
 │
 ├─→ 进入执行循环 ─────────────────┐
 │                                │
 │   ┌────────────────────────────┘
 │   │
 │   ├─→ 步骤 1: 构建提示词
 │   │    ┌─────────────────────────────┐
 │   │    │ • 系统提示词（角色定义）      │
 │   │    │ • 工具定义（可用工具列表）    │
 │   │    │ • 对话历史（上下文）          │
 │   │    │ • 思维链引导（推理指导）      │
 │   │    └─────────────────────────────┘
 │   │
 │   ├─→ 步骤 2: 调用 LLM
 │   │    llmClient.chat(prompt)
 │   │         │
 │   │         ├─→ 返回文本 ──────→ 任务完成
 │   │         │
 │   │         └─→ 返回工具调用 ──→ 继续
 │   │
 │   ├─→ 步骤 3: 解析工具调用
 │   │    {
 │   │      "tool": "write_file",
 │   │      "parameters": {
 │   │        "path": "Main.java",
 │   │        "content": "..."
 │   │      }
 │   │    }
 │   │
 │   ├─→ 步骤 4: 执行工具
 │   │    toolRegistry.getTool(name).execute(params)
 │   │         │
 │   │         ├─→ 成功: 返回结果
 │   │         └─→ 失败: 返回错误信息
 │   │
 │   ├─→ 步骤 5: 保存工具结果
 │   │    memory.addToolResult(toolName, result)
 │   │
 │   ├─→ 步骤 6: 检查任务状态
 │   │    • 是否包含错误？
 │   │    • 是否达到预期？
 │   │    • 是否超过最大迭代？
 │   │         │
 │   │         ├─→ 完成 ──→ 返回结果
 │   │         │
 │   │         └─→ 未完成 ──→ 回到步骤 1
 │   │                       （LLM 会根据工具结果决定下一步）
 │   │
 │   └────────────────────────────────┘
 │
 └─→ 结束
```

## 自动编译验证流程

```
用户: "创建一个计算器类并编译验证"
 │
 ▼
┌─────────────────────────────────────────┐
│ 迭代 1: LLM 决策                         │
│ 思考: 需要先创建 Java 文件              │
│ 决定: 使用 write_file 工具              │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 执行: write_file("Calculator.java")    │
│ 结果: 文件创建成功                       │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 迭代 2: LLM 决策                         │
│ 思考: 文件已创建，现在需要编译          │
│ 决定: 使用 run_command("javac ...")     │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 执行: run_command("javac")              │
│ 结果: 编译失败 - 第 10 行缺少分号       │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 迭代 3: LLM 决策                         │
│ 分析: 检测到编译错误                    │
│ 思考: 需要修复代码                       │
│ 决定: 重新使用 write_file 修复代码      │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 执行: write_file(修复后的代码)          │
│ 结果: 文件更新成功                       │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 迭代 4: LLM 决策                         │
│ 思考: 代码已修复，重新编译              │
│ 决定: 再次使用 run_command               │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 执行: run_command("javac")              │
│ 结果: 编译成功！                         │
└─────────────────────────────────────────┘
 │
 ▼
┌─────────────────────────────────────────┐
│ 迭代 5: LLM 决策                         │
│ 分析: 所有步骤都成功                    │
│ 决定: 任务完成，返回结果                │
└─────────────────────────────────────────┘
 │
 ▼
返回用户: "✅ 计算器类已创建并编译成功！"
```

## 核心组件详解

### 1. Agent 核心

```java
public class Agent {
    private LLMClient llmClient;      // LLM 客户端
    private ToolRegistry toolRegistry; // 工具注册表
    private ConversationMemory memory; // 对话记忆
    private int maxIterations;         // 最大迭代次数

    public String execute(String userInput) {
        // 主执行循环
        for (int i = 0; i < maxIterations; i++) {
            // 1. 构建提示词
            String prompt = buildPrompt();

            // 2. 调用 LLM
            LLMResponse response = llmClient.chat(prompt);

            // 3. 处理响应
            if (response.isToolCall()) {
                // 执行工具并保存结果
                String result = executeTool(response.getToolCall());
                memory.addToolResult(result);

                // 检查是否完成
                if (isTaskComplete(result)) {
                    return "Success";
                }
            } else {
                return response.getContent();
            }
        }
    }
}
```

### 2. 提示词构建器

```java
private String buildPrompt() {
    return String.format("""
        %s  // 系统提示词

        %s  // 工具定义

        %s  // 对话历史

        %s  // 思维链引导
        """,
        getSystemPrompt(),
        getToolDefinitions(),
        memory.getConversationHistory(),
        getGuidancePrompt()
    );
}
```

### 3. 工具系统

```java
public interface Tool {
    String getName();
    String getDescription();
    String getParameters();
    String execute(Map<String, Object> parameters);
}

// 示例工具
public class WriteFileTool implements Tool {
    public String execute(Map<String, Object> params) {
        String path = (String) params.get("path");
        String content = (String) params.get("content");
        // 写入文件逻辑
        return "文件写入成功";
    }
}
```

### 4. 记忆管理

```java
public class ConversationMemory {
    private List<Message> messages;

    public void addUserMessage(String content) {
        messages.add(new Message("user", content));
    }

    public void addToolResult(String toolName, String result) {
        messages.add(new Message("tool",
            String.format("[%s] 执行结果: %s", toolName, result)));
    }

    public String getConversationHistory() {
        // 格式化对话历史
        return formatMessages(messages);
    }
}
```

## 关键设计原则

### 1. 单一职责原则

- **Agent**: 只负责协调，不处理具体逻辑
- **Tool**: 每个工具只做一件事
- **LLMClient**: 只负责与 LLM 通信
- **Memory**: 只负责存储对话历史

### 2. 开放封闭原则

- 添加新工具：实现 Tool 接口即可
- 更换 LLM：实现 LLMClient 接口即可
- 扩展功能：不需要修改核心代码

### 3. 依赖倒置原则

```java
// Agent 依赖接口，不依赖具体实现
public Agent(LLMClient llmClient, ...) {
    this.llmClient = llmClient;  // 接口
}

// 可以注入不同的实现
new Agent(new OpenAILLMClient(...));
new Agent(new ClaudeLLMClient(...));
new Agent(new MockLLMClient(...));  // 用于测试
```

## Prompt Engineering 分层架构

```
┌─────────────────────────────────────────────┐
│          第 1 层: 系统提示词                 │
│      定义 Agent 的角色、能力、原则           │
│  "你是编程助手，能够自主完成编程任务"        │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│          第 2 层: 工具定义提示词             │
│        告诉 LLM 有哪些工具可用               │
│  "可用工具: write_file, run_command..."      │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│          第 3 层: 上下文提示词               │
│          提供对话历史和当前状态              │
│  "[user]: 创建计算器类"                      │
│  "[tool]: 编译失败 - 缺少分号"               │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│          第 4 层: 思维链提示词               │
│          引导 LLM 进行推理                   │
│  "请思考: 1)当前状态 2)下一步行动"           │
└─────────────────────────────────────────────┘
                    │
                    ▼
                 LLM 输出
```

## 错误处理机制

```
错误发生
 │
 ├─→ 1. 工具执行返回错误信息
 │     "编译失败: line 10: ';' expected"
 │
 ├─→ 2. 错误信息保存到记忆
 │     memory.addToolResult("run_command", errorMsg)
 │
 ├─→ 3. 构建包含错误信息的提示词
 │     提示词中包含: "[tool]: 编译失败..."
 │
 ├─→ 4. LLM 分析错误
 │     LLM 看到错误信息，理解出了什么问题
 │
 ├─→ 5. LLM 生成修复方案
 │     决定修改代码，在第 10 行添加分号
 │
 ├─→ 6. 执行修复（使用工具）
 │     write_file(修复后的代码)
 │
 ├─→ 7. 重新验证
 │     run_command("javac ...") → 成功
 │
 └─→ 8. 任务完成
```

## 扩展点

### 1. 添加新工具

```java
public class GitCommitTool implements Tool {
    public String getName() { return "git_commit"; }

    public String execute(Map<String, Object> params) {
        String message = (String) params.get("message");
        // 执行 git commit
        return "提交成功";
    }
}

// 注册工具
toolRegistry.registerTool(new GitCommitTool());
```

### 2. 更换 LLM 提供商

```java
public class ClaudeLLMClient implements LLMClient {
    public LLMResponse chat(String prompt) {
        // 调用 Claude API
        return parseClaudeResponse(response);
    }
}

// 使用 Claude
Agent agent = new Agent(new ClaudeLLMClient(...), ...);
```

### 3. 增强记忆系统

```java
public class VectorMemory extends ConversationMemory {
    private VectorDB vectorDB;

    public String getRelevantContext(String query) {
        // 使用向量数据库检索相关历史
        return vectorDB.search(query);
    }
}
```

### 4. 任务规划器

```java
public class TaskPlanner {
    public List<SubTask> decompose(String mainTask) {
        // 使用 LLM 分解任务
        return llm.planTasks(mainTask);
    }
}
```

## 性能优化

### 1. 提示词优化
- 压缩对话历史（只保留最近 N 条）
- 提取关键信息而非全部内容
- 使用摘要技术

### 2. 并行执行
- 独立任务并行处理
- 多个工具同时调用
- 异步 LLM 调用

### 3. 缓存机制
- 缓存 LLM 响应（相同提示词）
- 缓存工具执行结果
- 缓存编译结果

## 总结

这个架构实现了：

1. **模块化设计**: 各组件职责清晰，易于维护
2. **可扩展性**: 轻松添加新工具、更换 LLM
3. **自主性**: 通过反馈循环实现自主决策和纠错
4. **智能性**: 通过 Prompt Engineering 引导 LLM 行为

这就是现代 AI Agent（OpenClaw、Claude Code 等）的核心架构！

