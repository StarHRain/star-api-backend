package star.api.sdk.utils;

/**
 * @author 千树星雨
 * @date 2024年03月10日
 */

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.Digester;

/**
 * 签名生成类
 */
public class SignUtils {

    public static String getSign(String body, String secretKey) {
        String content = body + "." + secretKey;
        return DigestUtil.sha256Hex(content);
    }
}
