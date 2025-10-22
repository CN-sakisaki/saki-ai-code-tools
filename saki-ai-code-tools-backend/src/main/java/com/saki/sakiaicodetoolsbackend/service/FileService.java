package com.saki.sakiaicodetoolsbackend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件服务。
 */
public interface FileService {

    /**
     * 上传文件并返回访问地址。
     *
     * @param file 上传文件
     * @param biz  业务标识
     * @param targetUserId 目标用户ID，可为空
     * @param httpServletRequest 当前请求
     * @return 文件访问地址
     */
    String uploadFile(MultipartFile file, String biz, Long targetUserId, HttpServletRequest httpServletRequest);
}
