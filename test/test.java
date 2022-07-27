import static org.junit.Assert.assertArrayEquals;
import MyProtocol.MyProtocol;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;

public class test {
    @Test
    public void URLDecTest() throws UnsupportedEncodingException {
        String raw = URLDecoder.decode(
                "https://cn.bing.com/search?q=java+url_decode&qs=CT&pq=java+url&sk=CT1&sc=6-8&cvid=3CFFF5849FB94738BE3EA3E04569C497&FORM=QBRE&sp=2",
                "UTF-8");
        System.out.println(raw);
    }

    @Test
    public void MyProtocolAESTest() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException,
            NoSuchProviderException {
        MyProtocol mp = new MyProtocol();
        //AES test
        String aes_key = mp.getAeString(32);
        String aes_iv = mp.getAeString(16);
        String data_ini = "powershell";
        byte[] data_enc = mp.encryptByAES(data_ini.getBytes(), aes_key.getBytes(), aes_iv.getBytes());
        byte[] data_dec = mp.decryptByAES(data_enc, aes_key.getBytes(), aes_iv.getBytes());
        assertArrayEquals(data_dec, data_ini.getBytes());

    }

    @Test
    public void MyProtocolRSATest() throws Exception {
        MyProtocol mp = new MyProtocol();
        mp.loadPrivateKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\pkcs8_private_der.key");
        mp.loadPublicKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\rsa_public_key.der");
        byte[] data_ini = "powershell".getBytes();
        byte[] data_enc = mp.encryptByPublicKey(data_ini);
        System.out.println(data_enc.length);

        Map<String, Object> keyMap = MyProtocol.initKey();
        byte[] pubKey = MyProtocol.getPublicKey(keyMap);
        System.out.println(pubKey.toString());
        System.out.println(pubKey.length);
        System.out.println(MyProtocol.getPrivateKey(keyMap).length);
        System.out.println(mp.getMyPublicKey().length);
        System.out.println(mp.getMyPrivateKey().length);

        byte[] data_dec = mp.decryptByPrivateKey(data_enc);//能正常解密
        System.out.println(data_dec.length);

        String str = Base64.getEncoder().encodeToString(data_enc);
        System.out.println(str);
        System.out.println(str.length());
        //assertArrayEquals(data_dec, data_ini);

        //JS的加密结果，无法正常解密  将加密模式RSA/ECB/PKCS1Padding 换成RSA后成功
        String cipher = "pcG47HiDRjM3szF2XrpTNv4sEwnDenj+mRa4twHmdjWJXZGKco/DUaxUiQYE/RqiUhvwvp5l4GfD2qGDfg9OlJ4w6RQSLA8aT41cR4GK6q9ZGczgew/Rc4yqlRc9e0NgQtX4taq9izQchLPV2GkyfinRx8ojhM0fwIWgdDDuwlo=";
        byte[] cipher_basedec = Base64.getDecoder().decode(cipher);
        byte[] msg = mp.decryptByPrivateKey(cipher_basedec);
        System.out.println(msg.length);

    }

    @Test
    public void MyProtocolDecodeTest() throws Exception {
        MyProtocol mp = new MyProtocol();
        mp.loadPrivateKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\pkcs8_private_der.key");
        mp.loadPublicKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\rsa_public_key.der");
        //keystr="fXjsAtfYbPJ5n3SDBhiGDE6ZbRjM5N8Y"
        //iv="HMBYbpSyiPdNhsCs"
        //payload_json="{"username":"user","password":"123"}"
        //payload_json_enc="NAYjBqzdxOaXJwu+RX0Is8gHsDZo4GkV6saETFzqqIwhmZXm/y5I7fGaW8YsYB7j"

        //Pkcs7Padding √
        //String key_raw = "cbjAI7dQ0m6I%2BnMr1IHjSpGtoVwoYCfd4Nfeyc%2BVjWh2%2ByjVs7OrdWn7%2BtRrDOn1zOlUFhf681XyGPhMmdd8udkvuajkcMcRgCua52aDsXrk84vcVSegWWRx1YKjc0%2BcxRgDeO69T6bNqxVx0ZtPWLmtaEtcvMxk1lOlSi0I1aY%3D";
        //String iv_raw = "c9X%2BlZXMvNebIfreId7pfdyeUQHmDdujBYwnnJi3IqJb9Z0r65ffMVt9Fnzy3W4irBMRiPlGKhSP242Rw%2B22aG559oDUAm4SghS00EKW8Ay81A%2B3I0WRy5rAQZ2FVSL7SkiBdM%2BGDkNC0iDkhuX%2FvGaJQZQT4Vqk%2B0wL32LACiI%3D";
        //String payload_raw = "NAYjBqzdxOaXJwu%2BRX0Is8gHsDZo4GkV6saETFzqqIwhmZXm%2Fy5I7fGaW8YsYB7j";
        //String mac_trans = "739ba202a43881820835bddaba83d33c1ead1b0b30be1262c9a295ea1b582292e0e807c439338fdd6c65c49d381035d369b5309450ad85111826ed885ec1c7bb";

        //NoPadding × "Input length not multiple of 16 bytes"
        //String key_raw = "ZfXt7yuq75k6RbAB656TFNxvuKIxEmupbLU%2BTbEjqoWxjflJZPeZc4V%2BB9vznf38qu4NG6bhpfVZlIU9%2F0GZggv2bcreU6jL%2FK%2BYMsXsUOFDkfP%2FEpNern9f5i6ce97s4JNFvia4bQ1EU1z%2B9Kxfj%2FuX7tdYj052Sd5Mpdsv5fo%3D";
        //String iv_raw = "L3NAYIqA%2FKH17iRRvPNNV3%2B79GN%2FlJ52F63QrbfoXgLBiZiolTVbbpHqBQOeCPI%2Bi1TQr2q6IX%2F%2BeGnq7tA12F94inVImNI9HAY8d%2BHYhbDmzNfWKyV4%2FgL%2F1rtcrgC9f2MMgWYQ58nnxEWRQ9iAe7Z4c6%2BiYfb9bQn5e8T1lhU%3D";
        //String payload_raw = "%2FnMjZA5U1aPuc9IUDd%2FuO0wsA1Dt01y3GcUandgAJF5aLt9e";
        //String mac_trans = "83ab397ecdb7f6946eb01f195e01e0b93ba0b97122c3a0de390e833db6badf075afaeb43f36ca46757d203934539a1a7e918a43f77d9f17375c62cfb31ce7199";

        //ISO10126Padding √
        //String key_raw = "TAKCIwYzCN6Z42EaYWw4B%2FXd08BdFB%2B47Ttu5y9qDyJUO5jxhfuru%2FVxFC%2BQn0bXp8%2FP4dZQmd%2BAWnU4n8A4GuXcqpOvZrb1owmum%2FODPUrCTxTyFrAACiggr4Mh7g4XdLTmjD7wK%2Bcjulkomb41FiZTyVgZvrpdjDzvXktawKw%3D";
        //String iv_raw = "A62YEGmC2gxUQNNjz%2B492HmETFzjoVp%2FfcNcWKyzcwTrsB7FUb%2FlGTrG0k09gGoA5AVHbUHzv9eanvY%2FgsvF83sckQTMNa%2Bj5T2DspC06y6Vp%2F2%2B9q3oJF7WkWTyIObThbz1TO7dstgQNfQImzAmcGXo6%2FBPJsXplTUsL%2BNnLmY%3D";
        //String payload_raw = "rKqWn2vHidwsa47vpQ4VPXf%2FRYT%2BQCOSPRchOQwmZfzziMDOAbISnc0JBhW5oakH";
        //String mac_trans = "cda5023df156f97eba435d9ac236dbe8eb89206fb2691f9bc94708f92258ae145582f1996bbbbc3952d57deca6a52832f701821020fd449c081fb48a9edd26fc";
        String payload_raw = "72a8KmrLfUM01kwspZf89uxs2kJG/+NJcAFj693vwrVTLDheeyWD7tU4mOoNNBbE";
        String key_raw = "eTfqkRAEPmhJ0EwakOwft+4XmnvgziTpf9fNoPr07yp6AYsH92AVQbobRUq9wbse20nG+Co4HzUUfhYG+Hj0fMFtwxQ4ymBmg1P2Opx8KGd9su5Cv6DGemNMic0T0Ycosxagb3izIdIXe+OFk6YpO2bOyhjIeKzl5+PyvmJd37s=";
        String iv_raw = "rh6jb+i2X15JWufN7Slq4Fu1zkEf00H4M2BpwkPyV4vsV/P2flGSB76T+WNepdIH9N6pWwmA9Hxdr9j7byxKonpbsB41n0JyC3F8TVPNHUzgmzW9fzy3IHJWvs/BDzW2MFQ7sgJxLYZyG1J1vH4Z0PYeAwI2dqhI765Bqh3mBt0=";
        String mac_trans = "2e9d30ae65ecae064adf48b026df38c0aa379d8d9d52a55b1641073fa09f7ff1f4c3f3462a959d760cc0b0f8ddf16471a7595fdcb86256d2c1bc8fabb2487bfe";
        String str = mp.decode(payload_raw, key_raw, iv_raw, mac_trans);
        String reply = mp.reply(str);
        System.out.println(str);

        System.out.println(reply);

    }

    @Test
    public void DBResponseTest1() throws SQLException, FileNotFoundException {
        DBResponse dbResponse = new DBResponse();
        //get columns
        List<String> list = dbResponse.getColumnNames("course");
        for (String i : list) {
            System.out.println(i);
        }
        //select *
        List<String> dList = dbResponse.queryAllData("faculty");
        for (String i : dList) {
            System.out.println(i);
        }
        //query with whereClause
        System.out.println("query with whereClause");
        List<String> dList_1 = dbResponse.queryTableData("teacher", "id=1");
        for (String i : dList_1) {
            System.out.println(i);
        }

        //insert
        //Map<String, String> data_map = new HashMap<>();
        //data_map.put("id", "4");
        //data_map.put("name", "cc");
        //data_map.put("funding", "345");
        //PrintWriter p = new PrintWriter(System.out);
        //dbResponse.insertIntoTable("faculty", data_map, p);

        //get primary key
        List<String> pri_list = dbResponse.getPrimaryKey("study_relation");
        for (String i : pri_list) {
            System.out.println(i);
        }

    }

    @Test
    public void DBResponseDeleteTest() throws SQLException, FileNotFoundException {
        DBResponse dbResponse = new DBResponse();
        //delete
        Map<String, String> data_map = new HashMap<>();
        data_map.put("id", "4");
        PrintWriter p = new PrintWriter(System.out);
        dbResponse.deleteFromTable("faculty", data_map, p);
    }

    @Test
    public void DBResponseUpdateTest() throws SQLException, FileNotFoundException {
        DBResponse dbResponse = new DBResponse();
        //update
        Map<String, String> old_data_map = new HashMap<>();
        Map<String, String> new_data_map = new HashMap<>();
        old_data_map.put("id", "3");
        new_data_map.put("name", "newname");
        PrintWriter p = new PrintWriter(System.out);
        dbResponse.updateFromTable("faculty", old_data_map, new_data_map, p);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        System.out.println(timestamp.toString());
    }
}
