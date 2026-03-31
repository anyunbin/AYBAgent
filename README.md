# AYB Agent - AI 编程助手框架

一个完整的 AI Agent 实现框架，演示如何构建类似 OpenClaw、Claude Code 的智能编程助手。

## 核心特性

### 1. **自主任务执行**
- 理解用户意图
- 自动分解任务
- 选择合适的工具
- 执行并验证结果
- 遇到错误自动修复

### 2. **工具系统**
Agent 可以使用各种工具完成任务：
- `write_file`: 写入文件
- `read_file`: 读取文件
- `run_command`: 执行命令（编译、运行、测试等）

### 3. **智能反馈循环**
```
用户输入 → LLM分析 → 工具执行 → 结果验证 →
  ↑                                    ↓
  └────── 如果失败，分析错误并重试 ←────┘
```

## 架构设计

### 核心组件

```
Agent
├── LLMClient (大语言模型客户端)
│   └── OpenAILLMClient
├── ToolRegistry (工具注册表)
│   ├── WriteFileTool
│   ├── ReadFileTool
│   └── RunCommandTool
└── ConversationMemory (对话记忆)
```

### 工作流程

```java
// 1. 用户输入任务
String task = "创建一个计算器类并编译验证";

// 2. Agent 处理
Agent agent = new Agent(llmClient, toolRegistry, maxIterations);
String result = agent.execute(task);

// 3. 内部流程
while (未完成 && 迭代次数 < 最大值) {
    // 3.1 构建提示词（系统提示 + 工具定义 + 对话历史）
    String prompt = buildPrompt();

    // 3.2 调用 LLM
    LLMResponse response = llmClient.chat(prompt);

    // 3.3 如果需要调用工具
    if (response.isToolCall()) {
        // 执行工具
        String toolResult = executeTool(response.getToolCall());

        // 3.4 将结果反馈给 LLM
        memory.addToolResult(toolResult);

        // 3.5 检查是否成功
        if (包含错误) {
            // 继续循环，让 LLM 分析错误并修复
            continue;
        } else {
            // 任务完成
            break;
        }
    }
}
```

## 提示词工程详解

### 1. 系统提示词 (System Prompt)

定义 Agent 的角色和能力：

```
你是一个专业的编程助手 Agent。

你的工作流程：
1. 理解用户需求
2. 分解任务为具体步骤
3. 使用可用工具执行每个步骤
4. 验证执行结果
5. 如果出现错误，分析错误并修复
6. 重复直到任务完成

重要原则：
- 编写代码后必须进行编译验证
- 如果编译失败，分析错误信息并修复
- 始终验证最终结果是否符合用户预期
```

### 2. 工具定义提示词

让 LLM 知道可以使用哪些工具：

```
可用工具列表：

工具名: write_file
描述: 将内容写入指定文件
参数: path: 文件路径(必需), content: 文件内容(必需)

工具名: run_command
描述: 执行系统命令（如编译、运行测试等）
参数: command: 要执行的命令(必需)

使用工具时，以 JSON 格式输出：
{"tool": "工具名", "parameters": {"参数名": "参数值"}}
```

### 3. 思维链提示词 (Chain of Thought)

引导 LLM 逐步思考：

```
请按以下步骤思考：
1. 当前的任务状态是什么？
2. 下一步应该做什么？
3. 需要使用哪个工具？
4. 期望的结果是什么？

如果上一步有错误，请分析错误原因并提出修复方案。
```

### 4. 对话历史

保持上下文连贯性：

```
对话历史:
[user]: 创建一个计算器类
[assistant]: 好的，我将创建 Calculator.java 文件
[tool]: 工具 [write_file] 执行结果: 成功写入文件
[assistant]: 现在我将编译这个文件
[tool]: 工具 [run_command] 执行结果: 编译失败 - 缺少分号
[assistant]: 我发现了错误，现在修复...
```

## 关键实现细节

### 1. 编译验证循环

```java
// Agent 自动进行编译验证
1. 写入代码 → write_file
2. 编译代码 → run_command("javac Calculator.java")
3. 检查结果:
   - 成功 → 继续
   - 失败 → 分析错误 → 修改代码 → 重新编译
```

### 2. 错误修复机制

当编译失败时：
```
LLM 收到错误信息：
"编译失败: Calculator.java:10: error: ';' expected"

LLM 分析：
1. 识别错误类型（缺少分号）
2. 定位错误位置（第10行）
3. 生成修复方案
4. 使用 write_file 重新写入修复后的代码
5. 再次编译验证
```

### 3. 任务完成判断

```java
private boolean isTaskComplete(String toolResult) {
    // 检查是否包含错误信息
    return !toolResult.toLowerCase().contains("error")
        && !toolResult.toLowerCase().contains("failed");
}
```

## 使用方法

### 1. 配置环境

```bash
# 设置 OpenAI API Key
export OPENAI_API_KEY="your-api-key"

# 或者使用其他 LLM 服务（如 Claude、Gemini 等）
```

### 2. 运行示例

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.example.AgentDemo"
```

### 3. 自定义工具

```java
public class CustomTool implements Tool {
    @Override
    public String getName() {
        return "my_tool";
    }

    @Override
    public String getDescription() {
        return "工具描述";
    }

    @Override
    public String getParameters() {
        return "参数说明";
    }

    @Override
    public String execute(Map<String, Object> parameters) throws Exception {
        // 实现工具逻辑
        return "执行结果";
    }
}

// 注册工具
toolRegistry.registerTool(new CustomTool());
```

## 扩展方向

### 1. 增强 LLM 能力
- 支持 Function Calling（原生工具调用）
- 使用更强大的模型（GPT-4、Claude 3.5）
- 实现流式输出

### 2. 丰富工具生态
- `search_web`: 网络搜索
- `read_docs`: 读取文档
- `git_commit`: Git 操作
- `run_tests`: 自动化测试
- `debug_code`: 调试工具

### 3. 智能规划
- 多步骤任务规划
- 并行任务执行
- 依赖关系管理

### 4. 安全机制
- 工具权限控制
- 沙箱执行环境
- 代码审查机制

## 核心代码示例

### Agent 核心循环

```java
public String execute(String userInput) {
    memory.addUserMessage(userInput);

    for (int i = 0; i < maxIterations; i++) {
        // 1. 构建完整提示词
        String prompt = buildPrompt(); // 系统提示 + 工具定义 + 对话历史

        // 2. 调用 LLM
        LLMResponse response = llmClient.chat(prompt);

        // 3. 判断是否需要调用工具
        if (response.isToolCall()) {
            // 4. 执行工具
            String toolResult = executeToolCall(response.getToolCall());

            // 5. 保存工具结果到记忆
            memory.addToolResult(toolResult);

            // 6. 检查是否完成
            if (isTaskComplete(toolResult)) {
                return "任务完成";
            }
            // 否则继续循环，LLM 会根据工具结果做出下一步决策
        } else {
            // LLM 认为任务完成
            return response.getContent();
        }
    }

    return "超过最大迭代次数";
}
```

## 总结

这个框架展示了 AI Agent 的核心原理：

1. **Prompt Engineering 是核心**：通过精心设计的提示词引导 LLM 行为
2. **工具调用是关键**：让 LLM 能够与外部系统交互
3. **反馈循环是灵魂**：通过迭代实现自主纠错和优化
4. **记忆管理是基础**：保持上下文连贯性

通过这些机制，Agent 可以像人类程序员一样：
- 理解需求
- 编写代码
- 编译验证
- 发现错误
- 修复问题
- 直到完成任务

这就是 OpenClaw、Claude Code 等工具背后的核心原理！

