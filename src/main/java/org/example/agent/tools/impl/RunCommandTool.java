package org.example.agent.tools.impl;

import org.example.agent.tools.Tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 运行命令工具 - 用于编译、运行、测试等
 */
public class RunCommandTool implements Tool {
    @Override
    public String getName() {
        return "run_command";
    }

    @Override
    public String getDescription() {
        return "执行系统命令（如编译、运行测试等）";
    }

    @Override
    public String getParameters() {
        return "command: 要执行的命令(必需), workdir: 工作目录(可选)";
    }

    @Override
    public String execute(Map<String, Object> parameters) throws Exception {
        String command = (String) parameters.get("command");
        String workdir = (String) parameters.get("workdir");

        if (command == null) {
            throw new IllegalArgumentException("command 参数不能为空");
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();

            // 使用 shell 执行命令（支持管道等）
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("sh", "-c", command);
            }

            if (workdir != null) {
                processBuilder.directory(new java.io.File(workdir));
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            String result = output.toString();
            if (exitCode == 0) {
                return "命令执行成功:\n" + result;
            } else {
                return "命令执行失败 (退出码: " + exitCode + "):\n" + result;
            }
        } catch (Exception e) {
            throw new Exception("执行命令失败: " + e.getMessage());
        }
    }
}

