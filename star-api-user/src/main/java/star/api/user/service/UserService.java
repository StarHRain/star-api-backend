package star.api.user.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import star.api.common.DeleteRequest;
import star.api.model.dto.user.UserAddRequest;
import star.api.model.dto.user.UserLoginRequest;
import star.api.model.dto.user.UserQueryRequest;
import star.api.model.dto.user.UserUpdateRequest;
import star.api.model.entity.User;
import star.api.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author 千树星雨
 */
public interface UserService extends IService<User> {

    /**
     * 更新密钥
     * @param request
     * @return
     */
    Boolean updateSecretKey(HttpServletRequest request);

    /**
     * 创建用户公钥
     * @param request
     * @return
     */
    String createAccessKey(HttpServletRequest request);

    /**
     * 创建密钥
     * @param request
     * @return
     */
    String createSecretKey(HttpServletRequest request);

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    Long addUser(UserAddRequest userAddRequest, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteUser(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    Boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @return
     */
    UserVO getUserVOById(long id);

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    List<UserVO> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request);

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request);

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param request
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(UserLoginRequest userLoginRequest,String token, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);
}
