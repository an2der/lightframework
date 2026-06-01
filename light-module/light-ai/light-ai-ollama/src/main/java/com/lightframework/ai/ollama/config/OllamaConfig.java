package com.lightframework.ai.ollama.config;

import cn.hutool.json.JSONObject;
import com.lightframework.util.json.JsonSchemaUtil;
import lombok.Data;

import java.util.Map;

/**
 * Ollama 客户端配置类
 * 用于配置 Ollama API 请求的各种参数
 * 参数命名遵循 Ollama API 规范
 */
@Data
public class OllamaConfig {

    /**
     * Ollama 服务地址，默认为 http://localhost:11434
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * 模型名称，默认为 llama3
     */
    private String model = "llama3";

    // ===== 顶层请求参数 =====

    /**
     * 系统提示词
     * generate 模式下作为顶层 system 参数发送
     * chat 模式下作为 system 角色消息发送
     */
    private String systemPrompt;

    /**
     * JSON Schema，用于引导模型输出符合格式的 JSON
     * 不通过 API 的 format 参数发送（format 会抑制模型思考能力），
     * 而是通过系统强化提示词引导模型输出干净、符合 schema 的 JSON
     */
    private JSONObject jsonSchema;


    /**
     * 是否启用流式响应，默认为 false
     */
    private Boolean stream = false;

    /**
     * 思考模式控制（对应 API 的 think 参数）
     * 支持设为 Boolean（true/false）或字符串（"high"/"medium"/"low"）
     */
    private Boolean think = false;

    /**
     * 模型驻留内存时间控制（对应 API 的 keep_alive 参数）
     * 例如 "5m" 表示5分钟，0 表示立即卸载
     */
    private Object keepAlive;

    // ===== 超时控制参数 =====

    /**
     * 连接超时时间（毫秒）
     * 默认 30000 毫秒（30秒）
     * 设置为 0 表示无限等待
     */
    private Integer connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     * 默认 0 毫秒（无限等待）
     * 设置大于0的值表示：请求发送后在指定时间内没有返回数据则中断请求
     * 流式响应时建议设置为 0（无限等待），或设置一个较大的值如 300000（5分钟）
     */
    private Integer readTimeout = 0;

    // ===== Options 子参数 =====

    /**
     * 上下文窗口大小
     */
    private Integer numCtx;

    /**
     * 批处理大小
     */
    private Integer numBatch;

    /**
     * 保留的初始 token 数
     */
    private Integer numKeep;

    /**
     * 最大生成 token 数（默认 -1，即无限生成，直到模型自然结束或达到上下文窗口限制）
     * 对应 API 的 num_predict 选项
     * 若不发送此参数，Ollama 模型默认 num_predict 通常为 128，会导致生成长回复时中途截断
     */
    private Integer numPredict;

    /**
     * 线程数（对应 API 的 num_thread 选项，单数形式）
     */
    private Integer numThread;

    /**
     * GPU 层数（对应 API 的 num_gpu 选项）
     */
    private Integer numGpu;

    /**
     * 温度参数，控制生成的随机性（默认 0.8）
     */
    private Float temperature = 0.8f;

    /**
     * Top-K 采样参数（默认 40）
     */
    private Integer topK = 40;

    /**
     * Top-P（核采样）参数（默认 0.9）
     */
    private Float topP = 0.9f;

    /**
     * Min-P 参数，最小概率过滤（默认 0.0）
     */
    private Float minP;

    /**
     * 重复惩罚系数（默认 1.1）
     */
    private Float repeatPenalty;

    /**
     * 重复惩罚回看窗口大小（默认 64，0=禁用，-1=num_ctx）
     */
    private Integer repeatLastN;

    /**
     * 存在惩罚
     */
    private Float presencePenalty;

    /**
     * 频率惩罚
     */
    private Float frequencyPenalty;

    /**
     * 随机种子（默认 0）
     */
    private Integer seed;

    /**
     * 额外选项（自定义 key-value，会合并到 options 对象中）
     */
    private Map<String, Object> options;

    // ===== 构造函数 =====

    public OllamaConfig() {}

    public OllamaConfig(String model) {
        this.model = model;
    }

    public OllamaConfig(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
    }

    // ===== 链式配置方法 =====

    public OllamaConfig systemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * 设置输出格式的 JSON Schema（JSONObject 形式）
     * @param jsonSchema JSON Schema 对象
     * @return 当前配置对象
     */
    public OllamaConfig jsonSchema(JSONObject jsonSchema) {
        this.jsonSchema = jsonSchema;
        return this;
    }

    /**
     * 设置输出格式的 JSON Schema（字符串形式）
     * @param jsonSchema JSON Schema 字符串
     * @return 当前配置对象
     */
    public OllamaConfig jsonSchema(String jsonSchema) {
        this.jsonSchema = cn.hutool.json.JSONUtil.parseObj(jsonSchema);
        return this;
    }

    /**
     * 设置输出格式的 Java 类，自动将类转换为 JSON Schema
     * @param clazz Java 类
     * @return 当前配置对象
     */
    public OllamaConfig jsonSchema(Class<?> clazz) {
        this.jsonSchema = JsonSchemaUtil.generateSchemaObject(clazz);
        return this;
    }

    public OllamaConfig stream(Boolean stream) {
        this.stream = stream;
        return this;
    }

    public OllamaConfig think(Boolean think) {
        this.think = think;
        return this;
    }

    public OllamaConfig keepAlive(String keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public OllamaConfig keepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    /**
     * 设置连接超时时间
     * @param connectTimeout 超时时间（毫秒）
     * @return 当前配置对象
     */
    public OllamaConfig connectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 设置读取超时时间
     * @param readTimeout 超时时间（毫秒），设置为0表示无限等待
     * @return 当前配置对象
     */
    public OllamaConfig readTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public OllamaConfig numCtx(Integer numCtx) {
        this.numCtx = numCtx;
        return this;
    }

    public OllamaConfig numBatch(Integer numBatch) {
        this.numBatch = numBatch;
        return this;
    }

    public OllamaConfig numKeep(Integer numKeep) {
        this.numKeep = numKeep;
        return this;
    }

    public OllamaConfig numPredict(Integer numPredict) {
        this.numPredict = numPredict;
        return this;
    }

    public OllamaConfig numThread(Integer numThread) {
        this.numThread = numThread;
        return this;
    }

    public OllamaConfig numGpu(Integer numGpu) {
        this.numGpu = numGpu;
        return this;
    }

    public OllamaConfig temperature(Float temperature) {
        this.temperature = temperature;
        return this;
    }

    public OllamaConfig topP(Float topP) {
        this.topP = topP;
        return this;
    }

    public OllamaConfig topK(Integer topK) {
        this.topK = topK;
        return this;
    }

    public OllamaConfig minP(Float minP) {
        this.minP = minP;
        return this;
    }

    public OllamaConfig repeatPenalty(Float repeatPenalty) {
        this.repeatPenalty = repeatPenalty;
        return this;
    }

    public OllamaConfig repeatLastN(Integer repeatLastN) {
        this.repeatLastN = repeatLastN;
        return this;
    }

    public OllamaConfig presencePenalty(Float presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }

    public OllamaConfig frequencyPenalty(Float frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    public OllamaConfig seed(Integer seed) {
        this.seed = seed;
        return this;
    }

    public OllamaConfig options(Map<String, Object> options) {
        this.options = options;
        return this;
    }
}