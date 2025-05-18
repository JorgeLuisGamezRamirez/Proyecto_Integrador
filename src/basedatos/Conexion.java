package basedatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=PuntoVenta;integratedSecurity=true;encrypt=true;trustServerCertificate=true";
    
    static {
        try {
            // Registrar el driver de SQL Server al inicio
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de SQL Server: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar el driver de SQL Server", e);
        }
    }
    
    public static Connection getConnection() {
        try {
            // Crear una nueva conexión cada vez
            Connection conn = DriverManager.getConnection(URL);
            if (conn != null) {
                System.out.println("Nueva conexión establecida con la base de datos");
                return conn;
            }
            throw new SQLException("No se pudo establecer la conexión");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("Conexión cerrada exitosamente");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
