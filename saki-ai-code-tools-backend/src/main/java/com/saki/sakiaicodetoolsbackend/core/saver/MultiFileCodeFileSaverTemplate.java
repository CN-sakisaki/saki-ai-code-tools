package com.saki.sakiaicodetoolsbackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.ai.model.MultiFileCodeResult;
import com.saki.sakiaicodetoolsbackend.ai.model.enums.CodeGenTypeEnum;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

/**
 * 多文件代码保存器
 * 负责保存HTML、CSS、JavaScript多文件代码
 * 继承模板方法模式，实现具体的多文件保存逻辑
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    /**
     * 获取代码类型
     *
     * @return MULTI_FILE 枚举值
     */
    @Override
    public CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 保存多文件代码的具体实现
     * 分别保存HTML、CSS、JS文件到指定目录
     *
     * @param result      多文件代码结果对象
     * @param baseDirPath 基础目录路径
     */
    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 保存 CSS 文件
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        // 保存 JavaScript 文件
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    /**
     * 验证多文件代码输入参数
     * 至少需要HTML代码，CSS和JS可以为空
     *
     * @param result 多文件代码结果对象
     * @throws BusinessException 当HTML代码为空时抛出
     */
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有 HTML 代码，CSS 和 JS 可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}

