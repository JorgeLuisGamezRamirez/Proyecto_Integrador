package Vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import Vistas.Productos.GestionProductosDialog;
import Vistas.Productos.AdminProductosDialog;
import Vistas.Productos.EditarInventarioDialog;
import Vistas.Usuarios.AdminUsuariosDialog;
import Vistas.Reportes.GenerarReporteDialog;
import Vistas.Reportes.ReporteVentasGenerator;
import Vistas.Reportes.ReporteUsuariosGenerator;
import Vistas.Reportes.ReporteDevolucionesGenerator;
import Vistas.Reportes.ReporteInventarioGenerator;
import Vistas.Reportes.ReporteProductosProgressDialog;
import java.util.List;
import java.util.ArrayList;
import Models.Producto;
import Models.ProductoDAO;
import Models.Ticket;
import Models.TicketDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import basedatos.Conexion;

public class VentanaPrincipal extends JFrame {
    private String nombreEmpleado;
    private int idEmpleado;
    
    private JPanel panelProductos;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private DefaultTableModel modeloTablaCarrito;
    private JMenuBar menuBar;
    
    // Componentes para método de pago
    private JPanel panelPago;
    private JRadioButton rbEfectivo;
    private JRadioButton rbTarjeta;
    private JTextField txtNumeroTarjeta;
    private JTextField txtFechaExpiracion;
    private JTextField txtCVV;
    private JButton btnPagar;
    
    // Componentes para alertas de stock
    private JPanel panelAlertas;
    private JTextArea txtAlertasStock;
    private JLabel lblTotal;

    // Timer para actualización automática de alertas
    private Timer alertasTimer;
    private Timer clockTimer;
    
    public VentanaPrincipal(String rolUsuario, String usuario) {
        setTitle("Sistema integral de Ventas");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Obtener información del empleado logueado
        try {
            Connection conn = Conexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT id_empleado, nombre, apellido FROM Empleados WHERE usuario = ?"
            );
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                this.idEmpleado = rs.getInt("id_empleado");
                this.nombreEmpleado = rs.getString("nombre") + " " + rs.getString("apellido");
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al obtener información del empleado",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
          // Crear menú principal
        menuBar = new JMenuBar();
        setupMenu();
        setJMenuBar(menuBar);
        
        setupComponents();
        configurarPermisos(rolUsuario);
          // Iniciar timer para actualización de alertas
        iniciarActualizacionAutomatica();
    }

    private void setupComponents() {        // Configurar ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel de botones laterales
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        sidePanel.setBackground(new Color(255, 20, 147));  // Color fucsia intenso
        setupSideButtons(sidePanel);
        mainPanel.add(sidePanel, BorderLayout.WEST);

        // Panel central (productos)
        setupProductosPanel();
        mainPanel.add(panelProductos, BorderLayout.CENTER);

        // Panel inferior para método de pago y alertas
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottomPanel.setBackground(new Color(255, 228, 225)); // Color rosa muy suave de fondo

        // Panel de método de pago (izquierda)
        setupPagoPanel(bottomPanel);
        
        // Panel de alertas (derecha)
        setupAlertasPanel(bottomPanel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }    private void setupMenu() {
        // Menús principales
        JMenu menuVentas = new JMenu("Ventas");
        setMenuIcon(menuVentas, "/Img/Carrito-de-compras.png", 20, 20);
        
        // Agregar submenú a Ventas
        JMenuItem nuevaVenta = new JMenuItem("Nueva Venta");
        setMenuIcon(nuevaVenta, "/Img/compras.png", 16, 16);
        nuevaVenta.addActionListener(e -> {
            // Reinicializar el carrito
            String[] columnas = {"ID", "Nombre", "Precio Unit.", "Cantidad", "Total"};
            modeloTablaCarrito = new DefaultTableModel(columnas, 0);
            tablaProductos.setModel(modeloTablaCarrito);
            limpiarCarrito();
        });
        menuVentas.add(nuevaVenta);

        // Agregar submenú Devoluciones
        JMenuItem devoluciones = new JMenuItem("Devoluciones");
        setMenuIcon(devoluciones, "/Img/devoluciones.png", 16, 16);
        devoluciones.addActionListener(e -> {
            DevolucionesDialog dialog = new DevolucionesDialog(this);
            dialog.setVisible(true);
        });
        menuVentas.add(devoluciones);

        JMenu menuProductos = new JMenu("Productos");
        setMenuIcon(menuProductos, "/Img/productos.png", 20, 20);
        // Agregar submenú Administrar Productos
        JMenuItem adminProductos = new JMenuItem("Administrar Productos");
        adminProductos.addActionListener(e -> {
            AdminProductosDialog dialog = new AdminProductosDialog(this);
            dialog.setVisible(true);
        });
        menuProductos.add(adminProductos);        JMenu menuInventario = new JMenu("Inventario");
        setMenuIcon(menuInventario, "/Img/inventario.png", 20, 20);
        
        // Agregar opción para editar inventario
        JMenuItem editarInventario = new JMenuItem("Editar Inventario");
        setMenuIcon(editarInventario, "/Img/Actualizar (2).png", 16, 16);
        editarInventario.addActionListener(e -> {
            EditarInventarioDialog dialog = new EditarInventarioDialog(this);
            dialog.setVisible(true);
        });
        menuInventario.add(editarInventario);        JMenu menuReportes = new JMenu("Reportes");
        setMenuIcon(menuReportes, "/Img/reporte.png", 20, 20);

        // Menú Reportes
        JMenuItem reporteVentas = new JMenuItem("Reporte de Ventas");
        JMenuItem reporteUsuarios = new JMenuItem("Reporte de Usuarios");
        JMenuItem reporteDevoluciones = new JMenuItem("Reporte de Devoluciones");
        JMenuItem reporteInventario = new JMenuItem("Reporte de Inventario");
        JMenuItem reporteProductos = new JMenuItem("Reporte de Productos");
        
        // Configurar los iconos de los reportes
        setMenuIcon(reporteVentas, "/Img/reporte de ventas.png", 20, 20);
        setMenuIcon(reporteUsuarios, "/Img/reporte de usuarios.png", 20, 20);
        setMenuIcon(reporteDevoluciones, "/Img/reporte de devoluciones.png", 20, 20);
        setMenuIcon(reporteInventario, "/Img/reporte de inventario.png", 20, 20);
        setMenuIcon(reporteProductos, "/Img/reporte.png", 20, 20);
        
        // Agregar ActionListeners
        reporteVentas.addActionListener(e -> generarReporteVentas());
        reporteUsuarios.addActionListener(e -> generarReporteUsuarios());
        reporteDevoluciones.addActionListener(e -> generarReporteDevoluciones());
        reporteInventario.addActionListener(e -> generarReporteInventario());
        reporteProductos.addActionListener(e -> generarReporteProductos());
        
        // Agregar items al menú Reportes
        menuReportes.add(reporteVentas);
        menuReportes.add(reporteUsuarios);
        menuReportes.add(reporteDevoluciones);
        menuReportes.add(reporteInventario);
        menuReportes.add(reporteProductos);
        
        JMenu menuAjustes = new JMenu("Ajustes");
        setMenuIcon(menuAjustes, "/Img/ajustes.png", 20, 20);
        
        // Agregar submenú Administrar Usuarios
        JMenuItem adminUsuarios = new JMenuItem("Administrar Usuarios");
        setMenuIcon(adminUsuarios, "/Img/usuarios.png", 16, 16);
        adminUsuarios.addActionListener(e -> {
            AdminUsuariosDialog dialog = new AdminUsuariosDialog(this);
            dialog.setVisible(true);
        });
        menuAjustes.add(adminUsuarios);

        menuBar.add(menuVentas);
        menuBar.add(menuProductos);
        menuBar.add(menuInventario);
        menuBar.add(menuReportes);
        menuBar.add(menuAjustes);

        // Agregar espacio flexible
        menuBar.add(Box.createHorizontalGlue());

        // Panel para información del usuario y hora
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        infoPanel.setOpaque(false);

        // Etiqueta para el usuario
        JLabel lblUsuarioInfo = new JLabel(this.nombreEmpleado);
        lblUsuarioInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsuarioInfo.setForeground(Color.BLACK);

        // Icono de usuario
        try {
            ImageIcon iconUsuario = new ImageIcon("src/Img/usuarios.png");
            Image scaled = iconUsuario.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            lblUsuarioInfo.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("Error cargando icono de usuario");
        }
        infoPanel.add(lblUsuarioInfo);
        
        // Separador vertical
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 20));
        separator.setForeground(Color.BLACK);
        infoPanel.add(separator);

        // Etiqueta para la hora
        JLabel lblHora = new JLabel();
        lblHora.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHora.setForeground(Color.BLACK);
        
        // Icono de reloj
        try {
            ImageIcon iconReloj = new ImageIcon("src/Img/clock.png");
            Image scaled = iconReloj.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            lblHora.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("Error cargando icono de reloj");
        }
        infoPanel.add(lblHora);
        
        // Timer para actualizar la hora cada segundo
        clockTimer = new Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
            lblHora.setText(" " + sdf.format(new java.util.Date()));
        });
        clockTimer.start();

        menuBar.add(infoPanel);

        // Agregar botón de cerrar sesión con icono
        infoPanel.add(Box.createHorizontalStrut(15));
        JButton btnCerrar = new JButton("Cerrar Sesión");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            ImageIcon iconCerrar = new ImageIcon("src/Img/logout.png");
            Image scaled = iconCerrar.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            btnCerrar.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("Error cargando icono de cerrar sesión");
        }

        btnCerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCerrar.setForeground(new Color(255, 20, 147));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCerrar.setForeground(Color.BLACK);
            }
        });
        
        btnCerrar.addActionListener(e -> cerrarSesion());
        infoPanel.add(btnCerrar);
    }    private void setupSideButtons(JPanel sidePanel) {        // Botones con iconos
        JButton btnAgregarProducto = agregarBotonLateral(sidePanel, "Agregar Producto", "/Img/Carrito-de-compras.png");
        btnAgregarProducto.addActionListener(e -> mostrarGestionProductos());
        
        JButton btnEliminar = agregarBotonLateral(sidePanel, "Eliminar", "/Img/GuardarTodo.png");
        btnEliminar.addActionListener(e -> eliminarProductoCarrito());
        
        JButton btnEditar = agregarBotonLateral(sidePanel, "Editar", "/Img/Actualizar (2).png");
        btnEditar.addActionListener(e -> editarCantidadProducto());
    }    private JButton agregarBotonLateral(JPanel panel, String texto, String rutaIcono) {
        JButton btn = new JButton(texto);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setMaximumSize(new Dimension(150, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        try {
            // Usar ruta directa en vez de getResource
            ImageIcon icon = new ImageIcon("src" + rutaIcono);
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Error cargando icono: " + rutaIcono);
        }

        btn.setMargin(new Insets(5, 10, 5, 10));
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);
        return btn;
    }

    private void mostrarGestionProductos() {
        GestionProductosDialog dialog = new GestionProductosDialog(this);
        dialog.setVisible(true);
    }    private void setupProductosPanel() {
        panelProductos = new JPanel(new BorderLayout());
        panelProductos.setBorder(BorderFactory.createTitledBorder("Productos"));

        // Configurar la tabla
        String[] columnas = {"ID", "Nombre", "Precio Unit.", "Cantidad", "Total"};
        modeloTablaCarrito = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hace que ninguna celda sea editable
            }
        };
        
        tablaProductos = new JTable(modeloTablaCarrito);
        tablaProductos.getTableHeader().setReorderingAllowed(false); // Evitar que se reordenen las columnas
        tablaProductos.getTableHeader().setResizingAllowed(false); // Evitar que se redimensionen las columnas
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Solo permitir selección de una fila
        tablaProductos.setShowGrid(true);
        tablaProductos.setGridColor(new Color(200, 200, 200));
        tablaProductos.setRowHeight(25);
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Estilo del encabezado
        JTableHeader header = tablaProductos.getTableHeader();
        header.setBackground(new Color(255, 20, 147)); // Fucsia
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Centrar el contenido de todas las columnas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            tablaProductos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Agregar la tabla a un scroll pane con bordes personalizados
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 20, 147), 1));
        panelProductos.add(scrollPane, BorderLayout.CENTER);
    }

    private void configurarPermisos(String rolUsuario) {
        // Configurar accesos según el rol
        switch (rolUsuario.toLowerCase()) {
            case "admin":
                // Acceso total
                break;
            case "gerente":
                // Acceso limitado a ciertas funciones
                break;
            case "empleado":
                // Acceso básico
                break;
        }
    }

    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Cierre de Sesión",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmar == JOptionPane.YES_OPTION) {
            dispose();
            new Login().setVisible(true);
        }
    }

    private void setupPagoPanel(JPanel container) {
        panelPago = new JPanel();
        panelPago.setLayout(new BoxLayout(panelPago, BoxLayout.Y_AXIS));
        panelPago.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 2),  // Borde fucsia
            "Método de Pago",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(255, 20, 147)  // Texto fucsia
        ));
        panelPago.setBackground(new Color(255, 248, 220));  // Fondo amarillo muy suave
        panelPago.setPreferredSize(new Dimension(300, 150));

        // Panel para los radio buttons
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.setOpaque(false);
        ButtonGroup groupMetodoPago = new ButtonGroup();
        rbEfectivo = new JRadioButton("Efectivo");
        rbTarjeta = new JRadioButton("Tarjeta");
        rbEfectivo.setSelected(true);
        
        // Estilizar radio buttons
        rbEfectivo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rbTarjeta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rbEfectivo.setForeground(new Color(255, 20, 147));  // Texto fucsia
        rbTarjeta.setForeground(new Color(255, 20, 147));   // Texto fucsia
        rbEfectivo.setOpaque(false);
        rbTarjeta.setOpaque(false);
        
        groupMetodoPago.add(rbEfectivo);
        groupMetodoPago.add(rbTarjeta);
        radioPanel.add(rbEfectivo);
        radioPanel.add(rbTarjeta);
        panelPago.add(radioPanel);

        // Panel para campos de tarjeta
        JPanel panelTarjeta = new JPanel(new GridLayout(3, 2, 5, 5));
        panelTarjeta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelTarjeta.setOpaque(false);

        // Labels y campos
        JLabel lblNumTarjeta = new JLabel("Número:");
        txtNumeroTarjeta = new JTextField(15);
        JLabel lblFechaExp = new JLabel("Vencimiento:");
        txtFechaExpiracion = new JTextField(5);
        JLabel lblCVV = new JLabel("CVV:");
        txtCVV = new JTextField(3);

        // Estilizar labels y campos
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 12);
        Color labelColor = new Color(255, 20, 147);  // Fucsia para las etiquetas
        lblNumTarjeta.setFont(labelFont);
        lblFechaExp.setFont(labelFont);
        lblCVV.setFont(labelFont);
        lblNumTarjeta.setForeground(labelColor);
        lblFechaExp.setForeground(labelColor);
        lblCVV.setForeground(labelColor);

        panelTarjeta.add(lblNumTarjeta);
        panelTarjeta.add(txtNumeroTarjeta);
        panelTarjeta.add(lblFechaExp);
        panelTarjeta.add(txtFechaExpiracion);
        panelTarjeta.add(lblCVV);
        panelTarjeta.add(txtCVV);
        panelTarjeta.setVisible(false);
        panelPago.add(panelTarjeta);

        // Etiqueta para el total
        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(255, 20, 147));  // Fucsia
        panelPago.add(Box.createVerticalStrut(10));
        panelPago.add(lblTotal);

        // Botón de pagar con gradiente
        btnPagar = new JButton("PAGAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 20, 147),  // Fucsia
                    0, getHeight(), new Color(255, 105, 180)  // Rosa más claro
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnPagar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPagar.setMaximumSize(new Dimension(200, 35));
        btnPagar.setForeground(Color.WHITE);
        btnPagar.setBorderPainted(false);
        btnPagar.setContentAreaFilled(false);
        btnPagar.setFocusPainted(false);
        btnPagar.setOpaque(false);
        btnPagar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPagar.addActionListener(e -> realizarPago());

        // Listeners para mostrar/ocultar campos de tarjeta
        rbTarjeta.addActionListener(e -> panelTarjeta.setVisible(rbTarjeta.isSelected()));
        rbEfectivo.addActionListener(e -> panelTarjeta.setVisible(false));

        panelPago.add(Box.createVerticalStrut(10));
        panelPago.add(btnPagar);
        container.add(panelPago);
    }

    private void setupAlertasPanel(JPanel container) {
        panelAlertas = new JPanel(new BorderLayout(5, 5));
        panelAlertas.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 2),  // Borde fucsia
            "Alertas de Stock",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(255, 20, 147)  // Texto fucsia
        ));
        panelAlertas.setBackground(new Color(255, 248, 220));  // Fondo amarillo muy suave

        txtAlertasStock = new JTextArea();
        txtAlertasStock.setEditable(false);
        txtAlertasStock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtAlertasStock.setForeground(new Color(255, 20, 147));  // Texto fucsia
        txtAlertasStock.setBackground(new Color(255, 248, 220));  // Fondo amarillo muy suave
        txtAlertasStock.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollAlertas = new JScrollPane(txtAlertasStock);
        scrollAlertas.setBorder(BorderFactory.createEmptyBorder());
        scrollAlertas.setBackground(new Color(255, 248, 220));  // Fondo amarillo muy suave
        panelAlertas.add(scrollAlertas, BorderLayout.CENTER);

        // Inicializar con algunas alertas de ejemplo
        actualizarAlertas();

        container.add(panelAlertas);
    }    private void realizarPago() {
        // Verificar que haya productos en el carrito
        if (modeloTablaCarrito == null || modeloTablaCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No hay productos en el carrito",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el total a pagar
        double subtotal = 0.0;
        for (int i = 0; i < modeloTablaCarrito.getRowCount(); i++) {
            subtotal += Double.parseDouble(modeloTablaCarrito.getValueAt(i, 4).toString());
        }
        
        // Calcular impuestos (16% IVA)
        double impuestos = subtotal * 0.16;
        double total = subtotal + impuestos;        String metodoPago = "";
        String nombreCliente = "";
        boolean pagoExitoso = false;
        double efectivoRecibido = 0.0;
        
        if (rbTarjeta.isSelected()) {
            if (txtNumeroTarjeta.getText().isEmpty() || 
                txtFechaExpiracion.getText().isEmpty() || 
                txtCVV.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Por favor complete todos los campos de la tarjeta",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Pedir nombre del cliente para pago con tarjeta
            nombreCliente = JOptionPane.showInputDialog(this, 
                "Ingrese el nombre del cliente:",
                "Datos del Cliente",
                JOptionPane.QUESTION_MESSAGE);
                
            if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "El nombre del cliente es requerido",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Confirmar el pago con tarjeta?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);
                
            if (confirmar == JOptionPane.YES_OPTION) {
                metodoPago = "Tarjeta";
                pagoExitoso = true;
            }
        } else {
            // Pago en efectivo
            PagoEfectivoDialog dialogoEfectivo = new PagoEfectivoDialog(this, total);
            dialogoEfectivo.setVisible(true);
              if (dialogoEfectivo.isPagoConfirmado()) {
                efectivoRecibido = dialogoEfectivo.getEfectivoRecibido();
                nombreCliente = dialogoEfectivo.getNombreCliente();
                metodoPago = "Efectivo";
                
                // Calcular y mostrar el cambio
                double cambio = efectivoRecibido - total;
                JOptionPane.showMessageDialog(this,
                    String.format("Cambio a entregar: $%.2f", cambio),
                    "Cambio",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                pagoExitoso = true;
            }
        }
        
        if (pagoExitoso) {
            try {                // Generar número de ticket
                TicketDAO ticketDAO = new TicketDAO();
                String numeroTicket = ticketDAO.generarNumeroTicket();
                if (numeroTicket == null) {
                    throw new Exception("Error al generar número de ticket");
                }
                
                // Crear venta en la base de datos
                java.sql.Connection conn = basedatos.Conexion.getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Ventas (fecha, id_empleado, id_cliente, total, metodo_pago, estado, numero_ticket, subtotal, impuestos) " +
                    "VALUES (GETDATE(), ?, ?, ?, ?, 'COMPLETADA', ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                );
                
                stmt.setInt(1, this.idEmpleado); // ID del empleado actual
                stmt.setNull(2, java.sql.Types.INTEGER); // ID del cliente (si no está registrado)
                stmt.setDouble(3, total);
                stmt.setString(4, metodoPago);
                stmt.setString(5, numeroTicket);
                stmt.setDouble(6, subtotal);
                stmt.setDouble(7, impuestos);
                
                stmt.executeUpdate();
                
                // Obtener el ID de la venta generada
                java.sql.ResultSet rs = stmt.getGeneratedKeys();
                int idVenta = 0;
                if (rs.next()) {
                    idVenta = rs.getInt(1);
                }
                
                // Insertar los detalles de la venta
                stmt = conn.prepareStatement(
                    "INSERT INTO DetallesVenta (id_venta, id_producto, cantidad, precio_unitario, descuento_unitario, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"
                );
                
                for (int i = 0; i < modeloTablaCarrito.getRowCount(); i++) {
                    stmt.setInt(1, idVenta);
                    stmt.setString(2, modeloTablaCarrito.getValueAt(i, 0).toString());
                    stmt.setInt(3, Integer.parseInt(modeloTablaCarrito.getValueAt(i, 3).toString()));
                    stmt.setDouble(4, Double.parseDouble(modeloTablaCarrito.getValueAt(i, 2).toString()));
                    stmt.setDouble(5, 0.0); // Descuento unitario
                    stmt.setDouble(6, Double.parseDouble(modeloTablaCarrito.getValueAt(i, 4).toString()));
                    stmt.executeUpdate();
                }
                  // Crear objeto Ticket
                Ticket ticket = new Ticket(
                    idVenta,
                    numeroTicket,
                    new java.util.Date(),
                    subtotal,
                    impuestos,
                    total,
                    metodoPago,
                    "COMPLETADA",
                    0, // ID cliente (si no está registrado)
                    this.idEmpleado  // ID empleado
                );
                  // Establecer nombres de cliente y empleado
                ticket.setNombreCliente(nombreCliente);
                ticket.setNombreEmpleado(this.nombreEmpleado);
                
                // Crear lista de detalles
                List<Ticket.DetalleTicket> detalles = new ArrayList<>();
                for (int i = 0; i < modeloTablaCarrito.getRowCount(); i++) {
                    detalles.add(new Ticket.DetalleTicket(
                        modeloTablaCarrito.getValueAt(i, 0).toString(),
                        modeloTablaCarrito.getValueAt(i, 1).toString(),
                        Integer.parseInt(modeloTablaCarrito.getValueAt(i, 3).toString()),
                        Double.parseDouble(modeloTablaCarrito.getValueAt(i, 2).toString()),
                        0.0, // Descuento unitario
                        Double.parseDouble(modeloTablaCarrito.getValueAt(i, 4).toString())
                    ));
                }
                ticket.setDetalles(detalles);
                
                // Mostrar diálogo de ticket
                GenerarTicketDialog ticketDialog = new GenerarTicketDialog(this, ticket);
                ticketDialog.setVisible(true);
                
                // Limpiar carrito
                limpiarCarrito();
                
                conn.close();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al procesar la venta: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }private void actualizarAlertas() {
        ProductoDAO productoDAO = new ProductoDAO();
        List<Producto> productosBajoStock = productoDAO.getProductosBajoStock();
        
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("PRODUCTOS CON BAJO STOCK:\n\n");
        
        if (productosBajoStock.isEmpty()) {
            mensaje.append("No hay productos con stock bajo.\n");
        } else {
            for (Producto producto : productosBajoStock) {
                String estado = producto.getExistencias() == 0 ? "AGOTADO" : 
                              producto.getExistencias() + " unidades restantes";
                mensaje.append(String.format("• %s - %s (Mínimo: %d)\n", 
                    producto.getNombre(), 
                    estado,
                    producto.getStockMinimo()));
            }
        }
        
        txtAlertasStock.setText(mensaje.toString());
    }    private void limpiarCarrito() {
        // Limpiar la tabla de productos
        if (modeloTablaCarrito != null) {
            while (modeloTablaCarrito.getRowCount() > 0) {
                modeloTablaCarrito.removeRow(0);
            }
        }
        // Resetear campos de pago
        rbEfectivo.setSelected(true);
        txtNumeroTarjeta.setText("");
        txtFechaExpiracion.setText("");
        txtCVV.setText("");
        // Actualizar el total
        actualizarTotalCarrito();
    }

    public void agregarAlCarrito(String idProducto, String nombre, double precio, int cantidad) {        // Si la tabla del carrito no está inicializada, inicializarla
        if (modeloTablaCarrito == null) {
            String[] columnas = {"ID", "Nombre", "Precio Unit.", "Cantidad", "Total"};
            modeloTablaCarrito = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Hace que ninguna celda sea editable
                }
            };
            tablaProductos.setModel(modeloTablaCarrito);
        }

        // Calcular el total
        double total = precio * cantidad;

        // Buscar si el producto ya está en el carrito
        boolean encontrado = false;
        for (int i = 0; i < modeloTablaCarrito.getRowCount(); i++) {
            if (modeloTablaCarrito.getValueAt(i, 0).equals(idProducto)) {
                // Actualizar cantidad y total
                int cantidadActual = Integer.parseInt(modeloTablaCarrito.getValueAt(i, 3).toString());
                modeloTablaCarrito.setValueAt(cantidadActual + cantidad, i, 3);
                modeloTablaCarrito.setValueAt((cantidadActual + cantidad) * precio, i, 4);
                encontrado = true;
                break;
            }
        }

        // Si el producto no está en el carrito, agregarlo como nueva fila
        if (!encontrado) {
            Object[] fila = {
                idProducto,
                nombre,
                precio,
                cantidad,
                total
            };
            modeloTablaCarrito.addRow(fila);
        }

        actualizarTotalCarrito();
    }

    private void actualizarTotalCarrito() {
        double total = 0;
        for (int i = 0; i < modeloTablaCarrito.getRowCount(); i++) {
            total += Double.parseDouble(modeloTablaCarrito.getValueAt(i, 4).toString());
        }
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    private void eliminarProductoCarrito() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un producto para eliminar",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar este producto del carrito?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            // Obtener datos del producto
            String idProducto = modeloTablaCarrito.getValueAt(filaSeleccionada, 0).toString();
            int cantidad = Integer.parseInt(modeloTablaCarrito.getValueAt(filaSeleccionada, 3).toString());
            
            // Devolver el stock a la base de datos
            ProductoDAO productoDAO = new ProductoDAO();
            if (productoDAO.actualizarStock(idProducto, -cantidad)) { // Cantidad negativa para sumar al stock
                modeloTablaCarrito.removeRow(filaSeleccionada);
                actualizarTotalCarrito();
                JOptionPane.showMessageDialog(this,
                    "Producto eliminado del carrito exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al actualizar el stock",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarCantidadProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un producto para editar",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idProducto = modeloTablaCarrito.getValueAt(filaSeleccionada, 0).toString();
        int cantidadActual = Integer.parseInt(modeloTablaCarrito.getValueAt(filaSeleccionada, 3).toString());
        double precioUnitario = Double.parseDouble(modeloTablaCarrito.getValueAt(filaSeleccionada, 2).toString());

        // Crear un panel para la entrada de la nueva cantidad
        JPanel panel = new JPanel();
        panel.add(new JLabel("Nueva cantidad:"));
        JTextField txtNuevaCantidad = new JTextField(5);
        txtNuevaCantidad.setText(String.valueOf(cantidadActual));
        panel.add(txtNuevaCantidad);

        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Editar Cantidad", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int nuevaCantidad = Integer.parseInt(txtNuevaCantidad.getText().trim());
                if (nuevaCantidad <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "La cantidad debe ser mayor a 0",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Calcular la diferencia para actualizar el stock
                int diferencia = nuevaCantidad - cantidadActual;
                ProductoDAO productoDAO = new ProductoDAO();

                // Si es positiva, necesitamos verificar si hay suficiente stock
                if (diferencia > 0) {
                    List<Producto> productos = productoDAO.getProductos(null, idProducto, "");
                    if (!productos.isEmpty() && productos.get(0).getExistencias() < diferencia) {
                        JOptionPane.showMessageDialog(this,
                            "No hay suficiente stock disponible",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Actualizar el stock en la base de datos
                if (productoDAO.actualizarStock(idProducto, diferencia)) {
                    // Actualizar la tabla del carrito
                    modeloTablaCarrito.setValueAt(nuevaCantidad, filaSeleccionada, 3);
                    modeloTablaCarrito.setValueAt(nuevaCantidad * precioUnitario, filaSeleccionada, 4);
                    actualizarTotalCarrito();
                    
                    JOptionPane.showMessageDialog(this,
                        "Cantidad actualizada exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error al actualizar el stock",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Por favor, ingrese un número válido",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }    private void iniciarActualizacionAutomatica() {
        if (alertasTimer != null) {
            alertasTimer.stop();
        }
        
        // Actualizar cada 5 minutos (300000 ms)
        alertasTimer = new Timer(300000, e -> {
            try {
                actualizarAlertas();
            } catch (Exception ex) {
                System.err.println("Error al actualizar alertas: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        alertasTimer.setRepeats(true);
        alertasTimer.start();
        
        // Hacer la primera actualización inmediatamente
        try {
            actualizarAlertas();
        } catch (Exception ex) {
            System.err.println("Error en la actualización inicial de alertas: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        // Asegurar que el timer se detenga cuando se cierre la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (alertasTimer != null) {
                    alertasTimer.stop();
                }
            }
        });
    }

    private void setMenuIcon(JMenuItem menuItem, String iconPath, int width, int height) {
    try {
        ImageIcon icon = new ImageIcon("src" + iconPath);
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        menuItem.setIcon(new ImageIcon(img));
    } catch (Exception e) {
        System.err.println("Error cargando icono para menú: " + iconPath);
    }
}

private void generarReporteVentas() {
    GenerarReporteDialog dialog = new GenerarReporteDialog(this);
    dialog.setVisible(true);
    
    if (dialog.isConfirmed()) {
        ReporteVentasGenerator generator = new ReporteVentasGenerator();
        generator.generarReporte(
            dialog.getFechaInicio(),
            dialog.getFechaFin(),
            dialog.getFormato()
        );
    }
}
    
    private void generarReporteUsuarios() {
        GenerarReporteDialog dialog = new GenerarReporteDialog(this);
        dialog.setTitle("Generar Reporte de Usuario");
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ReporteUsuariosGenerator generator = new ReporteUsuariosGenerator();
            generator.generarReporte(
                dialog.getFechaInicio(),
                dialog.getFechaFin(),
                dialog.getFormato(),
                this.idEmpleado  // ID del empleado actual
            );
        }
    }
    
    private void generarReporteDevoluciones() {
        GenerarReporteDialog dialog = new GenerarReporteDialog(this);
        dialog.setTitle("Generar Reporte de Devoluciones");
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ReporteDevolucionesGenerator generator = new ReporteDevolucionesGenerator();
            generator.generarReporte(
                dialog.getFechaInicio(),
                dialog.getFechaFin(),
                dialog.getFormato()
            );
        }
    }
    
    private void generarReporteInventario() {
        String[] opciones = {"PDF", "Excel", "XML", "JSON"};
    String formato = (String) JOptionPane.showInputDialog(
        this,
        "Seleccione el formato del reporte:",
        "Reporte de Inventario",
        JOptionPane.QUESTION_MESSAGE,
        null,
        opciones,
        opciones[0]
    );
    
    if (formato != null) {
        ReporteInventarioGenerator reporteGenerator = new ReporteInventarioGenerator();
        if (formato.equals("Excel")) {
            formato = "XLS"; // El generador usa XLS internamente
        }
        reporteGenerator.generarReporte(formato);
    }
    }
      private void generarReporteProductos() {
        ReporteProductosProgressDialog.mostrarReporte(this);
    }

    // Asegurar que los timers se detengan al cerrar la ventana
    @Override
    public void dispose() {
        if (clockTimer != null && clockTimer.isRunning()) {
            clockTimer.stop();
        }
        if (alertasTimer != null && alertasTimer.isRunning()) {
            alertasTimer.stop();
        }
        super.dispose();
    }
}
