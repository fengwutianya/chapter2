package com.xuan.chapter2.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by xuan on 2017/8/4 0004.
 */
public final class StringUtil {
    public static boolean isEmpty(String str) {
        if (str != null) {
            str = str.trim();
        }
        return StringUtils.isEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
