package star.api.chart.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import star.api.chart.mapper.ImageMapper;
import star.api.chart.service.ImageService;
import star.api.model.entity.Image;

/**
* @author 千树星雨
* @description 针对表【image(图片分析表)】的数据库操作Service实现
* @createDate 2023-12-13 22:42:19
*/
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService {

}




