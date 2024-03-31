package star.api.model.vo;

import lombok.Data;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 26 日
 */

@Data
public class AiResultVO {
    /**
     * 图表id，需要保存成功后才有
     */
    private Long chartId;
    /**
     * 生成的图像数据
     */
    private String genChart;
    /**
     * 生成的分析结果
     */
    private String genResult;
}
