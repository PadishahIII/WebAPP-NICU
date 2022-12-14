import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DBResponse extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/education";
    static final String USER = "root";
    static final String PASS = "914075";
    static final String LogPath = "D:\\Tomcat\\webapps\\DBManager\\log\\sqlLog.txt";
    private Connection conn = null;
    private Statement stmt = null;
    //private MyProtocol mp;
    private PrintStream log;

    public DBResponse() throws FileNotFoundException {
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

        String[] uStrings = request.getRequestURI().split("/");
        String op = uStrings[uStrings.length - 1];
        if (op.equals("getTables")) {
            //???????????????????????????
            /**
            * ???????????????
            * {
            *  "num":??????
            *  "tblnames":"tbl1;tbl2;..."
            *  "colnames":"col1_1,col1_2..;col2_1,col2_2..."
            * }
            */
            try {
                JSONObject res = new JSONObject();
                StringBuffer tblnames = new StringBuffer();
                StringBuffer colnames = new StringBuffer();
                List<String> tblnameList = getTableNames();
                int num = tblnameList.size();
                for (String str : tblnameList) {
                    tblnames.append(str);
                    tblnames.append(";");
                }
                tblnames.deleteCharAt(tblnames.length() - 1);

                for (String str : tblnameList) {
                    if (!str.isEmpty()) {
                        List<String> colnameList = getColumnNames(str);
                        for (String colname : colnameList) {
                            colnames.append(colname);
                            colnames.append(",");
                        }
                        colnames.deleteCharAt(colnames.length() - 1);
                        colnames.append(";");
                    }
                }
                res.put("num", num);
                res.put("tblnames", tblnames.toString());
                res.put("colnames", colnames.toString());

                out.println(res.toJSONString());

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } //if url
        else if (op.equals("queryAllData")) {
            /**
            * ??????????????????????????????
            * ???????????????
            * {
            *  "num":9   //????????????
            *  "0":"aa;bb;cc;"
            *  "1":"dd;ee;ff;"
            *  ...
            * }
            */
            try {
                String tblname = request.getParameter("tblname");
                System.out.println("query: " + tblname);
                JSONObject res = new JSONObject();

                List<String> dataList = queryAllData(tblname);
                res.put("num", dataList.size());
                Integer index = 0;
                for (String i : dataList) {
                    res.put(index.toString(), i);
                    index++;
                }
                out.println(res.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String[] uStrings = request.getRequestURI().split("/");
        String op = uStrings[uStrings.length - 1];
        if (op.equals("insert")) {
            //??????????????????????????????
            try {
                String tblname = request.getParameter("tblname");
                Map<String, String> data_map = new HashMap<>();
                Map<String, String[]> temp_map = request.getParameterMap();
                for (String key : temp_map.keySet()) {
                    data_map.put(key, temp_map.get(key)[0]);
                }

                if (!insertIntoTable(tblname, data_map, out)) {
                    out.println("?????????????????????????????????");
                    return;
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                out.println("?????????????????????????????????");
            } //try
        } //if url
        else if (op.equals("delete")) {
            try {
                String tblname = request.getParameter("tblname");
                Map<String, String> data_map = new HashMap<>();
                Map<String, String[]> temp_map = request.getParameterMap();
                for (String key : temp_map.keySet()) {
                    data_map.put(key, temp_map.get(key)[0]);
                }

                if (!deleteFromTable(tblname, data_map, out)) {
                    out.println("???????????????");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("?????????????????????????????????");
            } //try
        } //if url
        else if (op.equals("update")) {
            try {
                String tblname = request.getParameter("tblname");
                String data_map_json = request.getParameter("data_map_json");
                JSONObject data_map = JSONObject.parseObject(data_map_json);
                String old_data_map_json = data_map.getString("old");
                String new_data_map_json = data_map.getString("new");
                JSONObject old_data_map_obj = JSONObject.parseObject(old_data_map_json);
                JSONObject new_data_map_obj = JSONObject.parseObject(new_data_map_json);

                Map<String, String> old_data_map = new HashMap<>();
                Map<String, String> new_data_map = new HashMap<>();
                for (String k : old_data_map_obj.keySet()) {
                    old_data_map.put(k, old_data_map_obj.getString(k));
                }
                for (String k : new_data_map_obj.keySet()) {
                    new_data_map.put(k, new_data_map_obj.getString(k));
                }
                if (!updateFromTable(tblname, old_data_map, new_data_map, out)) {
                    out.println("???????????????");
                    return;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                out.println("?????????????????????????????????");
            }
        } else if (op.equals("queryTableData")) {
            try {
                String tblname = request.getParameter("tblname");
                String whereClause = request.getParameter("whereClause");
                System.out.println("query: " + tblname + " where " + whereClause);
                JSONObject res = new JSONObject();

                List<String> dataList = queryTableData(tblname, whereClause);
                res.put("num", dataList.size());
                Integer index = 0;
                for (String i : dataList) {
                    res.put(index.toString(), i);
                    index++;
                }
                out.println(res.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ????????????????????????
     * @return
     * @throws SQLException
     */
    public List<String> getTableNames() throws SQLException {
        List<String> tblnameList = new ArrayList<>();
        String sql = "show tables;";
        ResultSet sql_res = stmt.executeQuery(sql);
        while (sql_res.next()) {
            String tbl_name = sql_res.getString(1);
            if (!tbl_name.isEmpty())
                tblnameList.add(tbl_name);
        }
        writeLog(sql);
        return tblnameList;
    }

    /**
     * ?????????????????????????????????
     * @param tblname
     * @return
     * @throws SQLException
     */
    public List<String> getColumnNames(String tblname) throws SQLException {
        List<String> colnameList = new ArrayList<>();
        String sql = "select column_name from information_schema.columns where table_schema='education' and table_name=?";
        java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setObject(1, tblname);
        ResultSet col_sql_res = pstmt.executeQuery();
        while (col_sql_res.next()) {
            String col_name = col_sql_res.getString(1);
            if (!col_name.isEmpty()) {
                colnameList.add(col_name);
            }
        }
        writeLog(pstmt.toString());
        return colnameList;
    }

    /**
     * ??????????????????????????????
     * @param tblname
     * @return
     * @throws SQLException
     */
    public List<String> queryAllData(String tblname) throws SQLException {
        List<String> dataList = new ArrayList<>();
        int colnum = getColumnNames(tblname).size();

        String sql = "select * from " + tblname;
        java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
        //pstmt.setString(1, tblname);
        ResultSet sql_res = pstmt.executeQuery(sql);

        StringBuffer line = new StringBuffer();
        while (sql_res.next()) {
            for (int j = 1; j <= colnum; j++) {
                String coldata = sql_res.getString(j);
                line.append(coldata);
                line.append(";");
            }
            line.deleteCharAt(line.length() - 1);
            dataList.add(line.toString());
            line = new StringBuffer();
        }
        writeLog(sql);
        return dataList;
    }

    /**
     * ?????????????????????????????????
     * @param tblname ???????????????????????????????????????
     * @param whereClause where??????  ????????????????????????
     * @return
     * @throws SQLException
     */
    public List<String> queryTableData(String tblname, String whereClause) throws SQLException {
        List<String> dataList = new ArrayList<>();
        int colnum;
        String projection = "*";//???????????????
        if (!tblname.contains(",")) {
            colnum = getColumnNames(tblname).size();
        } else {//????????????
            String real_tblname = tblname.split(",")[0];
            colnum = getColumnNames(real_tblname).size();
            switch (real_tblname) {
                case "teacher":
                    projection = "teacher.id,teacher.name,teacher.age,salary,direction.name,project.name,laboratory.name,faculty.name ";
                    break;
                case "student":
                    projection = "student.id,student.name,student.sex,student.age,student.grade,faculty.name";
                    break;
                case "course":
                    projection = "course.id,course.name,course.credit,classroom.building";
                    break;
                case "teach_relation":
                    projection = "course.name,teacher.name,teach_relation.credit_hour";
                    break;
                case "study_relation":
                    projection = "course.name,student.name,study_relation.grade";
                    break;
                default:
                    break;
            }
        }

        /**
         *  select teacher.id,teacher.name,teacher.age,salary,direction.name,project.name,laboratory.name,faculty.name 
         *          from teacher,direction,project,laboratory,faculty 
         *          where teacher.dno=direction.id and teacher.pno=project.id and teacher.lno=laboratory.id and teacher.fno=faculty.id;
         */
        String sql = "select " + projection + " from " + tblname + " where " + whereClause;
        writeLog(sql);
        java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
        //pstmt.setString(1, tblname);
        ResultSet sql_res = pstmt.executeQuery(sql);

        StringBuffer line = new StringBuffer();
        while (sql_res.next()) {
            for (int j = 1; j <= colnum; j++) {
                String coldata = sql_res.getString(j);
                line.append(coldata);
                line.append(";");
            }
            line.deleteCharAt(line.length() - 1);
            dataList.add(line.toString());
            line = new StringBuffer();
        }
        return dataList;
    }

    /**
     * ??????????????????
     * @param tblname
     * @param data_map
     * @param out
     * @return
     * @throws SQLException
     */
    public boolean insertIntoTable(String tblname, Map<String, String> data_map, PrintWriter out) throws SQLException {
        if (!getTableNames().contains(tblname)) {
            out.println("????????????????????????????????????????????????:" + tblname);
            return false;
        }
        List<String> colList = getColumnNames(tblname);
        StringBuffer col_strs = new StringBuffer();
        StringBuffer val_strs = new StringBuffer();
        col_strs.append("(");
        val_strs.append("('");
        for (String colname : colList) {
            String value = data_map.get(colname);
            if (value == null || value.isEmpty()) {
                out.println("????????????????????????:" + colname);
                return false;
            }
            col_strs.append(colname + ",");
            val_strs.append(value + "','");
        }
        col_strs.deleteCharAt(col_strs.length() - 1);
        val_strs.deleteCharAt(val_strs.length() - 1);
        val_strs.deleteCharAt(val_strs.length() - 1);

        col_strs.append(")");
        val_strs.append(")");

        String sql = "insert into " + tblname + " " + col_strs + " values " + val_strs;
        int lineno = stmt.executeUpdate(sql);
        if (lineno != 1) {
            out.println("?????????????????????????????????");
            return false;
        }
        out.println("????????????!");

        writeLog(sql);
        return true;
    }

    /**
     * ??????????????????
     * @param tblname
     * @param data_map
     * @param out
     * @return
     * @throws SQLException
     */
    public boolean deleteFromTable(String tblname, Map<String, String> data_map, PrintWriter out) throws SQLException {
        if (!getTableNames().contains(tblname)) {
            out.println("????????????????????????????????????????????????:" + tblname);
            return false;
        }
        StringBuffer str = new StringBuffer();
        //????????????
        List<String> pri_list = getPrimaryKey(tblname);

        for (String colname : pri_list) {
            String value = data_map.get(colname);
            if (value == null || value.isEmpty()) {
                out.println("????????????????????????:" + colname);
                return false;
            }
            str.append(colname + "='" + value + "' and ");
        }
        String sql_str = str.substring(0, str.length() - 5);

        String sql = "delete from " + tblname + " where " + sql_str;
        int lineno = stmt.executeUpdate(sql);
        if (lineno < 1) {
            out.println("???????????????????????????");
            return false;
        }
        out.println("????????????!");

        writeLog(sql);
        return true;
    }

    /**
     * ??????????????????
     * @param tblname
     * @param old_data_map ??????
     * @param new_data_map ??????
     * @param out
     * @return
     * @throws SQLException
     */
    public boolean updateFromTable(String tblname, Map<String, String> old_data_map, Map<String, String> new_data_map,
            PrintWriter out) throws SQLException {
        if (!getTableNames().contains(tblname)) {
            out.println("????????????????????????????????????????????????:" + tblname);
            return false;
        }
        StringBuffer where_str = new StringBuffer();
        StringBuffer set_str = new StringBuffer();

        List<String> pri_list = getPrimaryKey(tblname);

        //??????where??????
        for (String colname : pri_list) {
            String value = old_data_map.get(colname);
            if (value == null || value.isEmpty()) {
                out.println("????????????????????????:" + colname);
                return false;
            }
            where_str.append(colname + "='" + value + "' and ");
        }
        String where_str_sql = where_str.substring(0, where_str.length() - 5);

        //??????set??????
        for (String colname : new_data_map.keySet()) {
            String value = new_data_map.get(colname);
            set_str.append(colname + "='" + value + "',");
        }
        String set_str_sql = set_str.substring(0, set_str.length() - 1);

        String sql = "update " + tblname + " set " + set_str_sql + " where " + where_str_sql;
        int res = stmt.executeUpdate(sql);
        if (res < 1) {
            out.println("???????????????????????????");
            return false;
        }
        writeLog(sql);
        out.println("????????????!");
        return true;
    }

    /**
     * ????????????????????????
     * @param tblname
     * @return
     * @throws SQLException
     */
    public List<String> getPrimaryKey(String tblname) throws SQLException {
        List<String> list = new ArrayList<>();

        String sql = " select column_name from information_schema.key_column_usage where table_schema='education' and constraint_name='primary' and table_name=?";
        java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setObject(1, tblname);
        ResultSet sql_res = pstmt.executeQuery();
        while (sql_res.next()) {
            list.add(sql_res.getString(1));
        }
        writeLog(pstmt.toString());
        return list;
    }

    private void writeLog(String data) {
        Timestamp tm = new Timestamp(new Date().getTime());
        log.println("[" + tm.toString() + "]:" + data);
    }
}
