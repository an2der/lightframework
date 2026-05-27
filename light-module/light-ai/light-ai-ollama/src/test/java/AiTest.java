import com.lightframework.ai.ollama.bean.ChatMessage;
import com.lightframework.ai.ollama.client.OllamaClient;
import com.lightframework.ai.ollama.config.OllamaConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class AiTest {
    public static void main(String[] args) {
        OllamaConfig config = new OllamaConfig("http://192.168.142.28:11434","qwen3.6:35b");
        config.setTemperature(0.2f);
//        //简单生成式对话，一次性返回
//        config.setThink(false);//关闭思考
//        config.jsonSchema(LlmResponse.class);
        OllamaClient client = new OllamaClient(config);
//        System.out.println(client.generate("请用java写一个冒泡排序"));
//
//        //流式生成式对话，返回实时结果
//        config.setThink(true);//开启思考
//        client.generateStream("请用java写一个冒泡排序", new OllamaClient.StreamCallback() {
//            @Override
//            public void onChunk(String content) {
//                System.out.print(content);
//            }
//
//            @Override
//            public void onThinking(String thinking) {
//                System.out.print(thinking);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                System.out.println("错误信息：" + e.getMessage());
//            }
//        });

        //多模态视觉对话，一次性返回结果
        config.think(false);
        config.jsonSchema(AnalysisResponse.class);
        config.systemPrompt(
                "# 角色\n" +
                        "你是一名资深服务器性能分析与运维专家，擅长从监控时序图中精准识别内存异常趋势、判断内存泄漏特征，并给出容量预测与调优建议。\n" +
                        "\n" +
                        "# 任务\n" +
                        "用户将提供一张【主机内存使用率采样趋势图】（包含内存曲线与危险告警阈值线）。请基于图像内容完成分析，并严格仅输出指定格式的JSON结果。\n" +
                        "\n" +
                        "# 分析逻辑\n" +
                        "1. 趋势识别：观察曲线整体走向、波动特征及与告警阈值线的相对位置。\n" +
                        "2. 异常分析：根据曲线分析服务器设备是否存在内存溢出风险的可能，不管当前的曲线在不在安全范围，只要有可能则判定为存在，目的是为了提前发现内存溢出的风险，存在判定为 `abnormal: true`；否则判定为 `abnormal: false`。\n" +
                        "3. 天数预估：若 `abnormal: true`，请根据当前曲线斜率进行线性外推，估算剩余可用内存降至告警阈值大约需要多少天（取整数）。若图像时间轴不清晰，请基于常见监控采样周期（如每小时/每6小时/每天）进行合理推算。\n" +
                        "4. 建议生成：根据内存曲线的情况，给出相应的具体可操作的建议。\n" +
                        "\n" +
                        "# 强制规则\n" +
                        "- 当 abnormal 为 false 时，estimatedDays 必须为 0，recommend 必须为 \"\"（空字符串）。\n" +
                        "- 当 abnormal 为 true 时，estimatedDays 为 ≥1 的整数，recommend 为详细建议字符串。\n" +
                        "- 输出必须为合法JSON，确保可被标准解析器完整读取。\n");
        try {
            FileInputStream inputStream = new FileInputStream("C:\\Users\\an2de\\Desktop\\image.png");
            System.out.println(client.generate("请分析", Arrays.asList(ChatMessage.ImagePart.fromInputStream(inputStream)), config));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
