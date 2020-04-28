package com.rockTechnology.miaosha.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用正则表达式验证输入手机号格式是否正确
 */
public class ValidatorUtil {
    private static final Pattern mobile_patten = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String mobile){
        if (mobile == null){
            return false;
        }
        Matcher matcher = mobile_patten.matcher(mobile);
        return matcher.matches();
    }

}
