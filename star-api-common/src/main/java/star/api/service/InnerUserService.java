package star.api.service;

import star.api.model.entity.User;
import star.api.model.vo.UserVO;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 用户服务
 *
 * @author 千树星雨
 */
public interface InnerUserService {

    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * @param accessKey accessKey
     * @return User 用户信息
     */
    User getInvokeUser(String accessKey);

    /**
     * 得到当前用户
     * @param token
     * @return
     */
    User getLoginUser(String token);

    /**
     * 当前用户是否为管理员
     * @param token
     * @return
     */
    boolean isAdmin(String token);

    /**
     * 通过 用户ID 获取用户
     * @param id
     * @return
     */
    UserVO getUserVOById(long id);

    /**
     * 获取用户（ VO
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 通过 UserIdSet 获取 Map<ID,LIST>
     * @param userIdSet
     * @return
     */
    Map<Long, List<User>> getIdUserMapByIds(Set<Long> userIdSet);
}
