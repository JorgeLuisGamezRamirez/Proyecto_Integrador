package Vistas.Usuarios;

import Models.User;
import Models.UserDAO;
import Vistas.RegistroDialog;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import javax.swing.border.*;
import java.util.List;

public class AdminUsuariosDialog extends JDialog {
    private JTextField txtBusqueda;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextArea txtDetalles;
    private UserDAO userDAO;
    private Timer filterTimer;

    // Campos para edición
    private JTextField txtNombreEdit;
    private JTextField txtApellidoEdit;
    private JTextField txtUsuarioEdit;
    private JTextField txtTelefonoEdit;
    private JTextField txtDireccionEdit;
    private JComboBox<String> cmbRolEdit;
    private JPasswordField txtPasswordEdit;
    private JPasswordField txtConfirmarPasswordEdit;

    public AdminUsuariosDialog(Frame parent) {
        super(parent, "Administración de Usuarios", true);
        userDAO = new UserDAO();
        filterTimer = new Timer(300, e -> actualizarFiltros());
        filterTimer.setRepeats(false);
        setupComponents();
        setupWindow();
        cargarUsuarios("");
    }

    // Método público para recargar la tabla de usuarios
    public void recargarUsuarios() {
        cargarUsuarios(txtBusqueda.getText().trim());
    }

    private void setupComponents() {
        // Panel principal con gradiente
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                
                // Crear gradiente de fondo
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 255, 255),
                    0, h, new Color(255, 240, 245)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel de búsqueda estilizado
        JPanel searchPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);

        JLabel lblBusqueda = new JLabel("Buscar usuario:");
        txtBusqueda = new JTextField(20);
        styleTextField(txtBusqueda);
        txtBusqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { iniciarBusquedaDinamica(); }
        });

        searchPanel.add(lblBusqueda);
        searchPanel.add(txtBusqueda);

        // Panel central con tabla y panel de edición
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setOpaque(false);

        // Panel izquierdo (tabla y detalles)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);

        // Configurar tabla
        setupTabla();
        JScrollPane scrollTabla = new JScrollPane(tablaUsuarios);

        // Panel de detalles
        JPanel detallesPanel = new JPanel(new BorderLayout(5, 5));
        detallesPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            "Detalles del Usuario",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(255, 20, 147)
        ));

        txtDetalles = new JTextArea(4, 20);
        txtDetalles.setEditable(false);
        txtDetalles.setLineWrap(true);
        txtDetalles.setWrapStyleWord(true);
        JScrollPane scrollDetalles = new JScrollPane(txtDetalles);
        detallesPanel.add(scrollDetalles);

        leftPanel.add(scrollTabla, BorderLayout.CENTER);
        leftPanel.add(detallesPanel, BorderLayout.SOUTH);

        // Panel derecho (edición)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            "Editar Usuario",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(255, 20, 147)
        ));

        // Panel de campos de edición
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        setupEditPanel(editPanel, gbc);
        rightPanel.add(editPanel, BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnAgregar = new JButton("Agregar Usuario") {
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
        styleButton(btnAgregar);
        btnAgregar.addActionListener(e -> mostrarDialogoRegistro());

        JButton btnEliminar = new JButton("Eliminar Usuario") {
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
        styleButton(btnEliminar);
        btnEliminar.addActionListener(e -> eliminarUsuario());

        JButton btnGuardar = new JButton("Guardar Cambios") {
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
        styleButton(btnGuardar);
        btnGuardar.addActionListener(e -> guardarCambios());

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

        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnRegresar);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void setupEditPanel(JPanel editPanel, GridBagConstraints gbc) {
        // Nombre
        gbc.gridy = 0;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Nombre:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtNombreEdit = new JTextField(20);
        styleTextField(txtNombreEdit);
        editPanel.add(txtNombreEdit, gbc);

        // Apellido
        gbc.gridy = 1;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Apellido:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtApellidoEdit = new JTextField(20);
        styleTextField(txtApellidoEdit);
        editPanel.add(txtApellidoEdit, gbc);

        // Usuario
        gbc.gridy = 2;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Usuario:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtUsuarioEdit = new JTextField(20);
        styleTextField(txtUsuarioEdit);
        editPanel.add(txtUsuarioEdit, gbc);

        // Teléfono
        gbc.gridy = 3;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Teléfono:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtTelefonoEdit = new JTextField(20);
        styleTextField(txtTelefonoEdit);
        editPanel.add(txtTelefonoEdit, gbc);

        // Dirección
        gbc.gridy = 4;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Dirección:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtDireccionEdit = new JTextField(20);
        styleTextField(txtDireccionEdit);
        editPanel.add(txtDireccionEdit, gbc);

        // Rol
        gbc.gridy = 5;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Rol:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        cmbRolEdit = new JComboBox<>(new String[]{"Empleado", "Gerente", "Admin"});
        cmbRolEdit.setPreferredSize(new Dimension(200, 30));
        editPanel.add(cmbRolEdit, gbc);

        // Contraseña
        gbc.gridy = 6;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Contraseña:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtPasswordEdit = new JPasswordField(20);
        styleTextField(txtPasswordEdit);
        editPanel.add(txtPasswordEdit, gbc);

        // Confirmar Contraseña
        gbc.gridy = 7;
        gbc.gridx = 0;
        editPanel.add(new JLabel("Confirmar Contraseña:", SwingConstants.RIGHT), gbc);
        gbc.gridx = 1;
        txtConfirmarPasswordEdit = new JPasswordField(20);
        styleTextField(txtConfirmarPasswordEdit);
        editPanel.add(txtConfirmarPasswordEdit, gbc);
    }

    private void setupTabla() {
        String[] columnas = {"ID", "Usuario", "Nombre", "Apellido", "Rol", "Teléfono", "Dirección"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getTableHeader().setReorderingAllowed(false);
        tablaUsuarios.setRowHeight(30);
        
        // Configurar anchos de columna
        int[] anchos = {50, 100, 150, 150, 100, 100, 200};
        for (int i = 0; i < anchos.length; i++) {
            tablaUsuarios.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // Configurar el renderizador para centrar el contenido
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaUsuarios.getColumnCount(); i++) {
            tablaUsuarios.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Estilo de la tabla
        tablaUsuarios.setBackground(Color.WHITE);
        tablaUsuarios.setForeground(Color.BLACK);
        tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaUsuarios.setGridColor(new Color(230, 230, 230));
        tablaUsuarios.getTableHeader().setBackground(new Color(255, 20, 147));
        tablaUsuarios.getTableHeader().setForeground(Color.WHITE);
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Alternar colores de fila
        tablaUsuarios.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(255, 240, 245));
                } else {
                    c.setBackground(new Color(255, 182, 193));
                }
                
                return c;
            }
        });

        // Listener para mostrar detalles
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                mostrarDetallesUsuario(tablaUsuarios.getSelectedRow());
            }
        });
    }

    private void setupWindow() {
        setSize(1000, 600);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 20, 147));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 82, 182));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(255, 20, 147));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(200, 15, 115));
            }
        });
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        textField.setBackground(new Color(255, 255, 255));
        textField.setCaretColor(new Color(255, 20, 147));
        
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 82, 182), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
    }

    private void cargarUsuarios(String filtro) {
        // Limpiar la tabla
        modeloTabla.setRowCount(0);

        List<User> usuarios = userDAO.getAllUsers();
        for (User usuario : usuarios) {
            if (filtro.isEmpty() ||
                usuario.getUsuario().toLowerCase().contains(filtro.toLowerCase()) ||
                usuario.getNombre().toLowerCase().contains(filtro.toLowerCase()) ||
                usuario.getApellido().toLowerCase().contains(filtro.toLowerCase()) ||
                usuario.getRol().toLowerCase().contains(filtro.toLowerCase()) ||
                usuario.getTelefono().toLowerCase().contains(filtro.toLowerCase()) ||
                usuario.getDireccion().toLowerCase().contains(filtro.toLowerCase())) {
                
                Object[] fila = {
                    usuario.getId(),
                    usuario.getUsuario(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getRol(),
                    usuario.getTelefono(),
                    usuario.getDireccion()
                };
                modeloTabla.addRow(fila);
            }
        }
        
        // Si hay filas, seleccionar la primera
        if (modeloTabla.getRowCount() > 0) {
            tablaUsuarios.setRowSelectionInterval(0, 0);
        }
    }

    private void mostrarDialogoRegistro() {
        RegistroDialog dialog = new RegistroDialog(this);
        dialog.setVisible(true);
    }

    private void eliminarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un usuario para eliminar",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idUsuario = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombreUsuario = modeloTabla.getValueAt(filaSeleccionada, 2).toString() +
                             " " + modeloTabla.getValueAt(filaSeleccionada, 3).toString();

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar al usuario " + nombreUsuario + "?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(idUsuario)) {
                JOptionPane.showMessageDialog(this,
                    "Usuario eliminado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios(txtBusqueda.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el usuario",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDetallesUsuario(int row) {
        int id = (int) modeloTabla.getValueAt(row, 0);
        User usuario = userDAO.findById(id);
        if (usuario != null) {            // Actualizar el área de detalles con todos los datos
            txtDetalles.setText(String.format(
                "ID: %d\n" +
                "Usuario: %s\n" +
                "Nombre completo: %s %s\n" +
                "Rol: %s\n" +
                "Teléfono: %s\n" +
                "Dirección: %s",
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getTelefono() != null ? usuario.getTelefono() : "No especificado",
                usuario.getDireccion() != null ? usuario.getDireccion() : "No especificada"
            ));

            // Llenar campos de edición
            txtNombreEdit.setText(usuario.getNombre());
            txtApellidoEdit.setText(usuario.getApellido());
            txtUsuarioEdit.setText(usuario.getUsuario());
            txtTelefonoEdit.setText(usuario.getTelefono());
            txtDireccionEdit.setText(usuario.getDireccion());
            cmbRolEdit.setSelectedItem(usuario.getRol());
            txtPasswordEdit.setText("");
            txtConfirmarPasswordEdit.setText("");
        }
    }

    private void guardarCambios() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un usuario para modificar",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idUsuario = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        User usuarioOriginal = userDAO.findById(idUsuario);
        if (usuarioOriginal == null) {
            JOptionPane.showMessageDialog(this,
                "Error al obtener los datos originales del usuario",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar campos
        if (txtNombreEdit.getText().trim().isEmpty() ||
            txtApellidoEdit.getText().trim().isEmpty() ||
            txtUsuarioEdit.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, complete los campos obligatorios (Nombre, Apellido y Usuario)",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar contraseñas si se van a cambiar
        String newPassword = new String(txtPasswordEdit.getPassword());
        String confirmPassword = new String(txtConfirmarPasswordEdit.getPassword());
        if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Las contraseñas no coinciden",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear el usuario actualizado
        User usuarioActualizado = new User(
            idUsuario,
            txtNombreEdit.getText().trim(),
            txtApellidoEdit.getText().trim(),
            txtTelefonoEdit.getText().trim(),
            txtDireccionEdit.getText().trim(),
            cmbRolEdit.getSelectedItem().toString(),
            txtUsuarioEdit.getText().trim(),
            newPassword.isEmpty() ? usuarioOriginal.getContraseña() : newPassword
        );

        // Actualizar en la base de datos
        if (userDAO.updateUser(usuarioActualizado)) {
            JOptionPane.showMessageDialog(this,
                "Usuario actualizado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            cargarUsuarios(txtBusqueda.getText().trim());
            // Actualizar detalles
            mostrarDetallesUsuario(tablaUsuarios.getSelectedRow());
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al actualizar el usuario",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarFiltros() {
        cargarUsuarios(txtBusqueda.getText().trim());
    }

    private void iniciarBusquedaDinamica() {
        if (filterTimer.isRunning()) {
            filterTimer.restart();
        } else {
            filterTimer.start();
        }
    }
}
