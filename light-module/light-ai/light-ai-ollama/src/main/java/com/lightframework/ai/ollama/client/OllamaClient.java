package com.lightframework.ai.ollama.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lightframework.ai.ollama.bean.ChatMessage;
import com.lightframework.ai.ollama.config.OllamaConfig;
import com.lightframework.common.LightException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ollama Java 客户端
 * 提供对 Ollama API 的封装，支持 generate 和 chat 两种模式
 * 支持流式响应和一次性响应、思考模式、结构化输出和视觉多模态模型
 *
 * JSON Schema 输出通过系统强化提示词实现，不使用 API 的 format 参数，
 * 以避免 format 参数对模型思考能力的抑制
 */
@Slf4j
public class OllamaClient {

    /**
     * 默认配置
     */
    private OllamaConfig defaultConfig;

    /**
     * 线程池，用于处理流式请求
     */
    private ExecutorService executorService;

    /**
     * 默认构造函数，使用默认配置
     */
    public OllamaClient() {
        this.defaultConfig = new OllamaConfig();
    }

    /**
     * 使用指定配置构造客户端
     * @param config 配置对象
     */
    public OllamaClient(OllamaConfig config) {
        this.defaultConfig = config;
    }

    /**
     * 使用指定模型构造客户端
     * @param model 模型名称
     */
    public OllamaClient(String model) {
        this.defaultConfig = new OllamaConfig(model);
    }

    /**
     * 使用指定地址和模型构造客户端
     * @param baseUrl Ollama 服务地址
     * @param model 模型名称
     */
    public OllamaClient(String baseUrl, String model) {
        this.defaultConfig = new OllamaConfig(baseUrl, model);
    }

    /**
     * 获取线程池（懒加载）
     * @return ExecutorService 实例
     */
    private ExecutorService getExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }

    // ===== Generate 模式 =====

    /**
     * 生成模式 - 使用默认配置
     * @param prompt 提示词
     * @return 模型响应内容
     */
    public String generate(String prompt) {
        return generate(prompt, null);
    }

    /**
     * 生成模式 - 一次性请求返回
     * @param prompt 提示词
     * @param config 配置对象（可选，为空则使用默认配置）
     * @return 模型响应内容
     */
    public String generate(String prompt, OllamaConfig config) {
        return generate(prompt, null, config);
    }

    /**
     * 生成模式 - 支持图片输入（多模态）
     * @param prompt 提示词
     * @param images 图片列表（支持多模态视觉模型）
     * @param config 配置对象（可选，为空则使用默认配置）
     * @return 模型响应内容
     */
    public String generate(String prompt, List<ChatMessage.ImagePart> images, OllamaConfig config) {
        OllamaConfig effectiveConfig = mergeConfig(config);

        Map<String, Object> requestBody = buildGenerateRequest(prompt, images, effectiveConfig);

        String url = effectiveConfig.getBaseUrl() + "/api/generate";

        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .setConnectionTimeout(effectiveConfig.getConnectTimeout())
                .setReadTimeout(effectiveConfig.getReadTimeout())
                .execute()) {

            if (!response.isOk()) {
                throw new LightException("Ollama API request failed: " + response.getStatus());
            }

            String responseBody = response.body();
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);

            if (jsonResponse.containsKey("error")) {
                throw new LightException("Ollama API error: " + jsonResponse.getStr("error"));
            }

            String content = jsonResponse.getStr("response");

            // jsonSchema 模式：从 response 中提取纯 JSON，response 为空时从 thinking 中提取
            if (effectiveConfig.getJsonSchema() != null) {
                if (content == null || content.isEmpty()) {
                    String thinkingContent = jsonResponse.getStr("thinking");
                    String extracted = tryExtractJsonFromText(thinkingContent);
                    if (extracted != null) {
                        content = extracted;
                    }
                } else {
                    String extracted = tryExtractJsonFromText(content);
                    if (extracted != null) {
                        content = extracted;
                    }
                }
            }

            return content;

        } catch (Exception e) {
            log.error("Generate request failed", e);
            throw new LightException("Generate request failed", e);
        }
    }

    // ===== Chat 模式 =====

    /**
     * 聊天模式 - 使用默认配置
     * @param messages 消息列表
     * @return 模型响应内容
     */
    public String chat(List<ChatMessage> messages) {
        return chat(messages, null);
    }

    /**
     * 聊天模式 - 一次性请求返回（多轮对话）
     * @param messages 消息列表
     * @param config 配置对象（可选，为空则使用默认配置）
     * @return 模型响应内容
     */
    public String chat(List<ChatMessage> messages, OllamaConfig config) {
        OllamaConfig effectiveConfig = mergeConfig(config);

        Map<String, Object> requestBody = buildChatRequest(messages, effectiveConfig);

        String url = effectiveConfig.getBaseUrl() + "/api/chat";

        try (HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .setConnectionTimeout(effectiveConfig.getConnectTimeout())
                .setReadTimeout(effectiveConfig.getReadTimeout())
                .execute()) {

            if (!response.isOk()) {
                throw new LightException("Ollama API request failed: " + response.getStatus());
            }

            String responseBody = response.body();
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);

            if (jsonResponse.containsKey("error")) {
                throw new LightException("Ollama API error: " + jsonResponse.getStr("error"));
            }

            JSONObject messageObj = jsonResponse.getJSONObject("message");
            String content = messageObj.getStr("content");

            // jsonSchema 模式：从 content 中提取纯 JSON，content 为空时从 thinking 中提取
            if (effectiveConfig.getJsonSchema() != null) {
                if (content == null || content.isEmpty()) {
                    String thinkingContent = messageObj.getStr("thinking");
                    String extracted = tryExtractJsonFromText(thinkingContent);
                    if (extracted != null) {
                        content = extracted;
                    }
                } else {
                    String extracted = tryExtractJsonFromText(content);
                    if (extracted != null) {
                        content = extracted;
                    }
                }
            }

            return content;

        } catch (Exception e) {
            log.error("Chat request failed", e);
            throw new LightException("Chat request failed", e);
        }
    }

    // ===== 流式 Generate 模式 =====

    /**
     * 流式生成模式 - 使用默认配置
     * @param prompt 提示词
     * @param callback 回调函数
     */
    public void generateStream(String prompt, StreamCallback callback) {
        generateStream(prompt, null, callback);
    }

    /**
     * 流式生成模式 - 实时返回响应
     * @param prompt 提示词
     * @param config 配置对象（可选，为空则使用默认配置）
     * @param callback 回调函数，用于接收流式响应
     */
    public void generateStream(String prompt, OllamaConfig config, StreamCallback callback) {
        generateStream(prompt, null, config, callback);
    }

    /**
     * 流式生成模式 - 支持图片输入（多模态）
     * @param prompt 提示词
     * @param images 图片列表（支持多模态视觉模型）
     * @param config 配置对象（可选，为空则使用默认配置）
     * @param callback 回调函数，用于接收流式响应
     */
    public void generateStream(String prompt, List<ChatMessage.ImagePart> images, OllamaConfig config, StreamCallback callback) {
        OllamaConfig effectiveConfig = mergeConfig(config);
        effectiveConfig.setStream(true);

        Map<String, Object> requestBody = buildGenerateRequest(prompt, images, effectiveConfig);

        String url = effectiveConfig.getBaseUrl() + "/api/generate";

        getExecutorService().submit(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/x-ndjson");
                connection.setConnectTimeout(effectiveConfig.getConnectTimeout());
                connection.setReadTimeout(effectiveConfig.getReadTimeout());

                connection.getOutputStream().write(JSONUtil.toJsonStr(requestBody).getBytes(StandardCharsets.UTF_8));
                connection.getOutputStream().flush();
                connection.getOutputStream().close();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    callback.onError(new LightException("HTTP error: " + responseCode));
                    return;
                }

                try (InputStream inputStream = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                    StringBuilder responseBuilder = new StringBuilder();
                    StringBuilder thinkingBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;

                        JSONObject jsonResponse = JSONUtil.parseObj(line);
                        String content = jsonResponse.getStr("response");
                        String thinkingContent = jsonResponse.getStr("thinking");
                        boolean done = jsonResponse.getBool("done");

                        if (thinkingContent != null && !thinkingContent.isEmpty()) {
                            thinkingBuilder.append(thinkingContent);
                            callback.onThinking(thinkingContent);
                        }
                        if (content != null && !content.isEmpty()) {
                            responseBuilder.append(content);
                            callback.onChunk(content);
                        }

                        if (done) {
                            // jsonSchema 模式：从累积的 response 中提取纯 JSON
                            if (effectiveConfig.getJsonSchema() != null) {
                                if (responseBuilder.length() > 0) {
                                    String extracted = tryExtractJsonFromText(responseBuilder.toString());
                                    if (extracted != null) {
                                        callback.onExtractedJson(extracted);
                                    }
                                } else if (thinkingBuilder.length() > 0) {
                                    String extracted = tryExtractJsonFromText(thinkingBuilder.toString());
                                    if (extracted != null) {
                                        callback.onExtractedJson(extracted);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Stream generate request failed", e);
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    callback.onDone();
                }catch (Exception e){
                    log.error("Stream generate request callback failed", e);
                }

            }
        });
    }

    // ===== 流式 Chat 模式 =====

    /**
     * 流式聊天模式 - 使用默认配置
     * @param messages 消息列表
     * @param callback 回调函数
     */
    public void chatStream(List<ChatMessage> messages, StreamCallback callback) {
        chatStream(messages, null, callback);
    }

    /**
     * 流式聊天模式 - 实时返回响应（多轮对话）
     * @param messages 消息列表
     * @param config 配置对象（可选，为空则使用默认配置）
     * @param callback 回调函数，用于接收流式响应
     */
    public void chatStream(List<ChatMessage> messages, OllamaConfig config, StreamCallback callback) {
        OllamaConfig effectiveConfig = mergeConfig(config);
        effectiveConfig.setStream(true);

        Map<String, Object> requestBody = buildChatRequest(messages, effectiveConfig);

        String url = effectiveConfig.getBaseUrl() + "/api/chat";

        getExecutorService().submit(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/x-ndjson");
                connection.setConnectTimeout(effectiveConfig.getConnectTimeout());
                connection.setReadTimeout(effectiveConfig.getReadTimeout());

                connection.getOutputStream().write(JSONUtil.toJsonStr(requestBody).getBytes(StandardCharsets.UTF_8));
                connection.getOutputStream().flush();
                connection.getOutputStream().close();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    callback.onError(new LightException("HTTP error: " + responseCode));
                    return;
                }

                try (InputStream inputStream = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                    StringBuilder responseBuilder = new StringBuilder();
                    StringBuilder thinkingBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;

                        JSONObject jsonResponse = JSONUtil.parseObj(line);
                        JSONObject message = jsonResponse.getJSONObject("message");
                        String content = message != null ? message.getStr("content") : "";
                        String thinkingContent = message != null ? message.getStr("thinking") : null;
                        boolean done = jsonResponse.getBool("done");

                        if (thinkingContent != null && !thinkingContent.isEmpty()) {
                            thinkingBuilder.append(thinkingContent);
                            callback.onThinking(thinkingContent);
                        }
                        if (content != null && !content.isEmpty()) {
                            responseBuilder.append(content);
                            callback.onChunk(content);
                        }

                        if (done) {
                            // jsonSchema 模式：从累积的 response 中提取纯 JSON
                            if (effectiveConfig.getJsonSchema() != null) {
                                if (responseBuilder.length() > 0) {
                                    String extracted = tryExtractJsonFromText(responseBuilder.toString());
                                    if (extracted != null) {
                                        callback.onExtractedJson(extracted);
                                    }
                                } else if (thinkingBuilder.length() > 0) {
                                    String extracted = tryExtractJsonFromText(thinkingBuilder.toString());
                                    if (extracted != null) {
                                        callback.onExtractedJson(extracted);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Stream chat request failed", e);
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    callback.onDone();
                }catch (Exception e){
                    log.error("Stream generate request callback failed", e);
                }
            }
        });
    }

    // ===== 模型列表 =====

    /**
     * 获取可用模型列表
     * @return 模型名称列表
     */
    public List<String> listModels() {
        try (HttpResponse response = HttpRequest.get(defaultConfig.getBaseUrl() + "/api/tags").execute()) {
            if (!response.isOk()) {
                throw new LightException("List models request failed: " + response.getStatus());
            }

            JSONObject jsonResponse = JSONUtil.parseObj(response.body());
            JSONArray models = jsonResponse.getJSONArray("models");

            List<String> modelNames = new ArrayList<>();
            for (int i = 0; i < models.size(); i++) {
                modelNames.add(models.getJSONObject(i).getStr("name"));
            }

            return modelNames;
        } catch (Exception e) {
            log.error("List models request failed", e);
            throw new LightException("List models request failed", e);
        }
    }

    /**
     * 设置默认配置
     * @param config 配置对象
     */
    public void setDefaultConfig(OllamaConfig config) {
        this.defaultConfig = config;
    }

    // ===== 配置合并 =====

    /**
     * 合并配置
     * 将传入的配置与默认配置合并，传入配置优先
     * @param config 传入的配置（可为空）
     * @return 合并后的配置
     */
    private OllamaConfig mergeConfig(OllamaConfig config) {
        if (config == null) {
            return defaultConfig;
        }

        OllamaConfig merged = new OllamaConfig();

        // 顶层参数
        merged.setBaseUrl(config.getBaseUrl() != null ? config.getBaseUrl() : defaultConfig.getBaseUrl());
        merged.setModel(config.getModel() != null ? config.getModel() : defaultConfig.getModel());
        merged.setSystemPrompt(config.getSystemPrompt() != null ? config.getSystemPrompt() : defaultConfig.getSystemPrompt());
        merged.setJsonSchema(config.getJsonSchema() != null ? config.getJsonSchema() : defaultConfig.getJsonSchema());
        merged.setStream(config.getStream() != null ? config.getStream() : defaultConfig.getStream());
        merged.setThink(config.getThink() != null ? config.getThink() : defaultConfig.getThink());
        merged.setKeepAlive(config.getKeepAlive() != null ? config.getKeepAlive() : defaultConfig.getKeepAlive());
        merged.setConnectTimeout(config.getConnectTimeout() != null ? config.getConnectTimeout() : defaultConfig.getConnectTimeout());
        merged.setReadTimeout(config.getReadTimeout() != null ? config.getReadTimeout() : defaultConfig.getReadTimeout());

        // Options 子参数
        merged.setNumCtx(config.getNumCtx() != null ? config.getNumCtx() : defaultConfig.getNumCtx());
        merged.setNumBatch(config.getNumBatch() != null ? config.getNumBatch() : defaultConfig.getNumBatch());
        merged.setNumKeep(config.getNumKeep() != null ? config.getNumKeep() : defaultConfig.getNumKeep());
        merged.setNumPredict(config.getNumPredict() != null ? config.getNumPredict() : defaultConfig.getNumPredict());
        merged.setNumThread(config.getNumThread() != null ? config.getNumThread() : defaultConfig.getNumThread());
        merged.setNumGpu(config.getNumGpu() != null ? config.getNumGpu() : defaultConfig.getNumGpu());
        merged.setTemperature(config.getTemperature() != null ? config.getTemperature() : defaultConfig.getTemperature());
        merged.setTopP(config.getTopP() != null ? config.getTopP() : defaultConfig.getTopP());
        merged.setTopK(config.getTopK() != null ? config.getTopK() : defaultConfig.getTopK());
        merged.setMinP(config.getMinP() != null ? config.getMinP() : defaultConfig.getMinP());
        merged.setRepeatPenalty(config.getRepeatPenalty() != null ? config.getRepeatPenalty() : defaultConfig.getRepeatPenalty());
        merged.setRepeatLastN(config.getRepeatLastN() != null ? config.getRepeatLastN() : defaultConfig.getRepeatLastN());
        merged.setPresencePenalty(config.getPresencePenalty() != null ? config.getPresencePenalty() : defaultConfig.getPresencePenalty());
        merged.setFrequencyPenalty(config.getFrequencyPenalty() != null ? config.getFrequencyPenalty() : defaultConfig.getFrequencyPenalty());
        merged.setSeed(config.getSeed() != null ? config.getSeed() : defaultConfig.getSeed());
        merged.setOptions(config.getOptions() != null ? config.getOptions() : defaultConfig.getOptions());

        return merged;
    }

    // ===== 请求构建 =====

    /**
     * 构建生成请求体
     * @param prompt 提示词
     * @param config 配置对象
     * @return 请求体 Map
     */
    private Map<String, Object> buildGenerateRequest(String prompt, OllamaConfig config) {
        return buildGenerateRequest(prompt, null, config);
    }

    /**
     * 构建生成请求体（支持图片）
     * @param prompt 提示词
     * @param images 图片列表
     * @param config 配置对象
     * @return 请求体 Map
     */
    private Map<String, Object> buildGenerateRequest(String prompt, List<ChatMessage.ImagePart> images, OllamaConfig config) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("prompt", prompt);
        requestBody.put("stream", config.getStream());

        // 系统提示词（含 jsonSchema 强化指令）
        String effectiveSystem = buildEffectiveSystemPrompt(config);
        if (effectiveSystem != null && !effectiveSystem.isEmpty()) {
            requestBody.put("system", effectiveSystem);
        }
        if (config.getKeepAlive() != null) {
            requestBody.put("keep_alive", config.getKeepAlive());
        }
        if (config.getThink() != null) {
            requestBody.put("think", config.getThink());
        }

        // 图片（多模态）
        if (images != null && !images.isEmpty()) {
            List<String> imageData = new ArrayList<>();
            for (ChatMessage.ImagePart image : images) {
                imageData.add(image.getImage());
            }
            requestBody.put("images", imageData);
        }

        addOptionsToRequest(requestBody, config);

        return requestBody;
    }

    /**
     * 构建聊天请求体
     * @param messages 消息列表
     * @param config 配置对象
     * @return 请求体 Map
     */
    private Map<String, Object> buildChatRequest(List<ChatMessage> messages, OllamaConfig config) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("stream", config.getStream());

        if (config.getKeepAlive() != null) {
            requestBody.put("keep_alive", config.getKeepAlive());
        }
        if (config.getThink() != null) {
            requestBody.put("think", config.getThink());
        }

        // 消息列表
        List<Map<String, Object>> formattedMessages = new ArrayList<>();

        // 系统消息（含 jsonSchema 强化指令）
        String effectiveSystem = buildEffectiveSystemPrompt(config);
        if (effectiveSystem != null && !effectiveSystem.isEmpty()) {
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", effectiveSystem);
            formattedMessages.add(systemMsg);
        }

        // 用户/助手消息
        for (ChatMessage message : messages) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", message.getRole());
            msg.put("content", message.getContent() != null ? message.getContent() : "");

            // 图片以独立 images 数组发送（Ollama API 格式）
            if (message.getImages() != null && !message.getImages().isEmpty()) {
                List<String> imageStrings = new ArrayList<>();
                for (ChatMessage.ImagePart image : message.getImages()) {
                    imageStrings.add(image.getImage());
                }
                msg.put("images", imageStrings);
            }

            formattedMessages.add(msg);
        }

        requestBody.put("messages", formattedMessages);

        addOptionsToRequest(requestBody, config);

        return requestBody;
    }

    /**
     * 向请求体添加选项参数
     * 参数名遵循 Ollama API 规范（snake_case 格式）
     * @param requestBody 请求体
     * @param config 配置对象
     */
    private void addOptionsToRequest(Map<String, Object> requestBody, OllamaConfig config) {
        Map<String, Object> options = new HashMap<>();
        if (config.getNumCtx() != null) options.put("num_ctx", config.getNumCtx());
        if (config.getNumBatch() != null) options.put("num_batch", config.getNumBatch());
        if (config.getNumKeep() != null) options.put("num_keep", config.getNumKeep());
        if (config.getNumPredict() != null) options.put("num_predict", config.getNumPredict());
        if (config.getNumThread() != null) options.put("num_thread", config.getNumThread());
        if (config.getNumGpu() != null) options.put("num_gpu", config.getNumGpu());
        if (config.getTemperature() != null) options.put("temperature", config.getTemperature());
        if (config.getTopP() != null) options.put("top_p", config.getTopP());
        if (config.getTopK() != null) options.put("top_k", config.getTopK());
        if (config.getMinP() != null) options.put("min_p", config.getMinP());
        if (config.getRepeatPenalty() != null) options.put("repeat_penalty", config.getRepeatPenalty());
        if (config.getRepeatLastN() != null) options.put("repeat_last_n", config.getRepeatLastN());
        if (config.getPresencePenalty() != null) options.put("presence_penalty", config.getPresencePenalty());
        if (config.getFrequencyPenalty() != null) options.put("frequency_penalty", config.getFrequencyPenalty());
        if (config.getSeed() != null) options.put("seed", config.getSeed());
        if (config.getOptions() != null) options.putAll(config.getOptions());

        if (!options.isEmpty()) {
            requestBody.put("options", options);
        }
    }

    // ===== 系统提示词与 JSON 提取 =====

    /**
     * 构建有效的系统提示词
     * 当设置了 jsonSchema 时，智能融合用户提示词和 JSON 格式指令
     * 不使用 API 的 format 参数，通过提示词引导模型输出干净的 JSON，不影响思考能力
     * @param config 配置对象
     * @return 有效的系统提示词，可能为 null
     */
    private String buildEffectiveSystemPrompt(OllamaConfig config) {
        String systemPrompt = config.getSystemPrompt();

        if (config.getJsonSchema() != null) {
            String schemaStr = config.getJsonSchema().toJSONString(1);
            
            // 如果用户没有提供系统提示词，直接使用简洁的格式指令
            if (systemPrompt == null || systemPrompt.isEmpty()) {
                return "请严格按照以下 JSON Schema 格式输出：\n" + schemaStr;
            }
            
            // 智能融合策略：根据用户提示词的特征选择合适的融合方式
            return smartMergePrompts(systemPrompt, schemaStr);
        }

        return systemPrompt;
    }
    
    /**
     * 智能融合用户提示词和 JSON Schema 指令
     * @param userPrompt 用户系统提示词
     * @param schemaStr JSON Schema 字符串
     * @return 融合后的提示词
     */
    private String smartMergePrompts(String userPrompt, String schemaStr) {
        // 分析用户提示词的特征
        PromptCharacteristics characteristics = analyzePrompt(userPrompt);
        
        StringBuilder result = new StringBuilder();
        
        // 策略1：如果用户提示词已有明确的输出格式说明，采用补充式融合
        if (characteristics.hasFormatInstruction) {
            result.append(userPrompt);
            result.append("\n\n");
            result.append("JSON Schema 定义：");
            result.append(schemaStr);
        }
        // 策略2：如果用户提示词以任务描述为主，采用结构化融合
        else if (characteristics.isStructured) {
            result.append(userPrompt);
            result.append("\n\n");
            result.append("# 输出格式\n");
            result.append("请严格遵循以下 JSON Schema 输出结果：\n");
            result.append(schemaStr);
        }
        // 策略3：默认采用温和的追加式融合
        else {
            result.append(userPrompt);
            result.append("\n\n");
            result.append("【输出要求】请以 JSON 格式输出，需符合以下 Schema：\n");
            result.append(schemaStr);
        }
        
        return result.toString();
    }
    
    /**
     * 提示词特征分析结果
     */
    private static class PromptCharacteristics {
        boolean hasFormatInstruction;  // 是否包含格式指令
        boolean isStructured;          // 是否是结构化提示词（包含标题等）
        boolean endsWithConstraint;    // 是否以约束条件结尾
        
        PromptCharacteristics() {
            this.hasFormatInstruction = false;
            this.isStructured = false;
            this.endsWithConstraint = false;
        }
    }
    
    /**
     * 分析用户提示词的特征
     * @param prompt 用户提示词
     * @return 提示词特征
     */
    private PromptCharacteristics analyzePrompt(String prompt) {
        PromptCharacteristics chars = new PromptCharacteristics();
        
        if (prompt == null || prompt.isEmpty()) {
            return chars;
        }
        
        String trimmed = prompt.trim();
        String lowerPrompt = prompt.toLowerCase();
        
        // 检测是否包含格式相关指令
        chars.hasFormatInstruction = containsFormatInstruction(prompt, lowerPrompt);
        
        // 检测是否是结构化提示词（包含 Markdown 标题、章节等）
        chars.isStructured = trimmed.contains("#") || 
                            trimmed.contains("##") ||
                            prompt.contains("角色") ||
                            prompt.contains("任务") ||
                            prompt.contains("要求") ||
                            prompt.contains("步骤") ||
                            prompt.contains("注意");
        
        // 检测是否以约束条件结尾
        String lastLine = getLastSignificantLine(trimmed);
        chars.endsWithConstraint = lastLine.contains("必须") ||
                                  lastLine.contains("禁止") ||
                                  lastLine.contains("不要") ||
                                  lastLine.contains("仅") ||
                                  lastLine.contains("只");
        
        return chars;
    }
    
    /**
     * 检测是否包含格式相关指令
     */
    private boolean containsFormatInstruction(String prompt, String lowerPrompt) {
        // 检测 JSON 相关词汇
        if (lowerPrompt.contains("json") || 
            prompt.contains("JSON") ||
            prompt.contains("格式") ||
            prompt.contains("结构化")) {
            return true;
        }
        
        // 检测输出约束表达
        if (prompt.contains("输出格式") ||
            prompt.contains("返回格式") ||
            prompt.contains("以json") ||
            prompt.contains("用json") ||
            prompt.contains("json格式")) {
            return true;
        }
        
        // 检测示例或模板
        if (prompt.contains("{\"") || 
            prompt.contains("schema") ||
            prompt.contains("Schema") ||
            prompt.contains("示例：") ||
            prompt.contains("例如：")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取最后一行有意义的内容（跳过空行）
     */
    private String getLastSignificantLine(String text) {
        String[] lines = text.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                return line;
            }
        }
        return "";
    }

    /**
     * 从文本中提取 JSON 对象
     * 模型在提示词引导下可能仍然输出 markdown 代码块包裹的 JSON，此方法负责提取纯 JSON
     * 也适用于从 thinking 内容中提取 JSON
     * @param text 可能包含 JSON 的文本
     * @return 提取到的 JSON 字符串，无法提取时返回 null
     */
    private String tryExtractJsonFromText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String trimmed = text.trim();

        // 尝试1：整个文本就是纯 JSON
        if (trimmed.startsWith("{")) {
            try {
                JSONUtil.parseObj(trimmed);
                return trimmed;
            } catch (Exception ignored) {
            }
        }

        // 尝试2：从 ```json 代码块中提取
        int jsonBlockStart = trimmed.indexOf("```json");
        if (jsonBlockStart >= 0) {
            int contentStart = trimmed.indexOf('\n', jsonBlockStart) + 1;
            int contentEnd = trimmed.indexOf("```", contentStart);
            if (contentEnd > contentStart) {
                String jsonStr = trimmed.substring(contentStart, contentEnd).trim();
                try {
                    JSONUtil.parseObj(jsonStr);
                    return jsonStr;
                } catch (Exception ignored) {
                }
            }
        }

        // 尝试3：从 ``` 代码块中提取
        int codeBlockStart = trimmed.indexOf("```");
        if (codeBlockStart >= 0) {
            int contentStart = trimmed.indexOf('\n', codeBlockStart) + 1;
            int contentEnd = trimmed.indexOf("```", contentStart);
            if (contentEnd > contentStart) {
                String jsonStr = trimmed.substring(contentStart, contentEnd).trim();
                try {
                    JSONUtil.parseObj(jsonStr);
                    return jsonStr;
                } catch (Exception ignored) {
                }
            }
        }

        // 尝试4：查找第一个 { 和最后一个 } 之间的内容
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            String jsonStr = trimmed.substring(firstBrace, lastBrace + 1);
            try {
                JSONUtil.parseObj(jsonStr);
                return jsonStr;
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // ===== 流式回调接口 =====

    /**
     * 流式回调接口
     */
    public interface StreamCallback {
        /**
         * 接收响应内容片段
         * @param content 内容片段
         */
        void onChunk(String content);

        /**
         * 接收思考内容（当启用思考模式时）
         * @param thinking 思考内容片段
         */
        default void onThinking(String thinking) {
            // 默认忽略思考内容
        }

        /**
         * 接收提取的纯 JSON（当设置了 jsonSchema 且流式完成时）
         * 流式过程中 onChunk 接收的是原始文本片段，onExtractedJson 在完成时提供提取后的纯 JSON
         * @param json 提取到的纯 JSON 字符串
         */
        default void onExtractedJson(String json) {
            // 默认忽略
        }

        /**
         * 流式生成完成回调
         * 当 Ollama 返回 done=true 时调用，表示生成已完成
         * 注意：onChunk/onThinking 的 done 参数在最终行内容为空时不会传递 true，
         * 因此应通过此方法判断流式生成是否完成
         */
        default void onDone() {
            // 默认忽略
        }

        /**
         * 接收错误
         * @param e 异常
         */
        default void onError(Exception e){

        }
    }

    /**
     * 关闭客户端，释放资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}