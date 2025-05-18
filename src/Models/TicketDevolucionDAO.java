package Models;

import basedatos.Conexion;
import java.sql.*;
import javax.swing.JOptionPane;

public class TicketDevolucionDAO {    public String generarNumeroTicketDevolucion() {
        // Primero verificamos si existe el campo numero_ticket
        try (Connection conn = Conexion.getConnection()) {
            // Verificar si la columna existe
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, "Devoluciones", "numero_ticket");
            
            if (!rs.next()) {
                // La columna no existe, la creamos
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE Devoluciones ADD numero_ticket VARCHAR(20)");
                }
            }            // Ahora generamos el número de ticket
            String sql = "SELECT ISNULL(MAX(" +
                "CASE " +
                "WHEN numero_ticket LIKE 'DEV%' " +
                "THEN CAST(SUBSTRING(numero_ticket, 4, LEN(numero_ticket)) AS INT) " +
                "ELSE 0 " +
                "END" +
                "), 0) + 1 AS siguiente " +
                "FROM Devoluciones";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet resultSet = stmt.executeQuery(sql)) {
                
                if (resultSet.next()) {
                    int siguiente = resultSet.getInt("siguiente");
                    return String.format("DEV%06d", siguiente);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error al generar número de ticket: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public TicketDevolucion obtenerTicketDevolucion(int idDevolucion) {        String sql = "SELECT d.*, v.numero_ticket as ticket_original, " +
            "CONCAT(e.nombre, ' ', e.apellido) as nombre_empleado, " +
            "ISNULL(c.nombre + ' ' + c.apellido, 'Cliente General') as nombre_cliente " +
            "FROM Devoluciones d " +
            "JOIN Ventas v ON d.id_venta = v.id_venta " +
            "LEFT JOIN Empleados e ON v.id_empleado = e.id_empleado " +
            "LEFT JOIN Clientes c ON v.id_cliente = c.id_cliente " +
            "WHERE d.id_devolucion = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idDevolucion);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                TicketDevolucion ticket = new TicketDevolucion(
                    rs.getInt("id_devolucion"),
                    rs.getString("ticket_original"),
                    rs.getString("numero_ticket"),
                    rs.getTimestamp("fecha"),
                    rs.getDouble("total_devolucion"),
                    rs.getString("motivo"),
                    rs.getString("estado")
                );
                
                ticket.setNombreEmpleado(rs.getString("nombre_empleado"));
                ticket.setNombreCliente(rs.getString("nombre_cliente"));
                  // Cargar los detalles
                String sqlDetalles = "SELECT d.*, p.nombre as nombre_producto " +
                    "FROM DetallesDevolucion d " +
                    "JOIN Productos p ON d.id_producto = p.id_producto " +
                    "WHERE d.id_devolucion = ?";
                
                try (PreparedStatement stmtDetalles = conn.prepareStatement(sqlDetalles)) {
                    stmtDetalles.setInt(1, idDevolucion);
                    ResultSet rsDetalles = stmtDetalles.executeQuery();
                    
                    while (rsDetalles.next()) {
                        DetalleDevolucion detalle = new DetalleDevolucion(
                            rsDetalles.getInt("id_detalle"),
                            rsDetalles.getInt("id_devolucion"),
                            rsDetalles.getString("id_producto"),
                            rsDetalles.getInt("cantidad"),
                            rsDetalles.getDouble("precio_unitario"),
                            rsDetalles.getString("nombre_producto")
                        );
                        ticket.agregarDetalle(detalle);
                    }
                }
                
                return ticket;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
