package org.example.agent.tools;

import java.util.Map;

/**
 * 工具接口 - 所有工具必须实现此接口
 */
public interface Tool {
    /**
     * 获取工具名称
     */
    String getName();

    /**
     * 获取工具描述
     */
    String getDescription();

    /**
     * 获取参数定义
     */
    String getParameters();

    /**
     * 执行工具
     * @param parameters 参数
     * @return 执行结果
     */
    String execute(Map<String, Object> parameters) throws Exception;
}

