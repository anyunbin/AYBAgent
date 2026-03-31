package org.example.agent.tools.impl;

import org.example.agent.tools.Tool;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * 写文件工具
 */
public class WriteFileTool implements Tool {
    @Override
    public String getName() {
        return "write_file";
    }

    @Override
    public String getDescription() {
        return "将内容写入指定文件";
    }

    @Override
    public String getParameters() {
        return "path: 文件路径(必需), content: 文件内容(必需)";
    }

    @Override
    public String execute(Map<String, Object> parameters) throws Exception {
        String path = (String) parameters.get("path");
        String content = (String) parameters.get("content");

        if (path == null || content == null) {
            throw new IllegalArgumentException("path 和 content 参数不能为空");
        }

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            return "成功写入文件: " + path;
        } catch (IOException e) {
            throw new Exception("写入文件失败: " + e.getMessage());
        }
    }
}

