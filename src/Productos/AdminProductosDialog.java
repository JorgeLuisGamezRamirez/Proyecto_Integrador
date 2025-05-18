package Vistas.Productos;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;
import Models.Producto;
import Models.ProductoDAO;
import java.util.List;

public class AdminProductosDialog extends JDialog {
    private JTextField txtID;
    private JTextField txtNombre;
    private JComboBox<String> cmbCategorias;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextArea txtDetalles;
    private ProductoDAO productoDAO;
    private List<String[]> categorias;
    private Timer filterTimer;

    public AdminProductosDialog(Frame parent) {
        super(parent, "Administración de Productos", true);
        productoDAO = new ProductoDAO();
        filterTimer = new Timer(300, e -> actualizarFiltros());
        filterTimer.setRepeats(false);
        setupComponents();
        setupWindow();
        cargarProductos(null, "", "");
    }

    private void setupComponents() {
        // Panel principal con gradiente
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(250, 250, 250),
                    w, h, new Color(240, 240, 240)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel superior para búsqueda y filtros
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);

        // ID
        JLabel lblID = new JLabel("ID:");
        txtID = new JTextField(10);
        styleTextField(txtID);
        txtID.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
        });

        // Nombre
        JLabel lblNombre = new JLabel("Nombre:");
        txtNombre = new JTextField(15);
        styleTextField(txtNombre);
        txtNombre.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
        });

        // Categoría
        JLabel lblCategoria = new JLabel("Categoría:");
        cmbCategorias = new JComboBox<>();
        cmbCategorias.setPreferredSize(new Dimension(150, 30));
        cargarCategorias();
        cmbCategorias.addActionListener(e -> {
            String[] categoriaSeleccionada = categorias.get(cmbCategorias.getSelectedIndex());
            Integer idCategoria = Integer.parseInt(categoriaSeleccionada[0]);
            if (idCategoria == 0) idCategoria = null;
            cargarProductos(idCategoria, "", "");
        });

        searchPanel.add(lblID);
        searchPanel.add(txtID);
        searchPanel.add(lblNombre);
        searchPanel.add(txtNombre);
        searchPanel.add(lblCategoria);
        searchPanel.add(cmbCategorias);

        // Panel central con tabla y detalles
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setOpaque(false);

        // Tabla de productos
        setupTabla();
        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.setPreferredSize(new Dimension(600, 300));

        // Panel de detalles
        JPanel detallesPanel = new JPanel(new BorderLayout(5, 5));
        detallesPanel.setOpaque(false);
        detallesPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            "Detalles del Producto Seleccionado",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(255, 20, 147)
        ));

        txtDetalles = new JTextArea(8, 20);
        txtDetalles.setEditable(false);
        txtDetalles.setLineWrap(true);
        txtDetalles.setWrapStyleWord(true);
        txtDetalles.setBackground(Color.WHITE);
        txtDetalles.setForeground(Color.BLACK);
        txtDetalles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDetalles.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollDetalles = new JScrollPane(txtDetalles);
        detallesPanel.add(scrollDetalles, BorderLayout.CENTER);

        // Panel para botones de acciones
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setOpaque(false);

        // Botón Alta de Producto
        JButton btnAlta = new JButton("Alta de Producto") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 20, 147),
                    0, getHeight(), new Color(199, 21, 133)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleButton(btnAlta);
        btnAlta.addActionListener(e -> mostrarDialogoAlta());

        // Botón Baja de Producto
        JButton btnBaja = new JButton("Baja de Producto") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 20, 147),
                    0, getHeight(), new Color(199, 21, 133)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleButton(btnBaja);
        btnBaja.addActionListener(e -> eliminarProducto());

        // Botón Regresar
        JButton btnRegresar = new JButton("Regresar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 20, 147),
                    0, getHeight(), new Color(199, 21, 133)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleButton(btnRegresar);
        btnRegresar.addActionListener(e -> dispose());

        bottomPanel.add(btnAlta);
        bottomPanel.add(btnBaja);
        bottomPanel.add(btnRegresar);

        centerPanel.add(scrollTabla, BorderLayout.CENTER);
        centerPanel.add(detallesPanel, BorderLayout.EAST);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupTabla() {
        String[] columnas = {"ID", "Nombre", "Categoría", "Descripción", "Precio unitario", "Existencias"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getTableHeader().setReorderingAllowed(false);
        tablaProductos.setRowHeight(25);
        
        // Estilo de la tabla
        tablaProductos.setBackground(Color.WHITE);
        tablaProductos.setForeground(Color.BLACK);
        tablaProductos.setGridColor(new Color(200, 200, 200));
        tablaProductos.getTableHeader().setBackground(new Color(255, 20, 147));
        tablaProductos.getTableHeader().setForeground(Color.WHITE);
        tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Alternar colores de fila
        tablaProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        // Configurar el ancho de las columnas
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(70);  // ID
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(120); // Nombre
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(100); // Categoría
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(200); // Descripción
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(100); // Precio
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(80);  // Existencias

        // Listener para mostrar detalles
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProductos.getSelectedRow() != -1) {
                mostrarDetallesProducto(tablaProductos.getSelectedRow());
            }
        });
    }

    private void setupWindow() {
        setSize(900, 500);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
    }

    private void cargarCategorias() {
        categorias = productoDAO.getCategorias();
        cmbCategorias.removeAllItems();
        for (String[] categoria : categorias) {
            cmbCategorias.addItem(categoria[1]);
        }
    }

    private void actualizarFiltros() {
        String idFiltro = txtID.getText().trim();
        String nombreFiltro = txtNombre.getText().trim();
        String[] categoriaSeleccionada = categorias.get(cmbCategorias.getSelectedIndex());
        Integer idCategoria = Integer.parseInt(categoriaSeleccionada[0]);
        if (idCategoria == 0) idCategoria = null;

        cargarProductos(idCategoria, idFiltro, nombreFiltro);
    }

    private void cargarProductos(Integer idCategoria, String idFiltro, String nombreFiltro) {
        // Limpiar la tabla
        while (modeloTabla.getRowCount() > 0) {
            modeloTabla.removeRow(0);
        }

        // Cargar productos desde la base de datos
        List<Producto> productos = productoDAO.getProductos(idCategoria, idFiltro, nombreFiltro);
        
        for (Producto producto : productos) {
            Object[] fila = {
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getNombreCategoria(),
                producto.getDescripcion(),
                producto.getPrecioUnitario(),
                producto.getExistencias()
            };
            modeloTabla.addRow(fila);
        }
    }

    private void mostrarDetallesProducto(int row) {
        String id = modeloTabla.getValueAt(row, 0).toString();
        String nombre = modeloTabla.getValueAt(row, 1).toString();
        String categoria = modeloTabla.getValueAt(row, 2).toString();
        String descripcion = modeloTabla.getValueAt(row, 3).toString();
        double precio = Double.parseDouble(modeloTabla.getValueAt(row, 4).toString());
        int existencias = Integer.parseInt(modeloTabla.getValueAt(row, 5).toString());        txtDetalles.setText(String.format(
            "ID: %s\n" +
            "Nombre: %s\n" +
            "Categoría: %s\n" +
            "Precio: $%.2f\n" +
            "Existencias: %d\n\n" +
            "Descripción:\n%s", 
            id, nombre, categoria, precio, existencias, descripcion));
    }

    private void iniciarBusquedaDinamica() {
        filterTimer.restart();
    }

    private void mostrarDialogoAlta() {
        RegistroProductoDialog dialog = new RegistroProductoDialog((Frame)this.getOwner());
        dialog.setVisible(true);
        if (dialog.isRegistroExitoso()) {
            // Recargar la tabla para mostrar el nuevo producto
            String idFiltro = txtID.getText().trim();
            String nombreFiltro = txtNombre.getText().trim();
            String[] categoriaSeleccionada = categorias.get(cmbCategorias.getSelectedIndex());
            Integer idCategoria = Integer.parseInt(categoriaSeleccionada[0]);
            if (idCategoria == 0) idCategoria = null;
            cargarProductos(idCategoria, idFiltro, nombreFiltro);
        }
    }    private void eliminarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un producto para eliminar",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idProducto = modeloTabla.getValueAt(filaSeleccionada, 0).toString();
        String nombreProducto = modeloTabla.getValueAt(filaSeleccionada, 1).toString();

        // Mostrar diálogo de confirmación con más detalles
        int confirmacion = JOptionPane.showConfirmDialog(this,
            String.format("¿Está seguro que desea dar de baja el producto?\n\n" +
                        "ID: %s\n" +
                        "Nombre: %s\n\n" +
                        "Esta acción no se puede deshacer.", 
                        idProducto, nombreProducto),
            "Confirmar baja de producto",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (productoDAO.eliminarProducto(idProducto)) {
                JOptionPane.showMessageDialog(this,
                    "Producto dado de baja exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                // Recargar la tabla para reflejar el cambio
                String idFiltro = txtID.getText().trim();
                String nombreFiltro = txtNombre.getText().trim();
                String[] categoriaSeleccionada = categorias.get(cmbCategorias.getSelectedIndex());
                Integer idCategoria = Integer.parseInt(categoriaSeleccionada[0]);
                if (idCategoria == 0) idCategoria = null;
                cargarProductos(idCategoria, idFiltro, nombreFiltro);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al dar de baja el producto",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
