package com.saki.sakiaicodetoolsbackend.controller;

import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import com.saki.sakiaicodetoolsbackend.model.vo.FileUploadVO;
import com.saki.sakiaicodetoolsbackend.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * <p>提供文件上传相关接口</p>
 * @author saki酱
 * @version 1.0
 * @since 2025-10-26
 */
@RestController
@Tag(name = "FileController", description = "文件上传接口")
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 通用文件上传接口
     *
     * <p>支持各种业务场景的文件上传，返回文件访问地址和元数据信息</p>
     *
     * @param file 上传的文件，必须使用 "file" 作为参数名
     * @param biz 业务类型标识，用于区分不同的上传场景
     * @param userId 用户ID，可选参数，用于关联上传用户
     * @param request HTTP 请求对象，用于获取请求上下文信息
     * @return 包含文件上传结果的响应对象
     * @throws IllegalArgumentException 当文件为空或业务类型为空时抛出
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "通用文件上传接口")
    public BaseResponse<FileUploadVO> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("biz") String biz,
            @RequestParam(value = "userId", required = false) Long userId,
            HttpServletRequest request) {

        FileUploadVO vo = fileService.uploadFile(file, biz, userId, request);

        return ResultUtils.success(vo);
    }
}
