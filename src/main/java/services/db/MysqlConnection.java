package services.db; // Dòng này cực kỳ quan trọng, nó báo cho Java biết vị trí chính xác của file

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Đọc config 1 lần khi class được load
    static {
        try (InputStream input = MysqlConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            if (input == null) {
                throw new IOException("Không tìm thấy file config.properties trong resources!");
            }
            props.load(input);
            URL      = props.getProperty("db.url");
            USER     = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Lỗi đọc config.properties: " + e.getMessage());
            // Fallback để không crash toàn bộ app khi thiếu file
            URL      = "jdbc:mysql://localhost:3306/bluemoon";
            USER     = "root";
            PASSWORD = "";
        }
    }

    private MysqlConnection() {}

    /**
     * Trả về một Connection MỚI mỗi lần gọi.
     * Các DAO dùng try-with-resources sẽ tự đóng connection sau khi dùng xong.
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Lỗi kết nối Database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}