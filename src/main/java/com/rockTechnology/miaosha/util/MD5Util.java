package com.rockTechnology.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 对密码做两次MD5
 */

public class MD5Util {
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";
    public static String inputPassToFormPass(String inputPass){
        String str = inputPass + salt;
        return md5(str);
    }
    public static String formPassToDBPass(String formPass, String salt){
        String str = formPass + salt;
        return md5(str);
    }

    public static String inputPassToDBPass(String inputPass, String salt){
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, salt);
        return dbPass;
    }

    public static void main(String[] args) {//4b14ae892e9baf8500327741950c3740
        System.out.println(formPassToDBPass("41710db9d452c1d74e64592bfac3269e","abc"));
    }

}
