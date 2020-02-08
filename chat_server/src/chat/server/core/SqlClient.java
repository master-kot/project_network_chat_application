package chat.server.core;

import java.sql.*;

/**
 * Класс для работы с базой данных
 */
public class SqlClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            //Загружаем класс драйвера базы данных
            Class.forName("org.sqlite.JDBC");
            //устанавливаем подключение к файлу базы данных chat_server/chatDb.sqlite
            connection = DriverManager.getConnection("jdbc:sqlite:chat_server/chatDb.sqlite");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            //TODO
            //Пробросить исключение выше, в графическую оболочку
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            //TODO
            //Пробросить исключение выше, в графическую оболочку
            e.printStackTrace();
        }
    }

    synchronized static String getNickname(String login, String password) {
        String request = String.format("select nickname from users where login='%s' and password='%s'",
                login, password);
        try (ResultSet set = statement.executeQuery(request)) {
            //если результат запроса не пуст, забираем первое значение
            if (set.next()) {
                return set.getString(1);
            }
        } catch (SQLException e) {
            //TODO
            //Пробросить исключение выше, в графическую оболочку
            throw new RuntimeException(e);
        }
        return null;
    }
}