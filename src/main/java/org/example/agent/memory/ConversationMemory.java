package org.example.agent.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话记忆 - 保存对话历史
 */
public class ConversationMemory {
    private final List<Message> messages = new ArrayList<>();

    public void addUserMessage(String content) {
        messages.add(new Message("user", content));
    }

    public void addAssistantMessage(String content) {
        messages.add(new Message("assistant", content));
    }

    public void addToolResult(String toolName, String result) {
        messages.add(new Message("tool",
            String.format("工具 [%s] 执行结果:\n%s", toolName, result)));
    }

    /**
     * 添加观察结果（ReAct 模式）
     * Observation 是 Action 执行后的结果反馈
     */
    public void addObservation(String toolName, String observation) {
        messages.add(new Message("observation",
            String.format("[Observation] 工具 [%s] 执行结果:\n%s", toolName, observation)));
    }

    public String getConversationHistory() {
        StringBuilder history = new StringBuilder("对话历史:\n");
        for (Message msg : messages) {
            history.append(String.format("[%s]: %s\n", msg.role, msg.content));
        }
        return history.toString();
    }

    /**
     * 检查对话历史中是否有工具调用
     * 用于判断 Agent 是否已经开始执行任务
     */
    public boolean hasToolCalls() {
        for (Message msg : messages) {
            if ("tool".equals(msg.role)) {
                return true;
            }
        }
        return false;
    }

    private static class Message {
        String role;
        String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

