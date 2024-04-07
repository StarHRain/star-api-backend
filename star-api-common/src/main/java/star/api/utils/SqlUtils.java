package star.api.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 12 日
 */

/**
 * SQL工具
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField){
        if(StringUtils.isBlank(sortField)){
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
