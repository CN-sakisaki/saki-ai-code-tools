package com.saki.sakiaicodetoolsbackend.core.parser;

import com.saki.sakiaicodetoolsbackend.ai.model.enums.CodeGenTypeEnum;
import com.saki.sakiaicodetoolsbackend.core.parser.impl.HtmlCodeParser;
import com.saki.sakiaicodetoolsbackend.core.parser.impl.MultiFileCodeParser;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 * 采用静态工厂方法模式，封装解析器的创建和调用
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public class CodeParserExecutor {

    /**
     * HTML代码解析器实例
     */
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    /**
     * 多文件代码解析器实例
     */
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * 根据代码类型路由到相应的解析器
     *
     * @param codeContent 原始代码内容，不能为空
     * @param codeGenType 代码生成类型，不能为空
     * @return 解析结果对象（HtmlCodeResult 或 MultiFileCodeResult）
     * @throws BusinessException 当代码类型不支持时抛出
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}

