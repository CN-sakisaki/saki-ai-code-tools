package com.saki.sakiaicodetoolsbackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 文件上传返回结果
 *
 * @author saki
 * @since 2025-10-22
 */
@Data
@Builder
@Schema(description = "文件上传返回结果")
public class FileUploadVO {

    @Schema(description = "文件访问 URL")
    private String url;

    @Schema(description = "COS 对象键（路径）")
    private String objectKey;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件后缀名")
    private String extension;
}
