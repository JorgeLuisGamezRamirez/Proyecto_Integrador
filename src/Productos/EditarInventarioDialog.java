package Vistas.Productos;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;
import Models.Producto;
import Models.ProductoDAO;
import java.util.List;

public class EditarInventarioDialog extends JDialog {
    private JTextField txtID;
    private JTextField txtNombre;
    private JComboBox<String> cmbCategorias;    private JTable tablaProductosOriginal;
    private DefaultTableModel modeloTablaOriginal;
    private JTextField txtNombreEdit;
    private JTextField txtPrecioEdit;
    private JTextField txtDescripcionEdit;
    private JComboBox<String> cmbCategoriasEdit;
    private JTextField txtStockMinimoEdit;
    private JTextField txtExistenciasEdit;  // Nuevo campo para existencias
    private ProductoDAO productoDAO;
    private List<String[]> categorias;
    private Timer filterTimer;
    private JButton btnGuardarCambios;

    public EditarInventarioDialog(Frame parent) {
        super(parent, "Editar Inventario", true);
        productoDAO = new ProductoDAO();
        filterTimer = new Timer(300, e -> actualizarFiltros());
        filterTimer.setRepeats(false);
        setupComponents();
        setupWindow();
        cargarCategorias();
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

        // Panel de búsqueda
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

        // Panel central con las dos tablas
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setOpaque(false);

        // Panel izquierdo (tabla original)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            "Información Original",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(255, 20, 147)
        ));

        setupTablasProductos();
        JScrollPane scrollTablaOriginal = new JScrollPane(tablaProductosOriginal);
        leftPanel.add(scrollTablaOriginal, BorderLayout.CENTER);

        // Panel derecho (información editable)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            "Información Modificada",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(255, 20, 147)
        ));

        // Panel de edición
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Campos de edición
        JLabel lblNombreEdit = new JLabel("Nombre:", SwingConstants.RIGHT);
        txtNombreEdit = new JTextField(20);
        styleTextField(txtNombreEdit);

        JLabel lblPrecioEdit = new JLabel("Precio:", SwingConstants.RIGHT);
        txtPrecioEdit = new JTextField(20);
        styleTextField(txtPrecioEdit);

        JLabel lblDescripcionEdit = new JLabel("Descripción:", SwingConstants.RIGHT);
        txtDescripcionEdit = new JTextField(20);
        styleTextField(txtDescripcionEdit);

        JLabel lblCategoriaEdit = new JLabel("Categoría:", SwingConstants.RIGHT);
        cmbCategoriasEdit = new JComboBox<>();
        cmbCategoriasEdit.setPreferredSize(new Dimension(200, 30));

        JLabel lblStockMinimoEdit = new JLabel("Stock Mínimo:", SwingConstants.RIGHT);
        txtStockMinimoEdit = new JTextField(20);
        styleTextField(txtStockMinimoEdit);

        JLabel lblExistenciasEdit = new JLabel("Existencias:", SwingConstants.RIGHT);
        txtExistenciasEdit = new JTextField(20);
        styleTextField(txtExistenciasEdit);

        // Agregar componentes al panel de edición
        gbc.gridy = 0;
        gbc.gridx = 0; editPanel.add(lblNombreEdit, gbc);
        gbc.gridx = 1; editPanel.add(txtNombreEdit, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; editPanel.add(lblPrecioEdit, gbc);
        gbc.gridx = 1; editPanel.add(txtPrecioEdit, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0; editPanel.add(lblDescripcionEdit, gbc);
        gbc.gridx = 1; editPanel.add(txtDescripcionEdit, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0; editPanel.add(lblCategoriaEdit, gbc);
        gbc.gridx = 1; editPanel.add(cmbCategoriasEdit, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0; editPanel.add(lblStockMinimoEdit, gbc);
        gbc.gridx = 1; editPanel.add(txtStockMinimoEdit, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0; editPanel.add(lblExistenciasEdit, gbc);
        gbc.gridx = 1; editPanel.add(txtExistenciasEdit, gbc);

        rightPanel.add(editPanel, BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        // Panel inferior con botones
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setOpaque(false);

        // Botón Guardar Cambios
        btnGuardarCambios = new JButton("Guardar Cambios") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(200, 15, 115));
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(255, 30, 150));
                } else {
                    g.setColor(new Color(255, 20, 147));
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        styleButton(btnGuardarCambios);
        btnGuardarCambios.addActionListener(e -> guardarCambios());
        btnGuardarCambios.setEnabled(false);

        // Botón Regresar
        JButton btnRegresar = new JButton("Regresar") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    g.setColor(new Color(200, 15, 115));
                } else if (getModel().isRollover()) {
                    g.setColor(new Color(255, 30, 150));
                } else {
                    g.setColor(new Color(255, 20, 147));
                }
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        styleButton(btnRegresar);
        btnRegresar.addActionListener(e -> dispose());

        bottomPanel.add(btnGuardarCambios);
        bottomPanel.add(btnRegresar);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupTablasProductos() {
        String[] columnas = {"ID", "Nombre", "Categoría", "Descripción", "Precio", "Stock Min.", "Existencias"};
        
        // Tabla Original
        modeloTablaOriginal = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaProductosOriginal = new JTable(modeloTablaOriginal);
        configurarTabla(tablaProductosOriginal);
        
        // Agregar listener de selección
        tablaProductosOriginal.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProductosOriginal.getSelectedRow() != -1) {
                mostrarDetallesProducto(tablaProductosOriginal.getSelectedRow());
                btnGuardarCambios.setEnabled(true);
            }
        });
    }

    private void configurarTabla(JTable tabla) {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setRowHeight(25);
        
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(Color.BLACK);
        tabla.setGridColor(new Color(200, 200, 200));
        tabla.getTableHeader().setBackground(new Color(255, 20, 147));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Alternar colores de fila
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                
                return c;
            }
        });
    }

    private void setupWindow() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 30));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
    }

    private void styleButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(130, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void cargarCategorias() {
        categorias = productoDAO.getCategorias();
        cmbCategorias.removeAllItems();
        cmbCategoriasEdit.removeAllItems();
        for (String[] categoria : categorias) {
            cmbCategorias.addItem(categoria[1]);
            cmbCategoriasEdit.addItem(categoria[1]);
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
        while (modeloTablaOriginal.getRowCount() > 0) {
            modeloTablaOriginal.removeRow(0);
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
                producto.getStockMinimo(),
                producto.getExistencias() // Agregar existencias a la tabla
            };
            modeloTablaOriginal.addRow(fila);
        }
    }

    private void mostrarDetallesProducto(int row) {
        String nombre = modeloTablaOriginal.getValueAt(row, 1).toString();
        String categoria = modeloTablaOriginal.getValueAt(row, 2).toString();
        String descripcion = modeloTablaOriginal.getValueAt(row, 3).toString();
        String precio = modeloTablaOriginal.getValueAt(row, 4).toString();
        String stockMinimo = modeloTablaOriginal.getValueAt(row, 5).toString();
        String existencias = modeloTablaOriginal.getValueAt(row, 6).toString();

        txtNombreEdit.setText(nombre);
        txtDescripcionEdit.setText(descripcion);
        txtPrecioEdit.setText(precio);
        txtStockMinimoEdit.setText(stockMinimo);
        txtExistenciasEdit.setText(existencias);
        cmbCategoriasEdit.setSelectedItem(categoria);
    }

    private void guardarCambios() {
        int filaSeleccionada = tablaProductosOriginal.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un producto para editar",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double nuevoPrecio = Double.parseDouble(txtPrecioEdit.getText().trim());
            int nuevoStockMinimo = Integer.parseInt(txtStockMinimoEdit.getText().trim());
            int nuevasExistencias = Integer.parseInt(txtExistenciasEdit.getText().trim());

            if (nuevoPrecio <= 0 || nuevoStockMinimo < 0 || nuevasExistencias < 0) {
                JOptionPane.showMessageDialog(this,
                    "El precio debe ser mayor a 0 y las existencias y stock mínimo no pueden ser negativos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String idProducto = modeloTablaOriginal.getValueAt(filaSeleccionada, 0).toString();
            String nombreOriginal = modeloTablaOriginal.getValueAt(filaSeleccionada, 1).toString();
            String categoriaOriginal = modeloTablaOriginal.getValueAt(filaSeleccionada, 2).toString();
            String descripcionOriginal = modeloTablaOriginal.getValueAt(filaSeleccionada, 3).toString();
            double precioOriginal = Double.parseDouble(modeloTablaOriginal.getValueAt(filaSeleccionada, 4).toString());
            int stockMinimoOriginal = Integer.parseInt(modeloTablaOriginal.getValueAt(filaSeleccionada, 5).toString());
            int existenciasOriginales = Integer.parseInt(modeloTablaOriginal.getValueAt(filaSeleccionada, 6).toString());

            String mensaje = "¿Está seguro que desea guardar los siguientes cambios?\n\n";
            
            if (!nombreOriginal.equals(txtNombreEdit.getText().trim())) {
                mensaje += "Nombre: " + nombreOriginal + " → " + txtNombreEdit.getText().trim() + "\n";
            }
            if (!categoriaOriginal.equals(cmbCategoriasEdit.getSelectedItem())) {
                mensaje += "Categoría: " + categoriaOriginal + " → " + cmbCategoriasEdit.getSelectedItem() + "\n";
            }
            if (!descripcionOriginal.equals(txtDescripcionEdit.getText().trim())) {
                mensaje += "Descripción: " + descripcionOriginal + " → " + txtDescripcionEdit.getText().trim() + "\n";
            }
            if (precioOriginal != nuevoPrecio) {
                mensaje += String.format("Precio: $%.2f → $%.2f\n", precioOriginal, nuevoPrecio);
            }
            if (stockMinimoOriginal != nuevoStockMinimo) {
                mensaje += "Stock Mínimo: " + stockMinimoOriginal + " → " + nuevoStockMinimo + "\n";
            }
            if (existenciasOriginales != nuevasExistencias) {
                mensaje += "Existencias: " + existenciasOriginales + " → " + nuevasExistencias + "\n";
            }

            int confirmacion = JOptionPane.showConfirmDialog(this,
                mensaje,
                "Confirmar cambios",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                Producto productoModificado = new Producto(
                    idProducto,
                    txtNombreEdit.getText().trim(),
                    txtDescripcionEdit.getText().trim(),
                    nuevoPrecio,
                    nuevasExistencias,  // Actualizamos las existencias
                    nuevoStockMinimo,
                    obtenerIdCategoria(cmbCategoriasEdit.getSelectedItem().toString()),
                    "Activo",
                    null,
                    cmbCategoriasEdit.getSelectedItem().toString()
                );

                if (productoDAO.actualizarProducto(productoModificado)) {
                    JOptionPane.showMessageDialog(this,
                        "Producto actualizado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    actualizarFiltros();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error al actualizar el producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingrese valores numéricos válidos para precio, stock mínimo y existencias",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private int obtenerIdCategoria(String nombreCategoria) {
        for (String[] categoria : categorias) {
            if (categoria[1].equals(nombreCategoria)) {
                return Integer.parseInt(categoria[0]);
            }
        }
        return 0; // Valor por defecto en caso de no encontrar la categoría
    }

    private void iniciarBusquedaDinamica() {
        filterTimer.restart();
    }
}
