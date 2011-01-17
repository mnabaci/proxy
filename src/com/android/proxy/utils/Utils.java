package com.android.proxy.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
	
	public static final String CHARSET = "UTF-8";
	public static final String TRUST_KEY = "bc048293492skels";
	
	public static String Encrypt(String sSrc, String sKey) {
		try {
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
    
    public static String Encrypt(String sSrc){
		String sKey = "a09771a3e9601631";  
		return Encrypt(sSrc, sKey);
    }
    
    public static String Decrypt(String sSrc, String sKey) {
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
      
    public static String Decrypt(String sSrc){
    	String sKey = "a09771a3e9601631";  //
    	return Decrypt(sSrc, sKey);
    }

}
