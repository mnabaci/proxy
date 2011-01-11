package com.android.proxy.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
	
	public static final String CHARSET = "UTF-8";
	
	public static byte[] Encrypt2Bytes(byte[] sSrc, String sKey) throws Exception {
        byte[] raw = sKey.getBytes(CHARSET);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(sSrc);
        return encrypted;
    }
    
    public static String Encrypt(String sSrc, String sKey) throws Exception {
        byte[] raw = sKey.getBytes(CHARSET);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        return byte2hex(encrypted).toLowerCase();
    }

    public static String Decrypt(String sSrc, String sKey) throws Exception {
        try {
            byte[] raw = sKey.getBytes(CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = hex2byte(sSrc.getBytes());
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                originalString = new String(originalString.getBytes(), "UTF-8");
                return originalString;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static String Decrypt(byte[] sSrc, String sKey) throws Exception {
        try {
            byte[] raw = sKey.getBytes("GBK");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = hex2byte(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                originalString = new String(originalString.getBytes(), "UTF-8");
                return originalString;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {

            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
    
    private static byte[] hex2byte(byte[] b) { 
        if((b.length%2)!=0) 
             throw new IllegalArgumentException("length is not even"); 
        byte[] b2 = new byte[b.length/2]; 
        for (int n = 0; n < b.length; n+=2) { 
           String item = new String(b,n,2);
           b2[n/2] = (byte)Integer.parseInt(item,16); 
         } 
        return b2; 
    } 
    
    public static String Encrypt(String sSrc){
    	try {
    		String sKey = "a09771a3e9601631";  
    		byte[] raw = sKey.getBytes();
    		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    		IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
    		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
    		byte[] encrypted = cipher.doFinal(sSrc.getBytes());
    		return Base64.encode(encrypted);
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
           
       }
      
    public static String Decrypt(String sSrc){
    	String sKey = "a09771a3e9601631";  //
    	try {
    		if(sSrc==null || "".equals(sSrc)){
    			return null;
    		}
    		byte[] raw = sKey.getBytes("ASCII");
    		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    		IvParameterSpec iv = new IvParameterSpec("0102030405060708"
    				.getBytes());
    		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
    		byte[] encrypted1 = Base64.decode(sSrc);
    		try {
    			byte[] original = cipher.doFinal(encrypted1);
    			String originalString = new String(original);
    			return originalString;
    		} catch (Exception e) {
    			System.out.println(e.toString());
    			return null;
    		}
    	} catch (Exception ex) {
    		System.out.println(ex.toString());
    		return null;
    	}
    }

}
