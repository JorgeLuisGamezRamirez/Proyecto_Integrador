package Models;

import basedatos.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {    public Ticket getTicketByVenta(int idVenta) {
        String sql = "SELECT v.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
            "e.nombre as empleado_nombre, e.apellido as empleado_apellido " +
            "FROM Ventas v " +
            "LEFT JOIN Clientes c ON v.id_cliente = c.id_cliente " +
            "LEFT JOIN Empleados e ON v.id_empleado = e.id_empleado " +
            "WHERE v.id_venta = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Ticket ticket = new Ticket(
                    rs.getInt("id_venta"),
                    rs.getString("numero_ticket"),
                    rs.getTimestamp("fecha"),
                    rs.getDouble("subtotal"),
                    rs.getDouble("impuestos"),
                    rs.getDouble("total"),
                    rs.getString("metodo_pago"),
                    rs.getString("estado"),
                    rs.getInt("id_cliente"),
                    rs.getInt("id_empleado")
                );
                
                // Cargar los detalles del ticket
                ticket.setDetalles(getDetallesTicket(idVenta));
                return ticket;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener ticket: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    private List<Ticket.DetalleTicket> getDetallesTicket(int idVenta) {
        List<Ticket.DetalleTicket> detalles = new ArrayList<>();        String sql = "SELECT dv.*, p.nombre as nombre_producto " +
            "FROM DetallesVenta dv " +
            "JOIN Productos p ON dv.id_producto = p.id_producto " +
            "WHERE dv.id_venta = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Ticket.DetalleTicket detalle = new Ticket.DetalleTicket(
                    rs.getString("id_producto"),
                    rs.getString("nombre_producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario"),
                    rs.getDouble("descuento_unitario"),
                    rs.getDouble("subtotal")
                );
                detalles.add(detalle);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles del ticket: " + e.getMessage());
            e.printStackTrace();
        }
        
        return detalles;
    }
      public String generarNumeroTicket() {
        String sql = "SELECT MAX(CAST(SUBSTRING(numero_ticket, 2) AS INT)) as ultimo FROM Ventas WHERE numero_ticket LIKE 'T%'";
        
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int ultimo = 0;
            if (rs.next()) {
                ultimo = rs.getInt("ultimo");
                if (rs.wasNull()) {
                    ultimo = 0;
                }
            }
            return String.format("T%06d", ultimo + 1);
            
        } catch (SQLException e) {
            System.err.println("Error al generar número de ticket: " + e.getMessage());
            e.printStackTrace();
            return "T" + System.currentTimeMillis(); // Número de ticket basado en timestamp como fallback
        }
    }
}
