package org.example;

import org.example.agent.Agent;
import org.example.agent.llm.LLMClient;
import org.example.agent.llm.MockLLMClient;
import org.example.agent.tools.ToolRegistry;
import org.example.agent.tools.impl.ReadFileTool;
import org.example.agent.tools.impl.RunCommandTool;
import org.example.agent.tools.impl.WriteFileTool;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 数据处理任务演示
 * 展示 Agent 如何处理各种数据处理任务，而不仅仅是编程
 */
public class DataProcessingDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║        AI Agent 数据处理能力演示                          ║");
        System.out.println("║  Agent 不仅能编程，还能处理数据、执行 bash 命令等         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        // 准备测试数据
        prepareTestData();

        // 初始化 Agent
        ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.registerTool(new WriteFileTool());
        toolRegistry.registerTool(new ReadFileTool());
        toolRegistry.registerTool(new RunCommandTool());

        LLMClient llmClient = new MockLLMClient();
        Agent agent = new Agent(llmClient, toolRegistry, 10);

        // 演示各种任务类型
        demonstrateTasks();
    }

    private static void prepareTestData() throws IOException {
        System.out.println("📦 准备测试数据...\n");

        // 创建示例日志文件
        String logContent = "2024-01-15 10:23:45 INFO User login successful: user123\n" +
            "2024-01-15 10:24:12 ERROR Database connection failed: timeout\n" +
            "2024-01-15 10:25:30 INFO User logout: user123\n" +
            "2024-01-15 10:26:05 ERROR File not found: /data/config.xml\n" +
            "2024-01-15 10:27:18 WARNING Disk space low: 85% used\n" +
            "2024-01-15 10:28:22 INFO User login successful: user456\n" +
            "2024-01-15 10:29:41 ERROR Network error: connection refused\n" +
            "2024-01-15 10:30:15 INFO Data sync completed\n";
        writeFile("test_logs.txt", logContent);
        System.out.println("✓ 创建测试日志文件: test_logs.txt");

        // 创建示例 CSV 数据
        String csvContent = "name,age,city,salary\n" +
            "张三,28,北京,15000\n" +
            "李四,32,上海,22000\n" +
            "王五,25,深圳,18000\n" +
            "赵六,35,广州,25000\n" +
            "钱七,29,北京,19000\n" +
            "孙八,31,上海,21000\n";
        writeFile("employees.csv", csvContent);
        System.out.println("✓ 创建测试 CSV 文件: employees.csv");

        // 创建示例 JSON 数据
        String jsonContent = "[\n" +
            "  {\"id\": 1, \"product\": \"笔记本电脑\", \"price\": 5999, \"stock\": 50},\n" +
            "  {\"id\": 2, \"product\": \"台式机\", \"price\": 3999, \"stock\": 30},\n" +
            "  {\"id\": 3, \"product\": \"显示器\", \"price\": 1299, \"stock\": 100},\n" +
            "  {\"id\": 4, \"product\": \"键盘\", \"price\": 299, \"stock\": 200},\n" +
            "  {\"id\": 5, \"product\": \"鼠标\", \"price\": 99, \"stock\": 500}\n" +
            "]\n";
        writeFile("products.json", jsonContent);
        System.out.println("✓ 创建测试 JSON 文件: products.json\n");
    }

    private static void writeFile(String filename, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    private static void demonstrateTasks() {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📊 Agent 可以处理的任务类型示例");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        System.out.println("✅ 1. 编程任务");
        System.out.println("   示例: 创建一个 Java 类并编译");
        System.out.println("   工具: write_file → run_command(javac)\n");

        System.out.println("✅ 2. 数据处理任务（日志分析）");
        System.out.println("   示例: 从日志文件中提取所有 ERROR 记录");
        System.out.println("   工具: run_command(grep 'ERROR' test_logs.txt)");
        System.out.println("   或: run_command(awk '/ERROR/ {print}' test_logs.txt)\n");

        System.out.println("✅ 3. 数据统计任务");
        System.out.println("   示例: 统计日志中不同级别的数量");
        System.out.println("   工具: run_command(awk '{print $3}' test_logs.txt | sort | uniq -c)\n");

        System.out.println("✅ 4. CSV 数据处理");
        System.out.println("   示例: 计算员工平均工资");
        System.out.println("   工具: run_command(awk -F',' 'NR>1 {sum+=$4; count++} END {print sum/count}' employees.csv)\n");

        System.out.println("✅ 5. 文本过滤和提取");
        System.out.println("   示例: 提取所有北京的员工");
        System.out.println("   工具: run_command(grep '北京' employees.csv)\n");

        System.out.println("✅ 6. 数据转换");
        System.out.println("   示例: 将 CSV 转换为特定格式");
        System.out.println("   工具: run_command(awk -F',' 'NR>1 {printf \"%s: %s元\\n\", $1, $4}' employees.csv)\n");

        System.out.println("✅ 7. JSON 数据处理");
        System.out.println("   示例: 使用 jq 处理 JSON（如果系统有 jq）");
        System.out.println("   工具: run_command(jq '.[] | select(.price > 1000)' products.json)\n");

        System.out.println("✅ 8. 文件查找");
        System.out.println("   示例: 查找所有 .java 文件");
        System.out.println("   工具: run_command(find . -name '*.java')\n");

        System.out.println("✅ 9. 批量操作");
        System.out.println("   示例: 批量重命名文件");
        System.out.println("   工具: run_command(for f in *.txt; do mv \"$f\" \"backup_$f\"; done)\n");

        System.out.println("✅ 10. 系统监控");
        System.out.println("   示例: 检查磁盘使用情况");
        System.out.println("   工具: run_command(df -h)\n");

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("💡 实际执行示例");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        // 实际执行一些示例
        executeExamples();
    }

    private static void executeExamples() {
        System.out.println("示例 1: 提取日志中的 ERROR 记录");
        System.out.println("命令: grep 'ERROR' test_logs.txt");
        executeCommand("grep 'ERROR' test_logs.txt");

        System.out.println("\n示例 2: 统计日志级别数量");
        System.out.println("命令: awk '{print $3}' test_logs.txt | sort | uniq -c");
        executeCommand("awk '{print $3}' test_logs.txt | sort | uniq -c");

        System.out.println("\n示例 3: 提取北京员工");
        System.out.println("命令: grep '北京' employees.csv");
        executeCommand("grep '北京' employees.csv");

        System.out.println("\n示例 4: 计算平均工资");
        System.out.println("命令: awk -F',' 'NR>1 {sum+=$4; count++} END {print \"平均工资: \" sum/count}' employees.csv");
        executeCommand("awk -F',' 'NR>1 {sum+=$4; count++} END {print \"平均工资: \" sum/count}' employees.csv");

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("📚 Agent 如何处理这些任务？");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        printAgentWorkflow();
    }

    private static void executeCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );

            System.out.println("输出:");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }

            process.waitFor();
        } catch (Exception e) {
            System.out.println("  执行失败: " + e.getMessage());
        }
    }

    private static void printAgentWorkflow() {
        System.out.println("当用户说: \"从日志文件中提取所有错误记录\"");
        System.out.println();
        System.out.println("Agent 的处理流程:");
        System.out.println();
        System.out.println("1️⃣  LLM 分析任务类型");
        System.out.println("   识别: 这是数据处理任务（关键词: 提取、日志）");
        System.out.println("   决策: 使用 bash 命令处理");
        System.out.println();
        System.out.println("2️⃣  LLM 选择工具");
        System.out.println("   工具: run_command");
        System.out.println("   命令: grep 'ERROR' test_logs.txt");
        System.out.println();
        System.out.println("3️⃣  执行工具");
        System.out.println("   执行 grep 命令");
        System.out.println("   返回匹配的行");
        System.out.println();
        System.out.println("4️⃣  验证结果");
        System.out.println("   检查输出是否符合预期");
        System.out.println("   是否提取了所有 ERROR 记录");
        System.out.println();
        System.out.println("5️⃣  返回用户");
        System.out.println("   格式化输出结果");
        System.out.println("   任务完成");
        System.out.println();

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔑 关键提示词片段");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        System.out.println("系统提示词中包含:");
        System.out.println("对于数据处理任务：优先使用 bash 命令\n" +
            "- 文本处理: grep, awk, sed, cut\n" +
            "- 数据统计: sort, uniq, wc\n" +
            "- 文件操作: find, ls, cat\n\n" +
            "任务类型识别：\n" +
            "- \"处理数据\"、\"统计\"、\"过滤\"、\"提取\" → 数据处理任务\n" +
            "- 使用 run_command 工具执行 bash 命令\n");

        System.out.println("\n这样 Agent 就知道:");
        System.out.println("- 何时使用 bash 命令而不是编写程序");
        System.out.println("- 选择哪种 bash 命令（grep/awk/sed）");
        System.out.println("- 如何验证处理结果");
        System.out.println();

        System.out.println("总结: Agent 的通用性来自于:");
        System.out.println("1. 通用的系统提示词（支持多种任务类型）");
        System.out.println("2. 灵活的工具系统（run_command 可以执行任何命令）");
        System.out.println("3. 智能的任务识别（通过提示词引导 LLM 识别任务类型）");
        System.out.println("4. 自适应的执行策略（根据任务类型选择最佳方案）");
    }
}

