package com.saki.sakiaicodetoolsbackend.core.parser;

/**
 * 代码解析器策略接口
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}

