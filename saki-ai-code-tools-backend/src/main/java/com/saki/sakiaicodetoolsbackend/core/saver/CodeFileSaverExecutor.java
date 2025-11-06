package com.saki.sakiaicodetoolsbackend.core.saver;

import com.saki.sakiaicodetoolsbackend.ai.model.HtmlCodeResult;
import com.saki.sakiaicodetoolsbackend.ai.model.MultiFileCodeResult;
import com.saki.sakiaicodetoolsbackend.ai.model.enums.CodeGenTypeEnum;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 * 采用静态工厂方法模式，封装保存器的创建和调用
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public class CodeFileSaverExecutor {

    /**
     * HTML代码保存器实例
     */
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    /**
     * 多文件代码保存器实例
     */
    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     * 根据代码类型路由到相应的保存器
     *
     * @param codeResult  代码结果对象，不能为空
     * @param codeGenType 代码生成类型，不能为空
     * @param appId       应用ID，不能为空
     * @return 保存的目录文件对象
     * @throws BusinessException 当代码类型不支持时抛出
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}

