package org.lpz.aiagent.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AI响应内容清理工具类
 */
public class ResponseCleanupUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 需要过滤的无用字段
    private static final Set<String> SKIP_FIELDS = new HashSet<>(Arrays.asList(
        "position", "rank", "index", "id", "uuid", "guid", "hash",
        "thumbnail", "favicon", "breadcrumb", "navigation", "menu",
        "sidebar", "footer", "header", "ads", "advertisement", "tracking",
        "analytics", "metadata", "seo", "schema", "json_ld", "microdata",
        "og_", "twitter_", "fb_", "social_", "share_", "like_", "comment_",
        "related_", "similar_", "recommended_", "popular_", "trending_",
        "cached", "timestamp", "crawled", "indexed", "scraped",
        "debug", "trace", "log", "error", "warning", "info_log",
        "snippet_highlighted_words", "is_baidu_guaranteed"
    ));
    
    // 保留的有用字段
    private static final Set<String> USEFUL_FIELDS = new HashSet<>(Arrays.asList(
        "title", "name", "description", "content", "text", "info", "summary",
        "link", "url", "author", "date", "time", "snippet", "displayed_link",
        "source", "category", "tags", "keywords", "price", "rating"
    ));
    
    /**
     * 清理AI响应文本
     */
    public static String cleanResponseText(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        
        // 去除多余的空白和换行
        String cleaned = text
            .replaceAll("\\r\\n", "\n")  // 统一换行符
            .replaceAll("\\r", "\n")     // 统一换行符
            .replaceAll("^\\s+", "")     // 去除开头空白
            .replaceAll("\\s+$", "")     // 去除结尾空白
            .replaceAll("\\n{3,}", "\n\n") // 合并多余换行
            .replaceAll("[ \\t]+\\n", "\n") // 去除行尾空格
            .replaceAll("\\n[ \\t]+", "\n"); // 去除行首空格
        
        return cleaned;
    }
    
    /**
     * 清理JSON数据
     */
    public static String cleanJsonData(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return jsonString;
        }
        
        try {
            // 尝试解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            JsonNode cleanedNode = cleanJsonNode(jsonNode);
            
            if (cleanedNode != null) {
                return objectMapper.writeValueAsString(cleanedNode);
            }
        } catch (Exception e) {
            // 如果不是JSON格式，直接返回清理后的文本
            return cleanResponseText(jsonString);
        }
        
        return jsonString;
    }
    
    /**
     * 递归清理JSON节点
     */
    private static JsonNode cleanJsonNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        if (node.isArray()) {
            ArrayNode cleanedArray = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                JsonNode cleanedItem = cleanJsonNode(item);
                if (cleanedItem != null) {
                    cleanedArray.add(cleanedItem);
                }
            }
            return cleanedArray.size() > 0 ? cleanedArray : null;
        }
        
        if (node.isObject()) {
            ObjectNode cleanedObject = objectMapper.createObjectNode();
            
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                // 跳过无用字段
                if (shouldSkipField(key)) {
                    return;
                }
                
                // 只保留有用字段
                if (USEFUL_FIELDS.contains(key.toLowerCase())) {
                    if (value.isTextual()) {
                        String textValue = value.asText().trim();
                        if (!textValue.isEmpty()) {
                            cleanedObject.put(key, textValue);
                        }
                    } else {
                        JsonNode cleanedValue = cleanJsonNode(value);
                        if (cleanedValue != null) {
                            cleanedObject.set(key, cleanedValue);
                        }
                    }
                }
            });
            
            return cleanedObject.size() > 0 ? cleanedObject : null;
        }
        
        return node;
    }
    
    /**
     * 判断是否应该跳过某个字段
     */
    private static boolean shouldSkipField(String key) {
        if (key == null) return true;
        
        String lowerKey = key.toLowerCase();
        
        // 检查是否以下划线开头
        if (key.startsWith("_")) return true;
        
        // 检查是否包含无用关键词
        for (String skipField : SKIP_FIELDS) {
            if (lowerKey.contains(skipField)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 清理工具执行结果
     */
    public static String cleanToolResult(String toolResult) {
        if (!StringUtils.hasText(toolResult)) {
            return toolResult;
        }
        
        // 首先清理文本
        String cleaned = cleanResponseText(toolResult);
        
        // 检查是否包含JSON数据
        if (cleaned.contains("{") || cleaned.contains("[")) {
            // 尝试提取和清理JSON部分
            Pattern jsonPattern = Pattern.compile("结果:\\s*\"(.+)\"");
            java.util.regex.Matcher matcher = jsonPattern.matcher(cleaned);
            
            if (matcher.find()) {
                String jsonStr = matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
                
                String cleanedJson = cleanJsonData(jsonStr);
                return cleaned.replace(matcher.group(0), "结果: " + cleanedJson);
            }
            
            // 尝试直接清理JSON对象或数组
            Pattern directJsonPattern = Pattern.compile("(\\{[\\s\\S]*\\}|\\[[\\s\\S]*\\])");
            matcher = directJsonPattern.matcher(cleaned);
            
            if (matcher.find()) {
                String cleanedJson = cleanJsonData(matcher.group(1));
                return cleaned.replace(matcher.group(1), cleanedJson);
            }
        }
        
        return cleaned;
    }
    
    /**
     * 格式化搜索结果为易读格式
     */
    public static String formatSearchResults(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                StringBuilder formatted = new StringBuilder();
                formatted.append("找到 ").append(jsonNode.size()).append(" 个搜索结果:\n\n");
                
                int index = 1;
                for (JsonNode item : jsonNode) {
                    JsonNode cleanedItem = cleanJsonNode(item);
                    if (cleanedItem != null) {
                        formatted.append(index).append(". ");
                        
                        // 提取标题
                        String title = getTextValue(cleanedItem, "title", "name");
                        if (title != null) {
                            formatted.append(title).append("\n");
                        }
                        
                        // 提取链接
                        String link = getTextValue(cleanedItem, "link", "url");
                        if (link != null) {
                            formatted.append("链接: ").append(link).append("\n");
                        }
                        
                        // 提取描述
                        String description = getTextValue(cleanedItem, "snippet", "description", "content");
                        if (description != null) {
                            // 限制描述长度
                            if (description.length() > 150) {
                                description = description.substring(0, 150) + "...";
                            }
                            formatted.append("描述: ").append(description).append("\n");
                        }
                        
                        // 提取来源
                        String source = getTextValue(cleanedItem, "displayed_link", "source");
                        if (source != null) {
                            formatted.append("来源: ").append(source).append("\n");
                        }
                        
                        formatted.append("\n");
                        index++;
                    }
                }
                
                return formatted.toString().trim();
            }
        } catch (Exception e) {
            // 解析失败，返回清理后的原始数据
            return cleanJsonData(jsonString);
        }
        
        return jsonString;
    }
    
    /**
     * 从JSON节点中获取文本值
     */
    private static String getTextValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode field = node.get(fieldName);
            if (field != null && field.isTextual()) {
                String value = field.asText().trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }
}