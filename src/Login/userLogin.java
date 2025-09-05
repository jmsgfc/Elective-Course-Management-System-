package Login;
import DButil.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class userLogin {

    static int Login (String name , String password){
        try(Connection conn = DButil.connection();){
            String sql = "select * from users where uname = ? and pwd = ?";

            //对象创建
            PreparedStatement pstmt = conn.prepareStatement(sql);//执行对象
            pstmt.setString(1,name);
            pstmt.setString(2,password);

            //结果对象
            ResultSet rs = pstmt.executeQuery();//询问
            if(rs.next()){ //检查每一行
                String pwd = rs.getString("pwd");
                String uname = rs.getString("uname");
                int role_type = rs.getInt("role");
                if(name.equals(uname) && password.equals(pwd)){
                switch(role_type){
                    case 0 : return 1;//管理员
                    case 1 : return 2;//教师
                    case 2 : return 3;//学生
                }

                    //JOptionPane.showMessageDialog(null, "登录成功！", "提示", JOptionPane.ERROR_MESSAGE);

                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }



}
