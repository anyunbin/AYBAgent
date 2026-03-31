package org.example.agent.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具注册表 - 管理所有可用工具
 */
public class ToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();

    /**
     * 注册工具
     */
    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * 获取工具
     */
    public Tool getTool(String toolName) {
        return tools.get(toolName);
    }

    /**
     * 获取所有工具
     */
    public Collection<Tool> getAllTools() {
        return tools.values();
    }
}

