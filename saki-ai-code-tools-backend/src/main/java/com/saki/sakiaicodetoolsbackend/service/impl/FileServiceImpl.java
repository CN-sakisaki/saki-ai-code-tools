package com.saki.sakiaicodetoolsbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.saki.sakiaicodetoolsbackend.exception.BusinessException;
import com.saki.sakiaicodetoolsbackend.exception.ErrorCode;
import com.saki.sakiaicodetoolsbackend.exception.ThrowUtils;
import com.saki.sakiaicodetoolsbackend.manager.CosManager;
import com.saki.sakiaicodetoolsbackend.model.vo.FileUploadVO;
import com.saki.sakiaicodetoolsbackend.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件服务实现类
 * 提供文件上传、类型校验、临时文件管理等功能
 *
 * @author saki酱
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    /**
     * 最大文件大小：10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 允许的图片文件扩展名
     */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * 腾讯云COS管理器
     */
    private final CosManager cosManager;

    /**
     * 上传文件到对象存储
     * 包含文件校验、类型验证、临时文件处理和上传逻辑
     *
     * @param file 上传的文件
     * @param biz 业务类型（如：user_avatar）
     * @param userId 用户ID
     * @param request HTTP请求对象
     * @return 文件上传结果
     * @throws BusinessException 当文件为空、大小超限、类型不合法或上传失败时抛出
     */
    @Override
    public FileUploadVO uploadFile(MultipartFile file, String biz, Long userId, HttpServletRequest request) {
        // 基础校验
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_MISSING, "文件不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(biz), ErrorCode.PARAMS_MISSING, "业务类型不能为空");
        ThrowUtils.throwIf(file.getSize() > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过10MB");

        // 校验文件类型
        String extension = resolveFileExtension(file);
        validateFileType(biz, extension);

        // 构建对象存储路径
        String objectKey = buildObjectKey(biz, extension);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload-", "." + extension);
            file.transferTo(tempFile);

            // 上传到腾讯云 COS
            String url = cosManager.uploadFile(objectKey, tempFile);
            ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.EXTERNAL_SERVICE_ERROR, "文件上传失败");

            log.info("文件上传成功：biz={}, userId={}, key={}", biz, userId, objectKey);

            // 上传成功后返回结果
            return FileUploadVO.builder()
                    .url(url)
                    .objectKey(objectKey)
                    .originalName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .extension(extension)
                    .build();
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败，请稍后重试");
        } finally {
            deleteTempFileQuietly(tempFile);
        }
    }

    /**
     * 解析文件扩展名
     * 优先从文件名获取，失败时从Content-Type解析
     *
     * @param file 上传的文件
     * @return 文件扩展名（小写，不含特殊字符）
     * @throws BusinessException 当无法识别文件类型时抛出
     */
    private String resolveFileExtension(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (StrUtil.isBlank(extension)) {
            String contentType = file.getContentType();
            if (StrUtil.isNotBlank(contentType) && contentType.contains("/")) {
                extension = contentType.substring(contentType.indexOf('/') + 1);
            }
        }
        ThrowUtils.throwIf(StrUtil.isBlank(extension), ErrorCode.PARAMS_ERROR, "无法识别文件类型");
        return extension.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /**
     * 校验文件类型合法性
     * 根据业务类型验证对应的文件扩展名是否允许
     *
     * @param biz 业务类型
     * @param extension 文件扩展名
     * @throws BusinessException 当文件类型不符合业务要求时抛出
     */
    private void validateFileType(String biz, String extension) {
        // 目前仅 user_avatar 支持图片
        if (StrUtil.equalsIgnoreCase(biz, "user_avatar")) {
            ThrowUtils.throwIf(!ALLOWED_IMAGE_EXTENSIONS.contains(extension), ErrorCode.PARAMS_ERROR, "头像仅支持上传图片文件");
        }
        // 你可以在这里扩展更多业务类型，例如：
        // article_cover -> 仅支持图片
        // document_upload -> 支持 pdf, docx 等
    }

    /**
     * 构建 COS 对象路径
     * 格式：/{业务类型}/{日期}/{UUID}.{扩展名}
     *
     * @param biz 业务类型
     * @param extension 文件扩展名
     * @return 对象存储路径
     */
    private String buildObjectKey(String biz, String extension) {
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String randomName = UUID.randomUUID().toString().replace("-", "");
        return String.format("/%s/%s/%s.%s", biz, datePath, randomName, extension);
    }

    /**
     * 安全删除临时文件
     * 静默处理删除失败的情况，仅记录警告日志
     *
     * @param file 要删除的临时文件
     */
    private void deleteTempFileQuietly(File file) {
        if (file != null && file.exists() && !file.delete()) {
            log.warn("删除临时文件失败：{}", file.getAbsolutePath());
        }
    }
}