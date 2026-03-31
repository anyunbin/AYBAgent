package org.example.agent.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.agent.Agent.LLMResponse;
import org.example.agent.Agent.ToolCall;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * FRIDAY 平台 LLM 客户端实现
 * 对齐 OpenAI ChatCompletions 标准接口
 */
public class OpenAILLMClient implements LLMClient {
    private final String appId;  // FRIDAY 平台的 App ID
    private final String apiUrl;
    private final String model;
    private final Gson gson = new Gson();

    public OpenAILLMClient(String appId, String apiUrl, String model) {
        this.appId = appId;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Override
    public LLMResponse chat(String prompt) {
        try {
            // 打印分隔线
            String separator = "================================================================================";
            System.out.println("\n" + separator);
            System.out.println("📤 发送给模型的 Prompt:");
            System.out.println(separator);
            System.out.println(prompt);
            System.out.println(separator + "\n");

            // 构建请求体 - 符合 OpenAI ChatCompletions 标准
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", 0.7);

            // 构建消息数组
            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            requestBody.add("messages", messages);

            // 添加 user 参数用于问题追踪
            requestBody.addProperty("user", "agent-demo-" + System.currentTimeMillis());

            System.out.println("📋 请求配置:");
            System.out.println("  - 模型: " + model);
            System.out.println("  - 温度: 0.7");
            System.out.println("  - API: " + apiUrl);
            System.out.println();

            // 发送 HTTP 请求
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            // FRIDAY 平台使用 App ID 作为鉴权方式,放在 Authorization header 中
            connection.setRequestProperty("Authorization", appId);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);  // 30秒连接超时
            connection.setReadTimeout(60000);     // 60秒读取超时

            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
            } else {
                // 读取错误信息
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                LLMResponse errorResponse = new LLMResponse();
                errorResponse.setContent("API 调用失败 (HTTP " + responseCode + "): " + response.toString());
                return errorResponse;
            }

            // 解析响应
            return parseResponse(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            LLMResponse errorResponse = new LLMResponse();
            errorResponse.setContent("LLM 调用失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 解析 LLM 响应，提取工具调用或文本内容
     */
    private LLMResponse parseResponse(String jsonResponse) {
        LLMResponse response = new LLMResponse();

        try {
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
            String content = json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            // 解析 ReAct 格式的响应
            // 提取 Thought (支持多种格式)
            if (content.contains("Thought") || content.contains("**Thought**")) {
                String thought = extractSection(content, "Thought:", "Action:");
                if (thought != null) {
                    response.setThought(thought.trim());
                }
            }

            // 提取 Action 部分的 JSON (支持多种格式)
            if (content.contains("Action") || content.contains("**Action**")) {
                String actionJson = extractActionJson(content);
                if (actionJson != null) {
                    // 检查是否是 final_answer
                    if (actionJson.contains("\"final_answer\"")) {
                        String finalAnswer = parseFinalAnswer(actionJson);
                        if (finalAnswer != null) {
                            response.setFinalAnswer(finalAnswer);
                        } else {
                            response.setContent(content);
                        }
                    } else if (actionJson.contains("\"tool\"")) {
                        // 工具调用
                        ToolCall toolCall = parseToolCall(actionJson);
                        if (toolCall != null) {
                            response.setToolCall(toolCall);
                        } else {
                            response.setContent(content);
                        }
                    } else {
                        response.setContent(content);
                    }
                } else {
                    response.setContent(content);
                }
            } else {
                response.setContent(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContent(jsonResponse);
        }

        return response;
    }

    /**
     * 提取指定章节的内容
     */
    private String extractSection(String content, String startMarker, String endMarker) {
        // 支持多种格式: "Thought:", "**Thought**:", "Thought :", 等
        int start = -1;
        String[] markers = {startMarker, "**" + startMarker.replace(":", "**:"), "**" + startMarker.replace(":", "**")};
        for (String marker : markers) {
            start = content.indexOf(marker);
            if (start != -1) {
                start += marker.length();
                break;
            }
        }
        if (start == -1) return null;

        int end = content.indexOf(endMarker, start);
        if (end == -1) {
            // 也尝试查找 Markdown 格式的结束标记
            end = content.indexOf("**" + endMarker.replace(":", "**"), start);
        }
        if (end == -1) {
            return content.substring(start).trim();
        }
        return content.substring(start, end).trim();
    }

    /**
     * 提取 Action 部分的 JSON
     */
    private String extractActionJson(String content) {
        // 支持多种格式: "Action:", "**Action**:", 等
        int actionIndex = -1;
        String[] actionMarkers = {"Action:", "**Action**:", "**Action**"};
        for (String marker : actionMarkers) {
            actionIndex = content.indexOf(marker);
            if (actionIndex != -1) break;
        }
        if (actionIndex == -1) return null;

        int jsonStart = content.indexOf("{", actionIndex);
        if (jsonStart == -1) return null;

        // 找到匹配的右花括号
        int braceCount = 0;
        int jsonEnd = -1;
        for (int i = jsonStart; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    jsonEnd = i + 1;
                    break;
                }
            }
        }

        if (jsonEnd == -1) return null;
        return content.substring(jsonStart, jsonEnd);
    }

    /**
     * 解析 final_answer
     */
    private String parseFinalAnswer(String jsonStr) {
        try {
            JsonObject json = gson.fromJson(jsonStr, JsonObject.class);
            if (json.has("final_answer")) {
                return json.get("final_answer").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析工具调用
     */
    private ToolCall parseToolCall(String content) {
        try {
            JsonObject json = gson.fromJson(content, JsonObject.class);
            String toolName = json.get("tool").getAsString();

            Map<String, Object> parameters = new HashMap<>();
            JsonObject paramsJson = json.getAsJsonObject("parameters");
            for (String key : paramsJson.keySet()) {
                parameters.put(key, paramsJson.get(key).getAsString());
            }

            return new ToolCall(toolName, parameters);
        } catch (Exception e) {
            return null;
        }
    }
}

