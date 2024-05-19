package star.api.interfaceInfo.es.dto;

import com.google.gson.Gson;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import star.api.model.entity.InterfaceInfo;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子 ES 包装类
 *
 **/
// todo 取消注释开启 ES（须先配置 ES）
@Document(indexName = "interfaceInfo")
@Data
public class InterfaceInfoEsDTO implements Serializable {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 主机名
     */
    private String host;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求参数说明
     */
    private String requestParamsRemark;


    /**
     * 响应参数说明
     */
    private String responseParamsRemark;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;


    /**
     * 创建时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

    private static final Gson GSON = new Gson();

    /**
     * 对象转包装类
     *
     * @param interfaceInfo
     * @return
     */
    public static InterfaceInfoEsDTO objToDto(InterfaceInfo interfaceInfo) {
        if (interfaceInfo == null) {
            return null;
        }
        InterfaceInfoEsDTO interfaceInfoEsDTO = new InterfaceInfoEsDTO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoEsDTO);
        return interfaceInfoEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param interfaceInfoEsDTO
     * @return
     */
    public static InterfaceInfo dtoToObj(InterfaceInfoEsDTO interfaceInfoEsDTO) {
        if (interfaceInfoEsDTO == null) {
            return null;
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoEsDTO, interfaceInfo);
        return interfaceInfo;
    }
}
