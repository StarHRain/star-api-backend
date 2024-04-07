package star.api.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import star.api.common.DeleteRequest;
import star.api.common.ErrorCode;
import star.api.exception.BusinessException;
import star.api.model.dto.user.UserAddRequest;
import star.api.model.dto.user.UserLoginRequest;
import star.api.model.dto.user.UserQueryRequest;
import star.api.model.dto.user.UserUpdateRequest;
import star.api.model.entity.User;
import star.api.model.enums.UserRoleEnum;
import star.api.model.vo.UserVO;
import star.api.user.mapper.UserMapper;
import star.api.user.service.UserService;
import star.api.utils.ThrowUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static star.api.constant.RedisConstant.LOGIN_TOKEN_KEY;
import static star.api.constant.RedisConstant.LOGIN_TOKEN_TTL;


/**
 * 用户服务实现类
 *
 * @author 千树星雨
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "Star";


    @Override
    public Boolean updateSecretKey(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        loginUser.setAccessKey(this.createAccessKey(request));
        loginUser.setSecretKey(this.createSecretKey(request));
        return this.updateById(loginUser);
    }

    @Override
    public String createAccessKey(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        String accessKey = DigestUtil.md5Hex(SALT + loginUser.getUserAccount() + RandomUtil.randomNumbers(5));
        return accessKey;
    }

    @Override
    public String createSecretKey(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        String secretKey = DigestUtil.md5Hex(SALT + loginUser.getUserAccount() + RandomUtil.randomNumbers(8));
        return secretKey;
    }

    /**
     * 添加用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @Override
    public Long addUser(UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return user.getId();
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteUser(DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = this.removeById(deleteRequest.getId());
        return b;
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = this.updateById(user);
        return result;
    }

    /**
     * 获取用户（用户ID
     *
     * @param id
     * @return
     */
    @Override
    public UserVO getUserVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return getUserVO(this.getById(id));
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @Override
    public List<UserVO> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = this.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }

    /**
     * 获取用户列表（分页
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = 1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = this.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }


        // 账户不能重复 获取锁 然后注册账户
        User user = new User();

        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 分配 accessKey, secretKey
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(4));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
        // 4. 插入数据
        user = new User();
        user.setUserAccount(userAccount);
        user.setUserName(StringUtils.upperCase(userAccount));
        user.setUserPassword(encryptPassword);
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        user.setUserAvatar("https://image-bed-ichensw.oss-cn-hangzhou.aliyuncs.com/Multiavatar-f5871c303317a4dafbf6.png");
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param request
     * @return
     */
    @Override
    public UserVO userLogin(UserLoginRequest userLoginRequest, String token, HttpServletRequest request) {
        // 1. 校验
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 缓存用户信息
        Map<String, Object> userMap = BeanUtil.beanToMap(user);
        String tokenKey = LOGIN_TOKEN_KEY + token;
        redisTemplate.opsForHash().putAll(tokenKey, userMap);
        redisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.MINUTES);

        return this.getUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        String headerTokenKey = LOGIN_TOKEN_KEY + request.getHeader("Authorization");
        Map<Object, Object> cacheUserMap = redisTemplate.opsForHash().entries(headerTokenKey);
        //若缓存没有数据则报错
        ThrowUtils.throwIf(cacheUserMap==null||cacheUserMap.isEmpty(),ErrorCode.FORBIDDEN_ERROR,"未登录");
        User user = BeanUtil.fillBeanWithMap(cacheUserMap, new User(), false);
        return user;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        User user = getLoginUser(request);
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        // 移除登录态
        String headerTokenKey = LOGIN_TOKEN_KEY + request.getHeader("Authorization");
        // 删除 Redis 缓存中的键
        redisTemplate.delete(headerTokenKey);
        return true;
    }

    /**
     * 获取脱敏用户信息
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
     * 获取脱敏用户信息列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


}




