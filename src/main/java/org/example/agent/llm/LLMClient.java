package org.example.agent.llm;

import org.example.agent.Agent.LLMResponse;

/**
 * LLM 客户端接口 - 与大语言模型交互
 */
public interface LLMClient {
    /**
     * 发送聊天请求
     * @param prompt 完整的提示词
     * @return LLM 响应
     */
    LLMResponse chat(String prompt);
}

