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

            String[] uStrings = request.getRequestURI().split("/");
            String op = uStrings[uStrings.length - 1];

            MessageDigest md = MessageDigest.getInstance("SHA-512");
            //System.out.println("payload:" + request.getParameter("payload"));
            //System.out.println("key:" + request.getParameter("key"));
            //System.out.println("iv:" + request.getParameter("iv"));
            //System.out.println("mac:" + request.getParameter("mac"));
            if (op.equals("register")) {
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

            } else if (op.equals("validate")) {
                JSONObject payload = JSONObject.parseObject(request.getParameter("payload"));
                String pn = payload.getString("pn");

            } else {
                out.println("404");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean sendMsg(String pn) {

        return true;
    }

    private void writeLog(String data) {
        Timestamp tm = new Timestamp(new Date().getTime());
        log.println("[" + tm.toString() + "]:" + data);
    }

    /**
    * 使用AK&SK初始化账号Client
    * @param accessKeyId
    * @param accessKeySecret
    * @return Client
    * @throws Exception
    */
    //public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret)
    //        throws Exception {
    //    Config config = new Config()
    //            // 您的 AccessKey ID
    //            .setAccessKeyId(accessKeyId)
    //            // 您的 AccessKey Secret
    //            .setAccessKeySecret(accessKeySecret);
    //    // 访问的域名
    //    config.endpoint = "dysmsapi.aliyuncs.com";
    //    return new com.aliyun.dysmsapi20170525.Client(config);
    //}

    public static void main(String[] args_) throws Exception {
        //java.util.List<String> args = java.util.Arrays.asList(args_);
        //com.aliyun.dysmsapi20170525.Client client = register.createClient("100585899",
        //        "7237b0e2c6136531872d994cbabff26f");
        //SendSmsRequest sendSmsRequest = new SendSmsRequest()
        //        .setSignName("阿里云短信测试")
        //        .setTemplateCode("SMS_154950909")
        //        .setPhoneNumbers("13273487268")
        //        .setTemplateParam("{\"code\":\"1234\"}");
        //RuntimeOptions runtime = new RuntimeOptions();
        //try {
        //    // 复制代码运行请自行打印 API 的返回值
        //    client.sendSmsWithOptions(sendSmsRequest, runtime);
        //} catch (TeaException error) {
        //    // 如有需要，请打印 error
        //    com.aliyun.teautil.Common.assertAsString(error.message);
        //} catch (Exception _error) {
        //    TeaException error = new TeaException(_error.getMessage(), _error);
        //    // 如有需要，请打印 error
        //    com.aliyun.teautil.Common.assertAsString(error.message);
        //}
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
