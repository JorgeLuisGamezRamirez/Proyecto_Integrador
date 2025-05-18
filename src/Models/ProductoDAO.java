package Models;

import basedatos.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {    public List<Producto> getProductos(Integer idCategoriaFiltro, String idFiltro, String nombreFiltro) {
        List<Producto> productos = new ArrayList<>();        
        String sql = "SELECT p.*, c.nombre as nombre_categoria " +
            "FROM Productos p " +
            "INNER JOIN Categorias c ON p.id_categoria = c.id_categoria " +
            "WHERE p.estado = 'Activo'";
            
        if (idCategoriaFiltro != null && idCategoriaFiltro != 0) {
            sql += " AND EXISTS (SELECT 1 FROM Categorias c2 WHERE c2.nombre = (SELECT nombre FROM Categorias WHERE id_categoria = ?) AND c2.id_categoria = p.id_categoria)";
        }
        if (idFiltro != null && !idFiltro.trim().isEmpty()) {
            sql += " AND p.id_producto LIKE ?";
        }
        if (nombreFiltro != null && !nombreFiltro.trim().isEmpty()) {
            sql += " AND p.nombre LIKE ?";
        }
        
        sql += " ORDER BY p.nombre";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            int parameterIndex = 1;
            
            if (idCategoriaFiltro != null) {
                pstmt.setInt(parameterIndex++, idCategoriaFiltro);
            }
            if (idFiltro != null && !idFiltro.trim().isEmpty()) {
                pstmt.setString(parameterIndex++, "%" + idFiltro + "%");
            }
            if (nombreFiltro != null && !nombreFiltro.trim().isEmpty()) {
                pstmt.setString(parameterIndex++, "%" + nombreFiltro + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getString("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_unitario"),
                    rs.getInt("existencias"),
                    rs.getInt("stock_minimo"),
                    rs.getInt("id_categoria"),
                    rs.getString("estado"),
                    rs.getString("fecha_creacion"),
                    rs.getString("nombre_categoria")
                );
                productos.add(producto);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return productos;
    }    public List<String[]> getCategorias() {
        List<String[]> categorias = new ArrayList<>();
        // Primero obtener categorías únicas
        String sql = "SELECT MIN(c.id_categoria) as id_categoria, c.nombre " +
                    "FROM Categorias c " +
                    "GROUP BY c.nombre " +
                    "ORDER BY c.nombre";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            ResultSet rs = pstmt.executeQuery();
            
            // Agregar opción "Todas las categorías"
            categorias.add(new String[]{"0", "Todas las categorías"});
            
            while (rs.next()) {
                String[] categoria = {
                    String.valueOf(rs.getInt("id_categoria")),
                    rs.getString("nombre")
                };
                categorias.add(categoria);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categorias;
    }

    public boolean actualizarStock(String idProducto, int cantidadVendida) {
        String sql = "UPDATE Productos SET existencias = existencias - ? WHERE id_producto = ? AND existencias >= ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cantidadVendida);
            pstmt.setString(2, idProducto);
            pstmt.setInt(3, cantidadVendida);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Producto> getProductosBajoStock() {
        List<Producto> productos = new ArrayList<>();        String sql = "SELECT p.*, c.nombre as nombre_categoria " +
            "FROM Productos p " +
            "LEFT JOIN Categorias c ON p.id_categoria = c.id_categoria " +
            "WHERE p.estado = 'Activo' AND p.existencias <= p.stock_minimo " +
            "ORDER BY p.existencias ASC";
            
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getString("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_unitario"),
                    rs.getInt("existencias"),
                    rs.getInt("stock_minimo"),
                    rs.getInt("id_categoria"),
                    rs.getString("estado"),
                    rs.getString("fecha_creacion"),
                    rs.getString("nombre_categoria")
                );
                productos.add(producto);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con bajo stock: " + e.getMessage());
            e.printStackTrace();
        }
        
        return productos;
    }

    public boolean save(Producto producto) {
        String sql = "INSERT INTO Productos (id_producto, nombre, descripcion, precio_unitario, " +
            "existencias, stock_minimo, id_categoria, estado, fecha_creacion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getIdProducto());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setDouble(4, producto.getPrecioUnitario());
            pstmt.setInt(5, producto.getExistencias());
            pstmt.setInt(6, producto.getStockMinimo());
            pstmt.setInt(7, producto.getIdCategoria());
            pstmt.setString(8, "Activo");
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }    public String generateNextProductId(int idCategoria) {
        String prefix = getCategoryPrefix(idCategoria);
        int nextNumber = 1;
          String sql = "SELECT TOP 1 id_producto " +
            "FROM Productos " +
            "WHERE id_producto LIKE ? " +
            "ORDER BY id_producto DESC";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String lastId = rs.getString("id_producto");
                // Extraer el número después del guión o los últimos 3 dígitos
                String numStr;
                if (lastId.contains("-")) {
                    numStr = lastId.substring(lastId.indexOf("-") + 1);
                } else {
                    numStr = lastId.substring(lastId.length() - 3);
                }
                nextNumber = Integer.parseInt(numStr) + 2; // Incrementar de 2 en 2 como se ve en el ejemplo
            }
            
        } catch (SQLException e) {
            System.err.println("Error al generar siguiente ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Formato: PN001 para Panadería, XX-001 para otras categorías
        if (prefix.equals("PN")) {
            return String.format("%s%03d", prefix, nextNumber);
        } else {
            return String.format("%s%03d", prefix, nextNumber);
        }
    }    private String getCategoryPrefix(int idCategoria) {
        String sql = "SELECT nombre FROM Categorias WHERE id_categoria = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idCategoria);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                if (nombre != null && nombre.length() > 0) {
                    // Para la categoría Panaderia, usar PN
                    if (nombre.equalsIgnoreCase("Panaderia") || nombre.equalsIgnoreCase("Panadería")) {
                        return "PN";
                    }
                    // Para otras categorías, usar las primeras dos letras en mayúsculas
                    String prefix = nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();
                    return prefix + "-";
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener prefijo de categoría: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "XX-"; // Prefijo por defecto en caso de error
    }

    public boolean eliminarProducto(String idProducto) {
        // En lugar de eliminar físicamente, cambiamos el estado a 'Inactivo'
        String sql = "UPDATE Productos SET estado = 'Inactivo' WHERE id_producto = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, idProducto);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al dar de baja el producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarProducto(Producto producto) {        String sql = "UPDATE Productos " +
            "SET nombre = ?, " +
            "descripcion = ?, " +
            "precio_unitario = ?, " +
            "stock_minimo = ?, " +
            "id_categoria = ?, " +
            "existencias = ? " +
            "WHERE id_producto = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioUnitario());
            pstmt.setInt(4, producto.getStockMinimo());
            pstmt.setInt(5, producto.getIdCategoria());
            pstmt.setInt(6, producto.getExistencias());
            pstmt.setString(7, producto.getIdProducto());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
