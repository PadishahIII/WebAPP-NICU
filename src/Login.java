import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.PreparedStatement;

import MyProtocol.MyProtocol;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Login extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/education_admins";
    static final String USER = "root";
    static final String PASS = "914075";
    private Connection conn = null;
    private Statement stmt = null;
    private MyProtocol mp;

    public Login() throws Exception {
        init_mysql();
        mp = new MyProtocol();
        mp.loadPrivateKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\pkcs8_private_der.key");
        mp.loadPublicKeyFromFile("D:\\Tomcat\\webapps\\DBManager\\src\\rsa_public_key.der");
    }

    protected void init_mysql() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServerException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>bbbbb</title></head>");
        out.println("</html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            //System.out.println("payload:" + request.getParameter("payload"));
            //System.out.println("key:" + request.getParameter("key"));
            //System.out.println("iv:" + request.getParameter("iv"));
            //System.out.println("mac:" + request.getParameter("mac"));
            String payload_json = mp.decode(request.getParameter("payload"), request.getParameter("key"),
                    request.getParameter("iv"), request.getParameter("mac"));
            //System.out.println(payload_json);
            String reply_json = mp.reply(payload_json);
            //System.out.println("reply:" + reply);
            JSONObject payload = JSONObject.parseObject(payload_json);
            String username = payload.getString("username");
            String password = payload.getString("password");
            byte[] password_mac = md.digest(password.getBytes());
            String password_mac_str = bytes2HexString(password_mac);
            //查询数据库
            try {
                String sql = "SELECT password from admininfo where username='" + username + "';";
                //java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                //pstmt.setString(1, username);
                ResultSet res = stmt.executeQuery(sql);
                boolean Success = false;
                while (res.next()) {
                    //int id = res.getInt(("id"));
                    String password_db = res.getString("password");
                    if (password_mac_str.equals(password_db)) {
                        //成功
                        //out.println("Login Success!\nWelcome " + username + "!");
                        out.println(reply_json);
                        Success = true;
                        break;
                    }
                    //byte[] password_src = hexString2Bytes(password);
                    //assertArrayEquals(password_src, data);
                }
                if (!Success) {
                    out.println("Login Failed");
                    System.out.println("username:" + username);
                    System.out.println("sql:" + sql);
                    System.out.println("password:" + password);
                    System.out.println("password_mac:" + password_mac_str);
                }
                res.close();
            } catch (SQLException se) {
                se.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //out.println(reply);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String str = "123456";
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] data = md.digest(str.getBytes());
        String hex = bytes2HexString(data);
        System.out.println((hex));
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String username = "admin";
            String sql = "SELECT password from admininfo where username='" + username + "';";
            //java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            //pstmt.setString(1, username);
            ResultSet res = stmt.executeQuery(sql);
            //System.out.println(pstmt.toString());
            while (res.next()) {
                //int id = res.getInt(("id"));
                String password = res.getString("password");
                byte[] password_src = hexString2Bytes(password);
                //assertArrayEquals(password_src, data);
            }
            res.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
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
            result.append(String.format("%02X", b[i]));
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

}
