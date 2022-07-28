import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.sql.Connection;
import java.util.Date;
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

public class register extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/nicu";
    static final String USER = "root";
    static final String PASS = "914075";
    private Connection conn = null;
    private Statement stmt = null;
    static final String LogPath = "D:\\Tomcat\\webapps\\NICU\\log\\sqlLog.txt";
    private PrintStream log;

    public register() throws Exception {
        init_mysql();
        File logfile = new File(LogPath);
        log = new PrintStream(new FileOutputStream(logfile, true));
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
        out.println("GET");
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
            String payload_json = request.getParameter("payload");
            //System.out.println(payload_json);
            JSONObject payload = JSONObject.parseObject(payload_json);
            String username = payload.getString("username");
            String password = payload.getString("password");
            String phonenumber = payload.getString("phonenumber");

            byte[] password_mac = md.digest(password.getBytes());
            String password_mac_str = bytes2HexString(password_mac);
            //查询数据库
            try {
                String sql = "SELECT password from users where username='" + username + "' or phonenumber='"
                        + phonenumber + "';";
                String sql_insert = "insert into users values('" + username + "','" + password_mac_str + "','"
                        + phonenumber + "');";
                //java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                //pstmt.setString(1, username);
                ResultSet res = stmt.executeQuery(sql);
                writeLog(sql);
                System.out.println(sql);

                boolean Success = false;
                while (res.next()) {
                    //int id = res.getInt(("id"));
                    out.println("Register Failed: Username or PhoneNumber already exists!");
                    return;
                }
                int res_insert = stmt.executeUpdate(sql_insert);
                writeLog(sql_insert);
                System.out.println(sql_insert);
                Success = true;
                if (!Success) {
                    out.println("Register Failed");
                    System.out.println("username:" + username);
                    System.out.println("sql:" + sql);
                    System.out.println("password:" + password);
                    System.out.println("password_mac:" + password_mac_str);
                    return;
                }
                out.println("success");
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

    private void writeLog(String data) {
        Timestamp tm = new Timestamp(new Date().getTime());
        log.println("[" + tm.toString() + "]:" + data);
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
