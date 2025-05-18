package Vistas;

import Models.*;
import basedatos.Conexion;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RealizarDevolucionDialog extends JDialog {
    private int idVenta;
    private String numeroTicket;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextArea txtMotivo;
    private JLabel lblTotal;
    private boolean devolucionRealizada = false;
    
    public RealizarDevolucionDialog(Frame parent, int idVenta, String numeroTicket) {
        super(parent, "Realizar Devolución - Ticket " + numeroTicket, true);
        this.idVenta = idVenta;
        this.numeroTicket = numeroTicket;
        setupComponents();
        cargarDetallesVenta();
    }

    private void setupComponents() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior con información de la venta
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Ticket N°: " + numeroTicket));
        add(infoPanel, BorderLayout.NORTH);

        // Panel central con tabla de productos
        String[] columnas = {"ID", "Producto", "Cantidad Original", "Precio Unit.", "Devolver"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? Integer.class : Object.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        tablaProductos = new JTable(modeloTabla);
        configureTable(tablaProductos);

        // Panel para la tabla y el motivo
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel para el motivo
        JPanel motivoPanel = new JPanel(new BorderLayout(5, 5));
        motivoPanel.add(new JLabel("Motivo de la devolución:"), BorderLayout.NORTH);
        txtMotivo = new JTextArea(3, 40);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        motivoPanel.add(new JScrollPane(txtMotivo), BorderLayout.CENTER);
        centerPanel.add(motivoPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior con total y botones
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para el total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotal = new JLabel("Total a devolver: $0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPanel.add(lblTotal);
        bottomPanel.add(totalPanel, BorderLayout.WEST);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProcesar = new JButton("Procesar Devolución");
        btnProcesar.addActionListener(e -> procesarDevolucion());
        buttonPanel.add(btnProcesar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        buttonPanel.add(btnCancelar);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listener para actualizar el total cuando cambian las cantidades
        modeloTabla.addTableModelListener(e -> actualizarTotal());
    }

    private void configureTable(JTable table) {
        table.setRowHeight(25);
        table.setShowGrid(true);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Configurar el renderizador para centrar el contenido
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 4) { // No aplicar al spinner de cantidad
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void cargarDetallesVenta() {        String sql = "SELECT dv.id_producto, p.nombre, dv.cantidad, dv.precio_unitario " +
            "FROM DetallesVenta dv " +
            "JOIN Productos p ON dv.id_producto = p.id_producto " +
            "WHERE dv.id_venta = ?";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                    rs.getString("id_producto"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario"),
                    0  // Cantidad a devolver, inicialmente 0
                });
            }

            // Agregar límites a las cantidades a devolver
            for (int row = 0; row < modeloTabla.getRowCount(); row++) {
                final int currentRow = row;
                final int maxCantidad = (int) modeloTabla.getValueAt(row, 2);
                
                // Usar un JSpinner para controlar la cantidad a devolver
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, maxCantidad, 1));
                spinner.addChangeListener(e -> {
                    modeloTabla.setValueAt(spinner.getValue(), currentRow, 4);
                    actualizarTotal();
                });
                
                tablaProductos.getColumn("Devolver").setCellEditor(new DefaultCellEditor(new JTextField()) {
                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value,
                            boolean isSelected, int row, int column) {
                        return spinner;
                    }
                });
                
                tablaProductos.getColumn("Devolver").setCellRenderer(new TableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        JSpinner spinner = new JSpinner(new SpinnerNumberModel((int)value, 0, maxCantidad, 1));
                        if (isSelected) {
                            spinner.setBackground(table.getSelectionBackground());
                            spinner.setForeground(table.getSelectionForeground());
                        }
                        return spinner;
                    }
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cargar los detalles de la venta: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarTotal() {
        double total = 0;
        for (int row = 0; row < modeloTabla.getRowCount(); row++) {
            int cantidad = (Integer) modeloTabla.getValueAt(row, 4);
            double precio = (Double) modeloTabla.getValueAt(row, 3);
            total += cantidad * precio;
        }
        lblTotal.setText(String.format("Total a devolver: $%.2f", total));
    }    private void procesarDevolucion() {
        try {
            // Validación del motivo
            if (txtMotivo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Debe especificar un motivo para la devolución",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validación de productos seleccionados
            boolean hayProductosParaDevolver = false;
            for (int row = 0; row < modeloTabla.getRowCount(); row++) {
                int cantidad = (Integer) modeloTabla.getValueAt(row, 4);
                if (cantidad > 0) {
                    hayProductosParaDevolver = true;
                    // Validar que la cantidad no exceda el original
                    int cantidadOriginal = (Integer) modeloTabla.getValueAt(row, 2);
                    if (cantidad > cantidadOriginal) {
                        JOptionPane.showMessageDialog(this,
                            "La cantidad a devolver no puede ser mayor que la cantidad original",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            if (!hayProductosParaDevolver) {
                JOptionPane.showMessageDialog(this,
                    "Debe seleccionar al menos un producto para devolver",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear objeto Devolucion
            Devolucion devolucion = new Devolucion();
            devolucion.setIdVenta(idVenta);
            devolucion.setMotivo(txtMotivo.getText().trim());

            // Agregar detalles
            double totalDevolucion = 0.0;
            for (int row = 0; row < modeloTabla.getRowCount(); row++) {
                int cantidad = (Integer) modeloTabla.getValueAt(row, 4);
                if (cantidad > 0) {
                    DetalleDevolucion detalle = new DetalleDevolucion();
                    detalle.setIdProducto(modeloTabla.getValueAt(row, 0).toString());
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioUnitario((Double) modeloTabla.getValueAt(row, 3));
                    detalle.setNombreProducto(modeloTabla.getValueAt(row, 1).toString());
                    devolucion.agregarDetalle(detalle);
                    totalDevolucion += cantidad * (Double) modeloTabla.getValueAt(row, 3);
                }
            }
            devolucion.setTotalDevolucion(totalDevolucion);

            // Confirmación del usuario
            int confirmacion = JOptionPane.showConfirmDialog(this,
                String.format("¿Confirma la devolución por un total de $%.2f?", totalDevolucion),
                "Confirmar Devolución",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            // Procesar la devolución
            DevolucionDAO devolucionDAO = new DevolucionDAO();
            if (devolucionDAO.crearDevolucion(devolucion)) {
                // Obtener el ticket de devolución completo
                TicketDevolucionDAO ticketDevolucionDAO = new TicketDevolucionDAO();
                TicketDevolucion ticketDevolucion = ticketDevolucionDAO.obtenerTicketDevolucion(devolucion.getIdDevolucion());
                
                if (ticketDevolucion != null) {
                    // Mostrar el diálogo del ticket
                    GenerarTicketDevolucionDialog ticketDialog = new GenerarTicketDevolucionDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        ticketDevolucion
                    );
                    ticketDialog.setVisible(true);
                }
                
                devolucionRealizada = true;
                dispose();
            } else {
                throw new Exception("No se pudo procesar la devolución");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al procesar la devolución: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDevolucionRealizada() {
        return devolucionRealizada;
    }
}
