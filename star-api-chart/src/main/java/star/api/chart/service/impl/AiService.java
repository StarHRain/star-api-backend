package star.api.chart.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import star.api.chart.utils.BigModelChar;
import star.api.chart.utils.BigModelImage;
import star.api.model.vo.AiResultVO;

import javax.annotation.Resource;
import java.io.File;

@Service
public class AiService {
    @Resource
    private  RedissonClient redissonClient;

    /**
     * 星火大模型分析数据生成数学分析图
     * @param chartId
     * @param question
     * @return
     */
    public AiResultVO genChart(long chartId, String question) {
        BigModelChar bigModelChar = new BigModelChar(chartId,redissonClient);
        bigModelChar.getResult(question);
        String aReturn = bigModelChar.getReturn();

        String genChart = "服务错误";
        String genResult = "服务错误";
        if(aReturn.contains("：") && aReturn.contains("然后输出【【【【【"))
            genResult = aReturn.substring(aReturn.indexOf("：") + 1,aReturn.indexOf("然后输出【【【【【"));
        String[] split = aReturn.split("```json");
        if(split.length == 2){
            genChart = split[1].substring(0,split[1].indexOf("```"));
        }

        AiResultVO aiResultVO = new AiResultVO();
        aiResultVO.setGenChart(genChart);
        aiResultVO.setGenResult(genResult);

        return aiResultVO;
    }


    /**
     * 星火认知大模型-图片理解
     * @param file
     * @param question
     * @param id
     * @return
     */
    public String pictureToText(File file, String question, Long id) {
        BigModelImage bigModelImage = new BigModelImage(file,redissonClient,id);
        bigModelImage.getResult(question);
        String res = bigModelImage.getReturn();
        if(StringUtils.isBlank(res)){
            throw new RuntimeException("AI分析图片异常");
        }
        return res;
    }
}
