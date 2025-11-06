package com.saki.sakiaicodetoolsbackend.core.parser.impl;

import com.saki.sakiaicodetoolsbackend.ai.model.HtmlCodeResult;
import com.saki.sakiaicodetoolsbackend.core.parser.CodeParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 * 从Markdown代码块或纯文本中提取HTML代码
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    /**
     * HTML代码块正则表达式
     * 用于匹配 ```html ... ``` 格式的代码块
     */
    private static final Pattern HTML_CODE_PATTERN =
            Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析HTML代码内容
     * 优先提取代码块，如果没有找到则使用整个内容
     *
     * @param codeContent 包含HTML代码的原始内容
     * @return HTML代码结果对象
     */
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码块
        String htmlCode = extractHtmlCode(codeContent);

        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            // 使用提取的代码块内容
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 使用正则表达式提取HTML代码内容
     *
     * @param content 原始内容字符串
     * @return 提取的HTML代码，如果没有匹配则返回null
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

