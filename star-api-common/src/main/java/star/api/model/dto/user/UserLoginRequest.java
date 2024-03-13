package star.api.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 8535228054801491129L;

    private String userAccount;

    private String userPassword;
}
