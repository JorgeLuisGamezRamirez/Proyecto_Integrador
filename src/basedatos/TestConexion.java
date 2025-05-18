package basedatos;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestConexion {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            if (conn != null) {
                System.out.println("La conexión a la base de datos fue exitosa!");
                
                // Probar la conexión con una consulta simple
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 1");
                    if (rs.next()) {
                        System.out.println("La prueba de consulta fue exitosa!");
                    }
                }
            } else {
                System.out.println("No se pudo establecer la conexión a la base de datos.");
            }
        } catch (SQLException e) {
            System.err.println("Error durante la prueba de conexión: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                Conexion.closeConnection(conn);
            }
        }
    }
}
