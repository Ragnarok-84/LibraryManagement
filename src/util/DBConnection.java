package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library_management";
    private static final String USER = "root";       // thay bằng tài khoản MySQL của bạn
    private static final String PASSWORD = "kien1992005t1chy"; // thay bằng mật khẩu MySQL của bạn

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println(" Kết nối MySQL thành công!");
            } catch (SQLException e) {
                System.err.println(" Lỗi kết nối MySQL: " + e.getMessage());
            }
        }
        return connection;
    }
}
