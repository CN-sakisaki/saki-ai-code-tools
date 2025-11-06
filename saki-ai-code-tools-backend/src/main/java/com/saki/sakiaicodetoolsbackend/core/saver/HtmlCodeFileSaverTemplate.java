package com.saki.sakiaicodetoolsbackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.ai.model.HtmlCodeResult;
import com.saki.sakiaicodetoolsbackend.ai.model.enums.CodeGenTypeEnum;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

/**
 * HTML代码文件保存器
 * 负责保存单文件HTML代码
 * 继承模板方法模式，实现具体的单文件保存逻辑
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    /**
     * 获取代码类型
     *
     * @return HTML 枚举值
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 保存HTML单文件代码的具体实现
     *
     * @param result      HTML代码结果对象
     * @param baseDirPath 基础目录路径
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    /**
     * 验证HTML代码输入参数
     *
     * @param result HTML代码结果对象
     * @throws BusinessException 当HTML代码为空时抛出
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}
