package com.lightframework.util.json;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * JSON Schema 生成器
 * 用于将 Java 类自动转换为 JSON Schema 格式
 * 通过 @Schema 注解获取描述信息
 */
public class JsonSchemaUtil {

    /**
     * 将 Java 类转换为 JSON Schema 对象
     * @param clazz Java 类
     * @return JSON Schema JSONObject 对象
     */
    public static JSONObject generateSchemaObject(Class<?> clazz) {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("title", clazz.getSimpleName());

        String classDesc = getClassDescription(clazz);
        if (classDesc != null && !classDesc.isEmpty()) {
            schema.put("description", classDesc);
        }

        JSONObject properties = new JSONObject();
        JSONArray required = new JSONArray();

        List<Field> fields = getAllFields(clazz);

        for (Field field : fields) {
            String fieldName = field.getName();
            JSONObject propertySchema = generatePropertySchema(field);
            properties.put(fieldName, propertySchema);

            if (isRequired(field)) {
                required.add(fieldName);
            }
        }

        schema.put("properties", properties);

        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    /**
     * 将 Java 类转换为 JSON Schema 字符串
     * @param clazz Java 类
     * @return JSON Schema 字符串
     */
    public static String generate(Class<?> clazz) {
        return generateSchemaObject(clazz).toString();
    }

    /**
     * 获取类的所有字段（包括父类）
     * @param clazz Java 类
     * @return 字段列表
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * 生成字段的 JSON Schema
     * @param field 字段
     * @return 属性 Schema 对象
     */
    private static JSONObject generatePropertySchema(Field field) {
        JSONObject property = new JSONObject();

        String description = getFieldDescription(field);
        if (description != null && !description.isEmpty()) {
            property.put("description", description);
        }

        Class<?> fieldType = field.getType();
        String jsonType = getJsonType(fieldType);

        property.put("type", jsonType);

        if ("array".equals(jsonType)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                JSONObject items = new JSONObject();

                if (elementType instanceof Class) {
                    Class<?> elementClass = (Class<?>) elementType;
                    items.put("type", getJsonType(elementClass));

                    if (isComplexType(elementClass)) {
                        items.putAll(generateSchemaObject(elementClass));
                    }
                }

                property.put("items", items);
            }
        } else if ("object".equals(jsonType) && isComplexType(fieldType)) {
            property.putAll(generateSchemaObject(fieldType));
        }

        return property;
    }

    /**
     * 将 Java 类型转换为 JSON Schema 类型
     * @param clazz Java 类
     * @return JSON Schema 类型字符串
     */
    private static String getJsonType(Class<?> clazz) {
        if (clazz == String.class) {
            return "string";
        }
        if (clazz == Integer.class || clazz == int.class ||
            clazz == Long.class || clazz == long.class ||
            clazz == Short.class || clazz == short.class ||
            clazz == Byte.class || clazz == byte.class) {
            return "integer";
        }
        if (clazz == Float.class || clazz == float.class ||
            clazz == Double.class || clazz == double.class ||
            clazz == java.math.BigDecimal.class ||
            clazz == java.math.BigInteger.class) {
            return "number";
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return "boolean";
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return "array";
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return "object";
        }
        if (clazz.isEnum()) {
            return "string";
        }
        if (clazz.isPrimitive()) {
            return "string";
        }
        return "object";
    }

    /**
     * 判断是否为复杂类型（需要递归处理）
     * @param clazz Java 类
     * @return 是否为复杂类型
     */
    private static boolean isComplexType(Class<?> clazz) {
        if (clazz.isPrimitive()) return false;
        if (clazz == String.class) return false;
        if (Number.class.isAssignableFrom(clazz)) return false;
        if (clazz == Boolean.class) return false;
        if (clazz.isEnum()) return false;
        if (Collection.class.isAssignableFrom(clazz)) return false;
        if (Map.class.isAssignableFrom(clazz)) return false;
        if (clazz.isArray()) return false;
        return true;
    }

    /**
     * 判断字段是否为必填
     * 兼容 Swagger v3 的新旧两种写法：
     * - 新版：@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
     * - 旧版：@Schema(required = true)
     * 无 @Schema 注解时，原始类型默认为必填
     * @param field 字段
     * @return 是否必填
     */
    private static boolean isRequired(Field field) {
        if (field.isAnnotationPresent(Schema.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            // 优先检查新版 requiredMode（Swagger v3 2.x+）
            try {
                Object requiredMode = schema.requiredMode();
                if (requiredMode != null) {
                    // 通过枚举名称判断，避免直接依赖枚举类可能带来的版本兼容问题
                    String modeName = requiredMode.toString();
                    if ("REQUIRED".equals(modeName)) {
                        return true;
                    }
                    if ("NOT_REQUIRED".equals(modeName)) {
                        return false;
                    }
                    // AUTO 或其他值：继续走旧版 required 判断
                }
            } catch (Exception ignored) {
                // requiredMode 方法不存在（极旧版本），忽略
            }
            // 旧版 required 属性
            return schema.required();
        }
        return field.getType().isPrimitive();
    }

    /**
     * 从 @Schema 注解获取类的描述
     * @param clazz Java 类
     * @return 描述内容，无注解时返回 null
     */
    private static String getClassDescription(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Schema.class)) {
            Schema schema = clazz.getAnnotation(Schema.class);
            if (schema.description() != null && !schema.description().isEmpty()) {
                return schema.description();
            }
        }
        return null;
    }

    /**
     * 从 @Schema 注解获取字段的描述
     * @param field 字段
     * @return 描述内容，无注解时返回 null
     */
    private static String getFieldDescription(Field field) {
        if (field.isAnnotationPresent(Schema.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            if (schema.description() != null && !schema.description().isEmpty()) {
                return schema.description();
            }
        }
        return null;
    }
}