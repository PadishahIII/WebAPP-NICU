
import MyProtocol.MyProtocol;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class app extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServerException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>bbbbb</title></head>");
        out.println("<body>");
        out.println(request.getRequestURI());//  /DBxx/app

        out.println(request.getRequestURL());//  http://xx
        out.println("</body>");
        out.println("</html>");
    }
}
