package services.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection {
    private static Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/bluemoon?useUnicode=true&characterEncoding=utf-8";
    private static final String USER = "root";
    private static final String PASSWORD = "1127";

    private MysqlConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Lỗi cấu hình kết nối Database!");
            e.printStackTrace();
        }
        return connection;
    }
}