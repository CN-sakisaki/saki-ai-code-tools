package com.saki.sakiaicodetoolsbackend.core;

import com.saki.sakiaicodetoolsbackend.ai.AiCodeGeneratorService;
import com.saki.sakiaicodetoolsbackend.ai.model.HtmlCodeResult;
import com.saki.sakiaicodetoolsbackend.ai.model.MultiFileCodeResult;
import com.saki.sakiaicodetoolsbackend.ai.model.enums.CodeGenTypeEnum;
import com.saki.sakiaicodetoolsbackend.core.parser.CodeParserExecutor;
import com.saki.sakiaicodetoolsbackend.core.saver.CodeFileSaverExecutor;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 * 采用外观模式封装复杂的AI代码生成流程，提供统一的生成和保存入口
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     * 同步生成方式，适用于小规模代码生成
     *
     * @param userMessage     用户提示词，不能为空
     * @param codeGenTypeEnum 生成类型枚举，不能为空
     * @param appId           应用ID，用于文件目录隔离
     * @return 保存的目录文件对象
     * @throws BusinessException 当生成类型为空或不支持时抛出
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 参数校验：生成类型不能为空
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 使用switch表达式根据不同类型执行相应生成逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 生成HTML单文件代码
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                // 执行文件保存并返回保存目录
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                // 生成多文件代码（HTML+CSS+JS）
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                // 不支持的类型处理
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 统一入口：根据类型生成并保存代码（流式）
     * 异步流式生成方式，适用于大规模代码生成和实时响应
     *
     * @param userMessage     用户提示词，不能为空
     * @param codeGenTypeEnum 生成类型枚举，不能为空
     * @param appId           应用ID，用于文件目录隔离
     * @return 流式响应数据流
     * @throws BusinessException 当生成类型为空或不支持时抛出
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 参数校验：生成类型不能为空
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 获取HTML代码流
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                // 处理流式数据并保存
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                // 获取多文件代码流
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                // 不支持的类型处理
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法
     * 实时收集代码片段并在流完成时保存文件
     *
     * @param codeStream  代码数据流，不能为空
     * @param codeGenType 代码生成类型，不能为空
     * @param appId       应用ID，用于文件目录隔离
     * @return 处理后的数据流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        // StringBuilder用于累积流式数据
        StringBuilder codeBuilder = new StringBuilder();

        return codeStream
                .doOnNext(chunk -> {
                    // 实时收集代码片段，用于后续文件保存
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式返回完成后异步保存代码文件
                    try {
                        String completeCode = codeBuilder.toString();
                        // 使用执行器解析代码内容
                        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        // 使用执行器保存代码文件
                        File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                        log.info("代码保存成功，路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("代码保存失败: {}", e.getMessage(), e);
                    }
                });
    }
}


