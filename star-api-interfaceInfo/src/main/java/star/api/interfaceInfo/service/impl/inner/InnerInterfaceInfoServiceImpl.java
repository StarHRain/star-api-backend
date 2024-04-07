package star.api.interfaceInfo.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import star.api.interfaceInfo.service.InterfaceInfoService;
import star.api.model.entity.InterfaceInfo;
import star.api.model.vo.InterfaceInfoVO;
import star.api.service.InnerInterfaceInfoService;

import javax.annotation.Resource;
import java.util.List;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 获取接口信息
     * @param url
     * @param method
     * @return
     */
    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        return interfaceInfoService.query()
                .eq("url", url)
                .eq("method", method)
                .one();
    }


}

