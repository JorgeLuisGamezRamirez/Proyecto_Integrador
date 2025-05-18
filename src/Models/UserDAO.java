package Models;

import basedatos.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public boolean save(User user) {
        String sql = "INSERT INTO dbo.Empleados (nombre, apellido, usuario, contraseña, rol, telefono, direccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getNombre());
            pstmt.setString(2, user.getApellido());
            pstmt.setString(3, user.getUsuario());
            pstmt.setString(4, user.getContraseña());
            pstmt.setString(5, user.getRol()); // Usando el rol seleccionado
            pstmt.setString(6, user.getTelefono());
            pstmt.setString(7, user.getDireccion());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar empleado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }    public User findByUsuario(String usuario) {
        String sql = "SELECT * FROM dbo.Empleados WHERE usuario = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id_empleado"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("rol"),
                    rs.getString("usuario"),
                    rs.getString("contraseña")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar empleado: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }    public boolean updatePassword(String usuario, String contraseña) {
        String sql = "UPDATE dbo.Empleados SET contraseña = ? WHERE usuario = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, contraseña);
            pstmt.setString(2, usuario);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM dbo.Empleados";
        
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id_empleado"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("rol"),
                    rs.getString("usuario"),
                    rs.getString("contraseña")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM dbo.Empleados WHERE id_empleado = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id_empleado"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("rol"),
                    rs.getString("usuario"),
                    rs.getString("contraseña")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM dbo.Empleados WHERE id_empleado = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE dbo.Empleados SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, rol = ?, usuario = ?, contraseña = ? WHERE id_empleado = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getNombre());
            pstmt.setString(2, user.getApellido());
            pstmt.setString(3, user.getTelefono());
            pstmt.setString(4, user.getDireccion());
            pstmt.setString(5, user.getRol());
            pstmt.setString(6, user.getUsuario());
            pstmt.setString(7, user.getContraseña());
            pstmt.setInt(8, user.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
