package com.saki.sakiaicodetoolsbackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.ai.model.enums.CodeGenTypeEnum;
import com.saki.sakiaicodetoolsbackend.constant.AppConstants;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器 - 模板方法模式
 * 定义代码保存的标准流程，具体实现由子类完成
 * 采用模板方法模式确保保存流程的一致性
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
public abstract class CodeFileSaverTemplate<T> {

    /**
     * 文件保存根目录配置
     * 从应用常量中获取代码输出根目录
     */
    protected static final String FILE_SAVE_ROOT_DIR = AppConstants.CODE_OUTPUT_ROOT_DIR;

    /**
     * 模板方法：保存代码的标准流程
     * 定义保存过程的固定步骤：验证→创建目录→保存文件→返回结果
     *
     * @param result 代码结果对象，不能为空
     * @param appId  应用ID，用于目录隔离，不能为空
     * @return 保存的目录文件对象
     * @throws BusinessException 当参数验证失败时抛出
     */
    public final File saveCode(T result, Long appId) {
        // 1. 验证输入参数
        validateInput(result);
        // 2. 构建唯一目录路径
        String baseDirPath = buildUniqueDir(appId);
        // 3. 保存文件（具体实现由子类提供）
        saveFiles(result, baseDirPath);
        // 4. 返回目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入参数（可由子类覆盖增强）
     * 基础验证确保结果对象不为空
     *
     * @param result 代码结果对象
     * @throws BusinessException 当结果对象为空时抛出
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }


    /**
     * 构建唯一目录路径
     * 格式：根目录/代码类型_应用ID
     *
     * @param appId 应用ID，不能为空
     * @return 完整的目录路径
     * @throws BusinessException 当应用ID为空时抛出
     */
    protected final String buildUniqueDir(Long appId) {
        // 验证应用ID
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        String codeType = getCodeType().getValue();
        // 构建唯一目录名：代码类型_应用ID
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;

        // 创建目录（如果不存在）
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件的工具方法
     * 如果内容为空则跳过写入
     *
     * @param dirPath  目录路径，不能为空
     * @param filename 文件名，不能为空
     * @param content  文件内容，可以为空（空内容不写入）
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码类型（由子类实现）
     * 用于目录命名和逻辑路由
     *
     * @return 代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件的具体实现（由子类实现）
     * 子类根据具体代码类型实现文件保存逻辑
     *
     * @param result      代码结果对象
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}

