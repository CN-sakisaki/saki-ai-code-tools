package com.saki.sakiaicodetoolsbackend.core.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saki.sakiaicodetoolsbackend.ai.model.MultiFileCodeResult;
import com.saki.sakiaicodetoolsbackend.core.parser.CodeParser;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 * @author saki酱
 * @version 1.0
 * @since 2025-11-04
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    /**
     * 解析多文件代码（HTML + CSS + JS）
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(codeContent);
            JsonNode files = root.get("files");
            if (files == null || !files.isArray()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 返回结果缺少 files 数组");
            }

            for (JsonNode f : files) {
                String name = f.get("name").asText();
                String content = f.get("content").asText("");

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
