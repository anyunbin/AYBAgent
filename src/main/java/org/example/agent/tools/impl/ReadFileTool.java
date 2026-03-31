package org.example.agent.tools.impl;

import org.example.agent.tools.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 读文件工具
 */
public class ReadFileTool implements Tool {
    @Override
    public String getName() {
        return "read_file";
    }

    @Override
    public String getDescription() {
        return "读取指定文件的内容";
    }

    @Override
    public String getParameters() {
        return "path: 文件路径(必需)";
    }

    @Override
    public String execute(Map<String, Object> parameters) throws Exception {
        String path = (String) parameters.get("path");

        if (path == null) {
            throw new IllegalArgumentException("path 参数不能为空");
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            return "文件内容:\n" + content;
        } catch (IOException e) {
            throw new Exception("读取文件失败: " + e.getMessage());
        }
    }
}

