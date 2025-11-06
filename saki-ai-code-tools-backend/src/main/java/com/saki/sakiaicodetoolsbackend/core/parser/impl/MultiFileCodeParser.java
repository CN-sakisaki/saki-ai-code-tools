package com.saki.sakiaicodetoolsbackend.core.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saki.sakiaicodetoolsbackend.ai.model.MultiFileCodeResult;
import com.saki.sakiaicodetoolsbackend.core.parser.CodeParser;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 * 解析JSON格式的多文件代码内容，提取HTML、CSS、JS代码
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-04
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    /**
     * 解析多文件代码（HTML + CSS + JS）
     * 从JSON格式的AI响应中提取各类型代码文件
     *
     * @param codeContent JSON格式的代码内容，必须包含files数组
     * @return 多文件代码结果对象
     * @throws BusinessException 当JSON解析失败或格式不正确时抛出
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(codeContent);
            JsonNode files = root.get("files");

            // 验证files数组是否存在
            if (files == null || !files.isArray()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果缺少 files 数组");
            }

            // 遍历files数组，根据文件后缀分类代码
            for (JsonNode fileNode : files) {
                String name = fileNode.get("name").asText();
                String content = fileNode.get("content").asText("");

                // 根据文件后缀名分类存储代码
                if (name.endsWith(".html")) {
                    result.setHtmlCode(content);
                } else if (name.endsWith(".css")) {
                    result.setCssCode(content);
                } else if (name.endsWith(".js")) {
                    result.setJsCode(content);
                }
            }
            return result;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "JSON 解析失败: " + e.getMessage());
        }
    }
}
