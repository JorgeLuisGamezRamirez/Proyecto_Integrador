package Vistas.Productos;

import Models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class RegistroProductoDialog extends JDialog {
    private JTextField txtIdProducto;
    private JTextField txtNombre;
    private JTextField txtPrecioUnitario;
    private JTextField txtExistencias;
    private JTextField txtStockMinimo;
    private JTextArea txtDescripcion;
    private JComboBox<String> cmbCategorias;
    private JButton btnRegistrar;
    private JButton btnCancelar;
    private List<String[]> categorias;
    private boolean registroExitoso = false;
    private ProductoDAO productoDAO;

    public RegistroProductoDialog(Frame parent) {
        super(parent, "Registro de Producto", true);
        productoDAO = new ProductoDAO();
        setupComponents();
        setupDialog();
        cargarCategorias();
    }

    private void setupComponents() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(250, 250, 250),  // Casi blanco
                    w, h, new Color(240, 240, 240)   // Gris muy suave
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        panel.setLayout(null);

        // Campos del formulario
        int yPos = 20;
        int labelWidth = 150;
        int fieldWidth = 200;
        int height = 30;
        int gap = 35;

        // ID
        JLabel lblId = new JLabel("ID Producto:", SwingConstants.RIGHT);
        lblId.setBounds(10, yPos, labelWidth, height);
        panel.add(lblId);

        txtIdProducto = new JTextField();
        txtIdProducto.setBounds(170, yPos, fieldWidth, height);
        txtIdProducto.setEditable(false);
        txtIdProducto.setBackground(new Color(240, 240, 240));
        customizeField(txtIdProducto);
        panel.add(txtIdProducto);

        // Nombre
        yPos += gap;
        JLabel lblNombre = new JLabel("Nombre:", SwingConstants.RIGHT);
        lblNombre.setBounds(10, yPos, labelWidth, height);
        panel.add(lblNombre);

        txtNombre = new JTextField();
        txtNombre.setBounds(170, yPos, fieldWidth, height);
        customizeField(txtNombre);
        panel.add(txtNombre);

        // Categoría
        yPos += gap;
        JLabel lblCategoria = new JLabel("Categoría:", SwingConstants.RIGHT);
        lblCategoria.setBounds(10, yPos, labelWidth, height);
        panel.add(lblCategoria);

        cmbCategorias = new JComboBox<>();
        cmbCategorias.setBounds(170, yPos, fieldWidth, height);
        customizeComboBox(cmbCategorias);
        cmbCategorias.addActionListener(e -> {
            if (cmbCategorias.getSelectedIndex() != -1) {
                // +1 porque quitamos "Todas las categorías" al cargar las categorías
                int idCategoria = Integer.parseInt(categorias.get(cmbCategorias.getSelectedIndex() + 1)[0]);
                String nextId = productoDAO.generateNextProductId(idCategoria);
                txtIdProducto.setText(nextId);
            }
        });
        panel.add(cmbCategorias);

        // Precio Unitario
        yPos += gap;
        JLabel lblPrecio = new JLabel("Precio Unitario:", SwingConstants.RIGHT);
        lblPrecio.setBounds(10, yPos, labelWidth, height);
        panel.add(lblPrecio);

        txtPrecioUnitario = new JTextField();
        txtPrecioUnitario.setBounds(170, yPos, fieldWidth, height);
        customizeField(txtPrecioUnitario);
        panel.add(txtPrecioUnitario);

        // Existencias
        yPos += gap;
        JLabel lblExistencias = new JLabel("Existencias:", SwingConstants.RIGHT);
        lblExistencias.setBounds(10, yPos, labelWidth, height);
        panel.add(lblExistencias);

        txtExistencias = new JTextField();
        txtExistencias.setBounds(170, yPos, fieldWidth, height);
        customizeField(txtExistencias);
        panel.add(txtExistencias);

        // Stock Mínimo
        yPos += gap;
        JLabel lblStockMin = new JLabel("Stock Mínimo:", SwingConstants.RIGHT);
        lblStockMin.setBounds(10, yPos, labelWidth, height);
        panel.add(lblStockMin);

        txtStockMinimo = new JTextField();
        txtStockMinimo.setBounds(170, yPos, fieldWidth, height);
        customizeField(txtStockMinimo);
        panel.add(txtStockMinimo);

        // Descripción
        yPos += gap;
        JLabel lblDescripcion = new JLabel("Descripción:", SwingConstants.RIGHT);
        lblDescripcion.setBounds(10, yPos, labelWidth, height);
        panel.add(lblDescripcion);

        txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setBounds(170, yPos, fieldWidth, height * 2);
        customizeField(txtDescripcion);
        panel.add(scrollDescripcion);

        // Botones
        yPos += gap * 2;
        btnRegistrar = new JButton("Registrar");
        btnRegistrar.setBounds(100, yPos, 100, height);
        customizeButton(btnRegistrar);
        btnRegistrar.addActionListener(e -> registrar());
        panel.add(btnRegistrar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(210, yPos, 100, height);
        customizeButton(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnCancelar);

        setContentPane(panel);
    }

    private void setupDialog() {
        setSize(400, 500);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void customizeField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void customizeComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
    }

    private void customizeButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 20, 147));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 105, 180));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 20, 147));
            }
        });
    }

    private void cargarCategorias() {
        categorias = productoDAO.getCategorias();
        for (String[] categoria : categorias) {
            if (!categoria[0].equals("0")) { // Skip "Todas las categorías"
                cmbCategorias.addItem(categoria[1]);
            }
        }
    }

    private void registrar() {
        // Validación de campos
        if (txtIdProducto.getText().trim().isEmpty() ||
            txtNombre.getText().trim().isEmpty() ||
            txtPrecioUnitario.getText().trim().isEmpty() ||
            txtExistencias.getText().trim().isEmpty() ||
            txtStockMinimo.getText().trim().isEmpty() ||
            txtDescripcion.getText().trim().isEmpty() ||
            cmbCategorias.getSelectedIndex() == -1) {
            
            JOptionPane.showMessageDialog(this,
                "Todos los campos son obligatorios",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double precio = Double.parseDouble(txtPrecioUnitario.getText().trim());
            int existencias = Integer.parseInt(txtExistencias.getText().trim());
            int stockMinimo = Integer.parseInt(txtStockMinimo.getText().trim());

            if (precio <= 0 || existencias < 0 || stockMinimo < 0) {
                JOptionPane.showMessageDialog(this,
                    "Los valores numéricos deben ser válidos y positivos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }            // Crear el producto
            Producto producto = new Producto(
                txtIdProducto.getText().trim(),
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                precio,
                existencias,
                stockMinimo,
                Integer.parseInt(categorias.get(cmbCategorias.getSelectedIndex() + 1)[0]), // +1 porque quitamos "Todas las categorías"
                "Activo",
                null, // fecha_creacion se establece en la base de datos
                cmbCategorias.getSelectedItem().toString()
            );

            // Guardar el producto
            if (productoDAO.save(producto)) {
                registroExitoso = true;
                JOptionPane.showMessageDialog(this,
                    "Producto registrado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al registrar el producto",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingrese valores numéricos válidos",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistroExitoso() {
        return registroExitoso;
    }
}
