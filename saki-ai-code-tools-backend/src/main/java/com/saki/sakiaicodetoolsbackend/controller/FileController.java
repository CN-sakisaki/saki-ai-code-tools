package com.saki.sakiaicodetoolsbackend.controller;

import com.saki.sakiaicodetoolsbackend.annotation.AuthCheck;
import com.saki.sakiaicodetoolsbackend.common.BaseResponse;
import com.saki.sakiaicodetoolsbackend.common.ResultUtils;
import com.saki.sakiaicodetoolsbackend.constant.UserRoleConstant;
import com.saki.sakiaicodetoolsbackend.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器。
 */
@RestController
@Tag(name = "FileController", description = "文件上传接口")
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuthCheck(mustRole = UserRoleConstant.USER_ROLE)
    @Operation(description = "通用文件上传接口")
    public BaseResponse<String> upload(@RequestPart("file") MultipartFile file,
                                       @RequestParam("biz") String biz,
                                       @RequestParam(value = "userId", required = false) Long userId,
                                       HttpServletRequest httpServletRequest) {
        String url = fileService.uploadFile(file, biz, userId, httpServletRequest);
        return ResultUtils.success(url);
    }
}
