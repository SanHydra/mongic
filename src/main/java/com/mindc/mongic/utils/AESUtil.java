package com.mindc.mongic.utils;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * AES加解密工具类
 */
public class AESUtil {
    private static final String ENCODE_RULES = "1111111101010101";


    /**
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
    public static String aesEncode(String content) {
        return encode(content,ENCODE_RULES);
    }

    /**
     * 解密
     * 解密过程：
     * 1.同加密1-4步
     * 2.将加密后的字符串反纺成byte[]数组
     * 3.将加密内容解密
     */
    public static String aesDecode(String content) {
        return decode(content,ENCODE_RULES);
    }

    public static String encode(String content,String pwd){
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec aes = new SecretKeySpec(pwd.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, aes);
            byte[] bytes = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decode(String content,String pwd){

        Cipher cipher = null;
        try {
            byte[] decode = Base64.getDecoder().decode(content.replace(" ","+").trim());

            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec aes = new SecretKeySpec(pwd.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, aes);

            byte[] bytes = cipher.doFinal(decode);

            return new String(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getValidPwd(String pwd){
        if (pwd == null){
            return "0000000000000000";
        }
        if (pwd.length() == 16){
            return pwd;
        }
        if (pwd.length() > 16){
            return pwd.substring(0,16);
        }
        int zeroNum = 16 - pwd.length();
        StringBuilder stringBuilder = new StringBuilder(pwd);
        for (int i = 0; i < zeroNum; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();

    }

}
