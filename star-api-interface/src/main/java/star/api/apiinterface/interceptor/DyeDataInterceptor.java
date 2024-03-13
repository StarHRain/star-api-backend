package star.api.apiinterface.interceptor;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 13 日
 */

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 染色数据拦截器
 */
public class DyeDataInterceptor implements HandlerInterceptor {
    private static final String DYE_DATA_HEADER = "X-Dye-Data";
    private static final String DYE_DATA_VALUE = "star";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception {
        //获取请求头中的染色数据
        String dyeData = request.getHeader(DYE_DATA_HEADER);

        if(dyeData == null||!dyeData.equals(DYE_DATA_VALUE)){
            //染色数据不存在||不匹配，返回错误响应
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
