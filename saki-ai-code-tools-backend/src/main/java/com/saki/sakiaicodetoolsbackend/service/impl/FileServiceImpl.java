package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.service.FileService;
import com.saki.sakiaicodetoolsbackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务实现。
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String BIZ_USER_AVATAR = "user_avatar";

    private final UserService userService;

    @Override
    public String uploadFile(MultipartFile file, String biz, Long targetUserId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_MISSING, "文件不能为空");
        String trimmedBiz = StrUtil.trimToNull(biz);
        ThrowUtils.throwIf(StrUtil.isBlank(trimmedBiz), ErrorCode.PARAMS_MISSING, "业务类型不能为空");

        if (StrUtil.equalsIgnoreCase(trimmedBiz, BIZ_USER_AVATAR)) {
            return userService.uploadAvatar(file, targetUserId, httpServletRequest);
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂不支持的文件上传业务类型");
    }
}
