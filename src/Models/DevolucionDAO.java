package Models;

import basedatos.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DevolucionDAO {    public boolean crearDevolucion(Devolucion devolucion) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String numeroTicket = null;
        
        try {
            System.out.println("Iniciando proceso de devolución...");
            conn = Conexion.getConnection();
            if (conn == null) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos");
            }
            System.out.println("Conexión establecida correctamente");
            
            conn.setAutoCommit(false); // Iniciar transacción
            System.out.println("Transacción iniciada");
            
            // Verificar si la venta existe y está completada
            String sqlVerificarVenta = "SELECT estado FROM Ventas WHERE id_venta = ?";
            try (PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificarVenta)) {
                stmtVerificar.setInt(1, devolucion.getIdVenta());
                ResultSet rs = stmtVerificar.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("La venta no existe");
                }
                String estadoVenta = rs.getString("estado");
                if (!"COMPLETADA".equals(estadoVenta)) {
                    throw new SQLException("La venta no está en estado COMPLETADA");
                }
            }
            System.out.println("Venta verificada correctamente");
            
            // Generar número de ticket
            TicketDevolucionDAO ticketDAO = new TicketDevolucionDAO();
            numeroTicket = ticketDAO.generarNumeroTicketDevolucion();
            if (numeroTicket == null) {
                throw new SQLException("Error al generar número de ticket");
            }
            System.out.println("Número de ticket generado: " + numeroTicket);            // Insertar la devolución
            String sql = "INSERT INTO Devoluciones " +
                "(id_venta, fecha, motivo, total_devolucion, estado, numero_ticket) " +
                "VALUES (?, GETDATE(), ?, ?, 'COMPLETADA', ?)";
                
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, devolucion.getIdVenta());
            stmt.setString(2, devolucion.getMotivo());
            stmt.setDouble(3, devolucion.getTotalDevolucion());
            stmt.setString(4, numeroTicket);
            
            System.out.println("Ejecutando insert en Devoluciones...");
            
            int affectedRows = stmt.executeUpdate();            if (affectedRows > 0) {
                System.out.println("Devolución insertada correctamente");
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int idDevolucion = rs.getInt(1);
                    devolucion.setIdDevolucion(idDevolucion);
                    System.out.println("ID de devolución generado: " + idDevolucion);
                    
                    // Insertar los detalles
                    boolean detallesInsertados = insertarDetallesDevolucion(conn, devolucion);
                    
                    if (detallesInsertados) {
                        System.out.println("Detalles insertados correctamente, confirmando transacción...");
                        conn.commit();
                        System.out.println("Transacción confirmada exitosamente");
                        return true;
                    } else {
                        System.out.println("Error al insertar detalles, realizando rollback...");
                        conn.rollback();
                        return false;
                    }
                }
            } else {
                System.out.println("No se insertó la devolución");
            }return false;
              } catch (SQLException e) {
            System.out.println("Error SQL: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.out.println("Realizando rollback por error...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error al hacer rollback: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error al procesar la devolución: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }    private boolean insertarDetallesDevolucion(Connection conn, Devolucion devolucion) throws SQLException {        String sql = "INSERT INTO DetallesDevolucion " +
            "(id_devolucion, id_producto, cantidad, precio_unitario, subtotal) " +
            "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            System.out.println("Insertando detalles de devolución...");
            
            for (DetalleDevolucion detalle : devolucion.getDetalles()) {
                // Calcular el subtotal
                double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
                
                stmt.setInt(1, devolucion.getIdDevolucion());
                stmt.setString(2, detalle.getIdProducto());
                stmt.setInt(3, detalle.getCantidad());
                stmt.setDouble(4, detalle.getPrecioUnitario());
                stmt.setDouble(5, subtotal);
                stmt.addBatch();
                
                System.out.println(String.format(
                    "Preparando detalle - Producto: %s, Cantidad: %d, Precio: %.2f, Subtotal: %.2f",
                    detalle.getIdProducto(), 
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    subtotal
                ));
                
                // Actualizar el stock del producto
                actualizarStockProducto(conn, detalle.getIdProducto(), detalle.getCantidad());
            }
            
            System.out.println("Ejecutando batch de detalles...");
            stmt.executeBatch();
            System.out.println("Detalles insertados correctamente");
            return true;
        } catch (SQLException e) {
            System.out.println("Error al insertar detalles: " + e.getMessage());
            throw e;
        }
    }private void actualizarStockProducto(Connection conn, String idProducto, int cantidad) throws SQLException {
        // Primero verificamos si existe la columna fecha_ultima_modificacion
        boolean tieneColumnaFecha = false;
        try {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getColumns(null, null, "Productos", "fecha_ultima_modificacion");
            tieneColumnaFecha = rs.next();
        } catch (SQLException e) {
            System.out.println("No se pudo verificar la columna fecha_ultima_modificacion: " + e.getMessage());
        }

        // SQL base para la actualización
        String sql = tieneColumnaFecha ?
            "UPDATE Productos SET existencias = existencias + ?, fecha_ultima_modificacion = GETDATE() WHERE id_producto = ?" :
            "UPDATE Productos SET existencias = existencias + ? WHERE id_producto = ?";
            
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setString(2, idProducto);
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo actualizar el stock del producto: " + idProducto);
            }
            System.out.println("Stock actualizado correctamente para el producto: " + idProducto);
        }
    }

    public List<Devolucion> obtenerDevoluciones(Date fechaInicio, Date fechaFin) {
        List<Devolucion> devoluciones = new ArrayList<>();        String sql = "SELECT d.*, v.numero_ticket " +
            "FROM Devoluciones d " +
            "JOIN Ventas v ON d.id_venta = v.id_venta " +
            "WHERE d.fecha BETWEEN ? AND ? " +
            "ORDER BY d.fecha DESC";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, new Timestamp(fechaInicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(fechaFin.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Devolucion devolucion = new Devolucion(
                    rs.getInt("id_devolucion"),
                    rs.getInt("id_venta"),
                    rs.getTimestamp("fecha"),
                    rs.getString("motivo"),
                    rs.getDouble("total_devolucion"),
                    rs.getString("estado")
                );
                cargarDetallesDevolucion(conn, devolucion);
                devoluciones.add(devolucion);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return devoluciones;
    }

    private void cargarDetallesDevolucion(Connection conn, Devolucion devolucion) throws SQLException {        String sql = "SELECT d.*, p.nombre as nombre_producto " +
            "FROM DetallesDevolucion d " +
            "JOIN Productos p ON d.id_producto = p.id_producto " +
            "WHERE d.id_devolucion = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, devolucion.getIdDevolucion());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                DetalleDevolucion detalle = new DetalleDevolucion(
                    rs.getInt("id_detalle"),
                    rs.getInt("id_devolucion"),
                    rs.getString("id_producto"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario"),
                    rs.getString("nombre_producto")
                );
                devolucion.getDetalles().add(detalle);
            }
        }
    }
}
