package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnFactory {

 private static final Properties properties = new Properties();

 static {
  try (InputStream input = ConnFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
   if (input == null) {
    throw new RuntimeException("Fille not found");
   }
   properties.load(input);
  } catch (IOException e) {
   throw new RuntimeException("Failed to load database properties", e);
  }
 }

 public static Connection getConnection() {
  try {
   String url = properties.getProperty("durl");
   String username = properties.getProperty("user");
   String password = properties.getProperty("password");

   return DriverManager.getConnection(url, username, password);

  } catch (SQLException e) {
   throw new RuntimeException("Failed to establish database connection", e);
  }
 }
}
