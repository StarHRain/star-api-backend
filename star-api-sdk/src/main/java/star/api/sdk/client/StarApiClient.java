package star.api.sdk.client;

/**
 * @author 千树星雨
 * @date 2024年03月10日
 */

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static star.api.sdk.utils.SignUtils.getSign;


/**
 *  API 调用
 */
public class StarApiClient {
    public static String GATWAY_HOST="http://localhost:8090";

    private String accessKey;

    private String secretKey;

    public StarApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public void setGatwayHost(String gatwayHost) {GATWAY_HOST=gatwayHost;}

    private Map<String,String> getHeaderMap(String body,String method) {
        HashMap<String, String> map = new HashMap();
        map.put("accessKey", accessKey);
        map.put("nonce", RandomUtil.randomNumbers(10));
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("sign", getSign(body, secretKey));
        body = URLUtil.encode(body, CharsetUtil.CHARSET_UTF_8);
        map.put("body", body);
        map.put("method", method);
        return map;
    }

    public String invokeInterface(String params,String url,String method) throws UnsupportedEncodingException {
        HttpResponse httpResponse = HttpRequest.post(GATWAY_HOST + url)
                .header("Accept-Charset", CharsetUtil.UTF_8)
                .addHeaders(getHeaderMap(params, method))
                .body(params)
                .execute();
        return JSONUtil.formatJsonStr(httpResponse.body());
    }
}
