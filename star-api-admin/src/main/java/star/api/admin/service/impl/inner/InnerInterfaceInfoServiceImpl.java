package star.api.admin.service.impl.inner;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.dubbo.config.annotation.DubboService;
import star.api.admin.exception.BusinessException;
import star.api.admin.service.InterfaceInfoService;
import star.api.common.ErrorCode;
import star.api.model.entity.InterfaceInfo;
import star.api.service.InnerInterfaceInfoService;

import javax.annotation.Resource;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        return interfaceInfoService.query()
                .eq("url", url)
                .eq("method", method)
                .one();
    }

}

