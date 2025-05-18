package Vistas;

import basedatos.Conexion;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class DevolucionesDialog extends JDialog {
    private JTextField txtNumeroTicket;
    private JTable tablaVentas;
    private DefaultTableModel modeloTablaVentas;
    private JButton btnBuscar;
    private JButton btnRealizarDevolucion;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private Color colorPrincipal = new Color(255, 20, 147); // Rosa fucsia

    public DevolucionesDialog(Frame parent) {
        super(parent, "Gestión de Devoluciones", true);
        setupComponents();
        cargarDatos();
    }

    private void setupComponents() {
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Panel superior de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblTicket = new JLabel("Número de Ticket:");
        lblTicket.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTicket.setForeground(colorPrincipal);

        txtNumeroTicket = new JTextField(20);
        txtNumeroTicket.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNumeroTicket.setPreferredSize(new Dimension(200, 30));
        txtNumeroTicket.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorPrincipal),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(colorPrincipal);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(e -> buscarVenta());

        searchPanel.add(lblTicket);
        searchPanel.add(txtNumeroTicket);
        searchPanel.add(btnBuscar);

        // Tabla de ventas
        String[] columnas = {"ID Venta", "Fecha", "N° Ticket", "Total", "Estado"};
        modeloTablaVentas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVentas = new JTable(modeloTablaVentas);
        configureTable(tablaVentas);

        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, colorPrincipal));

        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        btnRealizarDevolucion = new JButton("Realizar Devolución");
        btnRealizarDevolucion.setBackground(colorPrincipal);
        btnRealizarDevolucion.setForeground(Color.WHITE);
        btnRealizarDevolucion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRealizarDevolucion.setFocusPainted(false);
        btnRealizarDevolucion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRealizarDevolucion.setEnabled(false);
        btnRealizarDevolucion.addActionListener(e -> realizarDevolucion());

        buttonPanel.add(btnRealizarDevolucion);

        // Agregar componentes al panel principal
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Listener para la selección de la tabla
        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            btnRealizarDevolucion.setEnabled(tablaVentas.getSelectedRow() != -1);
        });

        // Presionar Enter para buscar
        txtNumeroTicket.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buscarVenta();
                }
            }
        });
    }

    private void configureTable(JTable table) {
        // Configurar la apariencia general de la tabla
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(colorPrincipal);
        table.setSelectionForeground(Color.WHITE);

        // Configurar el header
        JTableHeader header = table.getTableHeader();
        header.setBackground(colorPrincipal);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));

        // Centrar el contenido de todas las columnas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void buscarVenta() {
        String numeroTicket = txtNumeroTicket.getText().trim();
        if (numeroTicket.isEmpty()) {
            cargarDatos();
            return;
        }

        modeloTablaVentas.setRowCount(0);
        String sql = "SELECT v.id_venta, v.fecha, v.numero_ticket, v.total, v.estado " +
                    "FROM Ventas v " +
                    "WHERE v.numero_ticket LIKE ? AND v.estado = 'COMPLETADA' " +
                    "ORDER BY v.fecha DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + numeroTicket + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                modeloTablaVentas.addRow(new Object[]{
                    rs.getInt("id_venta"),
                    sdf.format(rs.getTimestamp("fecha")),
                    rs.getString("numero_ticket"),
                    String.format("$%.2f", rs.getDouble("total")),
                    rs.getString("estado")
                });
            }

            if (modeloTablaVentas.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "No se encontraron ventas con el número de ticket especificado",
                    "Sin resultados",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al buscar la venta: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        modeloTablaVentas.setRowCount(0);
        String sql = "SELECT TOP 100 v.id_venta, v.fecha, v.numero_ticket, v.total, v.estado " +
                    "FROM Ventas v " +
                    "WHERE v.estado = 'COMPLETADA' " +
                    "ORDER BY v.fecha DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                modeloTablaVentas.addRow(new Object[]{
                    rs.getInt("id_venta"),
                    sdf.format(rs.getTimestamp("fecha")),
                    rs.getString("numero_ticket"),
                    String.format("$%.2f", rs.getDouble("total")),
                    rs.getString("estado")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cargar las ventas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void realizarDevolucion() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow != -1) {
            int idVenta = (int) tablaVentas.getValueAt(selectedRow, 0);
            String numeroTicket = tablaVentas.getValueAt(selectedRow, 2).toString();

            RealizarDevolucionDialog dialog = new RealizarDevolucionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                idVenta,
                numeroTicket
            );
            dialog.setVisible(true);

            if (dialog.isDevolucionRealizada()) {
                cargarDatos();
                JOptionPane.showMessageDialog(this,
                    "Devolución realizada con éxito",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
