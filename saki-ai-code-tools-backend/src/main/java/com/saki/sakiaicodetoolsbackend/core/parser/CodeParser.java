package com.saki.sakiaicodetoolsbackend.core.parser;

import com.saki.sakiaicodetoolsbackend.exception.BusinessException;

/**
 * 代码解析器策略接口
 * 定义统一的代码解析规范，支持不同代码类型的解析实现
 *
 * @param <T> 解析结果类型
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     * 将原始代码字符串解析为结构化的结果对象
     *
     * @param codeContent 原始代码内容，不能为空
     * @return 解析后的结果对象
     * @throws BusinessException 当解析失败时抛出
     */
    T parseCode(String codeContent);
}

