package com.lightframework.ai.ollama.bean;

import com.lightframework.common.LightException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

/**
 * 聊天消息类
 * 用于构建对话消息，支持文本和图片内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 消息角色：system、user、assistant
     */
    private String role;
    
    /**
     * 消息内容（文本）
     */
    private String content;
    
    /**
     * 图片内容列表（用于多模态视觉模型）
     */
    private List<ImagePart> images;

    /**
     * 创建系统消息
     * @param content 系统提示词内容
     * @return ChatMessage 实例
     */
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, null);
    }

    /**
     * 创建用户消息（纯文本）
     * @param content 用户输入内容
     * @return ChatMessage 实例
     */
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, null);
    }

    /**
     * 创建用户消息（文本+图片）
     * @param content 用户输入内容
     * @param images 图片列表
     * @return ChatMessage 实例
     */
    public static ChatMessage user(String content, List<ImagePart> images) {
        return new ChatMessage("user", content, images);
    }

    /**
     * 创建助手消息
     * @param content 助手回复内容
     * @return ChatMessage 实例
     */
    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, null);
    }

    /**
     * 图片内容类
     * 用于封装图片数据，支持 Base64 编码格式
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImagePart {
        /**
         * 类型，默认为 "image"
         */
        private String type = "image";
        
        /**
         * Base64 编码的图片数据
         */
        private String image;
        
        /**
         * 从 Base64 字符串创建图片部分
         * @param base64Image Base64 编码的图片
         * @return ImagePart 实例
         */
        public static ImagePart fromBase64(String base64Image) {
            return new ImagePart("image", base64Image);
        }

        /**
         * 从 InputStream 创建图片部分（自动转换为 Base64）
         * @param inputStream 图片输入流
         * @return ImagePart 实例
         */
        public static ImagePart fromInputStream(InputStream inputStream) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                buffer.flush();
                byte[] imageBytes = buffer.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                return new ImagePart("image", base64Image);
            } catch (IOException e) {
                throw new LightException(e);
            }
        }
    }
}
