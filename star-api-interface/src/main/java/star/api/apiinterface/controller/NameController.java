package star.api.apiinterface.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import star.api.sdk.model.User;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 13 日
 */
@RestController
public class NameController {
    @PostMapping("/api/name/user")
    public String getUserNameByPost(@RequestBody User user) {
        return "POST 你的用户名字是"+user.getUsername();
    }

    @GetMapping("/api/name")
    public String getUserNameByGet() {
        return "GET 你的用户名字是";
    }
}
