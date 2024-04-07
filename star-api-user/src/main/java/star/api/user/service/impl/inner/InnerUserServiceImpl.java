package star.api.user.service.impl.inner;

import cn.hutool.core.bean.BeanUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import star.api.common.ErrorCode;
import star.api.exception.BusinessException;
import star.api.model.entity.User;
import star.api.model.enums.UserRoleEnum;
import star.api.model.vo.UserVO;
import star.api.service.InnerUserService;
import star.api.user.service.UserService;
import star.api.utils.ThrowUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static star.api.constant.RedisConstant.LOGIN_TOKEN_KEY;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @Override
    public User getInvokeUser(String accessKey) {
        return userService.query()
                .eq("accessKey", accessKey)
                .one();
    }

    /**
     * 获取当前登录用户
     *
     * @param token
     * @return
     */
    @Override
    public User getLoginUser(String token) {
        String headerTokenKey = LOGIN_TOKEN_KEY + token;
        Map<Object, Object> cacheUserMap = redisTemplate.opsForHash().entries(headerTokenKey);
        //若缓存没有数据则报错
        ThrowUtils.throwIf(cacheUserMap==null||cacheUserMap.isEmpty(), ErrorCode.FORBIDDEN_ERROR,"未登录");
        User user = BeanUtil.fillBeanWithMap(cacheUserMap, new User(), false);
        return user;
    }

    /**
     * 是否管理员
     * @param token
     * @return
     */
    @Override
    public boolean isAdmin(String token) {
        User user = getLoginUser(token);
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 通过 用户ID 获取用户
     *
     * @param id
     * @return
     */
    @Override
    public UserVO getUserVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return getUserVO(userService.getById(id));
    }

    /**
     * 获取用户信息（ VO
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 通过 UserIdSet 获取 Map<Id,List>
     * @param userIdSet
     * @return
     */
    @Override
    public Map<Long, List<User>> getIdUserMapByIds(Set<Long> userIdSet) {
        Map<Long, List<User>> idUserMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        return idUserMap;
    }
}



