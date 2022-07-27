package MyProtocol;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSONObject;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MyProtocol {
    //public MyProtocol() {
    //    Security.addProvider(new BouncyCastleProvider());
    //}

    private String PRIV_KEY = "MIICXAIBAAKBgQCztkK+sbF4LzuKPshL9DmKbMq6mvvT7s+GVVmURiZNC8m4Awhe" +
            "BDje4RTdbDXqnhZSSS8MtziszfWPhvf1q3SnpkTa7G9+U8p835UG7SQSH6f3mOrJ" +
            "WMHqyYzDyOnNKc/V5am72D6qNgjwlr5FAHj2O3RstdaIcRW+HPuNjNNGqwIDAQAB" +
            "AoGBAK76oKRSGc0+mAd0N8wUoM4SPZZR/y8MkE1o3w7K+tH7z032zffUvpbsq0co" +
            "7JpjkLJQBZqo72r6IsW8EcTHS42rWMCt9uhDQrcJeQMToAY20TazfukIWOOMgLhn" +
            "MRLyL/g4ewocXHq/EXUF2uElFNy3Ti58+RHLUIjU40f8nJOxAkEA1/CldWDcM6M6" +
            "rzA6qvoibars5Epfn+6ExvtdpOQPDo5vkZOFQ3DtLNsyfpQVEsCdqFkpJVkNoIS5" +
            "qcKLrlDxCQJBANUNFSu7fKJbotNe7zL3borhFc65qVMkTUNtKhNgF8YzXFI0RIE5" +
            "Nhx4iPntikWD7ZHpUDBnd7lbS003zupGCxMCQGJi/sAwVaQhZweTDegA99bH3g9V" +
            "46PW5SBUPyJ11nZnZ2YItNs5hJa/eI47oi5dHHgrx5eAr7jHQGCch0/xCSECQA0A" +
            "Cl2ryBQkIVBih5gFjyI8T9dYbuOa4HgPzjR2dZzrf2OoutFjy1B7bmhJvVk2jqWL" +
            "pg/+EEkoL/UbRa337i8CQEvMm8SJOFYnj1YJ9jBWI80GG/3wG28+cwUglUvysypz" +
            "EUyktxfiogP76QBpRf6nG/DL0vDnGg8FnEJP5Yg20v4=";
    private String PUB_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCztkK+sbF4LzuKPshL9DmKbMq6mvvT7s+GVVmURiZNC8m4AwheBDje4RTdbDXqnhZSSS8MtziszfWPhvf1q3SnpkTa7G9+U8p835UG7SQSH6f3mOrJWMHqyYzDyOnNKc/V5am72D6qNgjwlr5FAHj2O3RstdaIcRW+HPuNjNNGqwIDAQAB";

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private byte[] AESKey;
    private byte[] AESIV;

    //RSA密钥长度
    private static final int KEY_SIZE = 1024;
    //AES
    //private static final String AES_ALGORI = "AES/CBC/PKCS5Padding";
    //private static final String AES_ALGORI = "AES/CBC/NoPadding";
    private static final String AES_ALGORI = "AES/CBC/ISO10126Padding";
    private static final String RSA_ALGORI = "RSA";

    public String decode(String payload, String key, String iv, String mac) throws UnsupportedEncodingException {
        try {
            Base64.Decoder base64dec = Base64.getDecoder();
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            //String payload_raw = URLDecoder.decode(payload, "UTF-8");
            String payload_raw = payload;
            byte[] payload_json_enc = base64dec.decode(payload_raw);
            //String mac_raw = URLDecoder.decode(mac, "UTF-8");
            String mac_raw = mac;
            //String mac_basedec = base64dec.decode(mac).toString();
            //String key_raw = URLDecoder.decode(key, "UTF-8");
            String key_raw = key;
            byte[] key_basedec = base64dec.decode(key_raw);
            //String iv_raw = URLDecoder.decode(iv, "UTF-8");
            String iv_raw = iv;
            byte[] iv_basedec = base64dec.decode(iv_raw);
            byte[] mac_cal = md.digest((key_raw + iv_raw + payload_raw).getBytes());
            //String mac_str = mac_cal.toString();
            String mac_str = bytes2HexString(mac_cal);

            //验证完整性
            if (!mac_str.equals(mac_raw))
                return new String("Mac Error");
            //assertTrue(mac_str.equals(mac_raw));
            //assertArrayEquals(mac_cal, mac_raw.getBytes());
            //RSA解密
            byte[] key_dec = decryptByPrivateKey(key_basedec);
            byte[] iv_dec = decryptByPrivateKey(iv_basedec);
            AESKey = key_dec;
            AESIV = iv_dec;
            printbytearray(key_dec);
            printbytearray(iv_dec);
            //AES CBC解密
            byte[] payload_json = decryptByAES(payload_json_enc, key_dec, iv_dec);
            return bytearray2string(payload_json);
        } catch (NoSuchAlgorithmException ae) {
            ae.printStackTrace();
            return new String("error");
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new String("error");
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new String("error");
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new String("error");
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new String("error");
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new String("error");
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new String("error");
        } finally {
            //return new String("Error");
        }
    }

    public byte[] decryptByPrivateKey(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(PRIV_KEY.getBytes());
        //KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //PrivateKey privateKey = keyFactory.generatePrivate((pkcs8EncodedKeySpec));
        Cipher cipher = Cipher.getInstance(RSA_ALGORI);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] key_dec = cipher.doFinal(data);
        return cleanRSAResult(key_dec);
    }

    public byte[] encryptByPublicKey(byte[] data) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(PUB_KEY.getBytes());
        //KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //PublicKey publicKey = keyFactory.generatePublic((x509EncodedKeySpec));
        Cipher cipher = Cipher.getInstance(RSA_ALGORI);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] key_dec = cipher.doFinal(data);
        return cleanRSAResult(key_dec);
    }

    public byte[] decryptByAES(byte[] data, byte[] key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(AES_ALGORI);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    /**
     * 应答消息格式
     * replymsg:{
     *  payload:{
     *      "data":data
     *      }
     *  mac:hashOfPayload
     * }
     */
    public String reply(String data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException {
        Base64.Encoder baseenc = Base64.getEncoder();
        JSONObject payload = new JSONObject();
        JSONObject replymsg = new JSONObject();
        payload.put("data", data);
        String payload_json = payload.toJSONString();

        byte[] payload_json_enc_byte = encryptByAES(payload_json.getBytes(), AESKey, AESIV);
        //String payload_json_enc = bytearray2string(payload_json_enc_byte);
        String payload_json_enc_base = baseenc.encodeToString(payload_json_enc_byte);
        //mac
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] mac_byte = md.digest(payload_json_enc_base.getBytes());
        String mac = bytes2HexString(mac_byte);

        replymsg.put("payload", payload_json_enc_byte);
        replymsg.put("mac", mac);

        return replymsg.toJSONString();

    }

    //生成指定大小的密钥对
    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put("RSAPublicKey", publicKey);
        keyMap.put("RSAPrivateKey", privateKey);
        return keyMap;
    }

    public static byte[] getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPrivateKey");
        return key.getEncoded();//base64
    }

    public static byte[] getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get("RSAPublicKey");
        return key.getEncoded();
    }

    public byte[] getMyPrivateKey() {
        return PRIV_KEY.getBytes();
    }

    public byte[] getMyPublicKey() {
        return PUB_KEY.getBytes();
    }

    //由文件设置公钥
    public PublicKey loadPublicKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        publicKey = kf.generatePublic(spec);
        PUB_KEY = publicKey.getEncoded().toString();
        return publicKey;
    }

    public PrivateKey loadPrivateKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(spec);
        PRIV_KEY = privateKey.getEncoded().toString();
        return privateKey;
    }

    public byte[] encryptByAES(byte[] data, byte[] key, byte[] iv)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        //Cipher cipher = Cipher.getInstance(AES_ALGORI, "BC");
        Cipher cipher = Cipher.getInstance(AES_ALGORI);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    public String getAeString(int len) {
        String chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";
        int maxpos = chars.length();
        String res = "";
        for (int i = 0; i < len; i++) {
            res += chars.charAt((int) Math.floor(Math.random() * maxpos));
        }
        return res;
    }

    /**
    * @Title:bytes2HexString
    * @Description:字节数组转16进制字符串
    * @param b
    * 字节数组
    * @return 16进制字符串
    * @throws
    */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(String.format("%02x", b[i]));
        }
        return result.toString();
    }

    /**
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     * @param src
     * 16进制字符串
     * @return 字节数组
     * @throws
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    //RSA解密后的byte数组有效值在末尾，之前的部分都为0，去掉为0的元素
    public static byte[] cleanRSAResult(byte[] data) {
        int num = 0;

        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0)
                num++;
        }
        byte[] res = new byte[num];
        for (int i = num - 1; i >= 0; i--) {
            res[i] = data[data.length - (num - i)];
        }
        return res;

    }

    public static void printbytearray(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            System.out.print(String.format("%c", (int) data[i]));
            //System.out.print(",");
        }
        System.out.println();
    }

    public static String bytearray2string(byte[] data) {
        int i = 0;
        try {
            StringBuffer str = new StringBuffer();
            for (i = 0; i < data.length; i++) {
                str.append(String.format("%c", (int) data[i]));
            }
            return str.toString();
        } catch (IllegalFormatCodePointException e) {
            e.printStackTrace();
            System.out.println(i);
            System.out.println(data[i]);
            System.out.println((int) data[i]);

            return "error";
        }
    }
}
