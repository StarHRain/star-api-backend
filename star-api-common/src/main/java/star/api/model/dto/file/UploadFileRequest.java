package star.api.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 */
@Data
public class UploadFileRequest implements Serializable {

    private static final long serialVersionUID = 4035742270780814350L;
    /**
     * 业务
     */
    private String biz;

}
