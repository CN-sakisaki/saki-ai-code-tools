package com.saki.sakiaicodetoolsbackend.ai.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-11-03
 */
@Schema(description = "生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -5076459810673478680L;

    @Schema(description = "HTML代码")
    private String htmlCode;

    @Schema(description = "生成代码的描述")
    private String description;
}


