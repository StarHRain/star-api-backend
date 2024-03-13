package star.api.admin.service.impl.inner;

import org.apache.dubbo.config.annotation.DubboService;
import star.api.admin.service.UserService;
import star.api.model.entity.User;
import star.api.service.InnerUserService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public User getInvokeUser(String accessKey) {
        return userService.query()
                .eq("accessKey", accessKey)
                .one();
    }
}

