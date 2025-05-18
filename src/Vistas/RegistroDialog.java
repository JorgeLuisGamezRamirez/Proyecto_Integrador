package Vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.*;
import Models.User;
import Models.UserDAO;
import Vistas.Usuarios.AdminUsuariosDialog;

public class RegistroDialog extends JDialog {
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JTextField txtTelefono;
    private JTextField txtDireccion;  // Cambiado de txtCorreo a txtDireccion
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmarPassword;
    private JComboBox<String> cmbRol;
    private JCheckBox chkMostrarPassword;
    private JButton btnRegistrar;
    private JButton btnCancelar;
    private UserDAO userDAO;
    private AdminUsuariosDialog adminDialog;

    public RegistroDialog(Frame parent) {
        super(parent, "Registro de Usuario", true);
        this.adminDialog = null;
        userDAO = new UserDAO();
        setupComponents();
        setupDialog();
    }    public RegistroDialog(AdminUsuariosDialog parent) {
        super((Dialog)parent, "Registro de Usuario", true);
        this.adminDialog = parent;
        userDAO = new UserDAO();
        setupComponents();
        setupDialog();
    }

    private void setupComponents() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth();
                int h = getHeight();
                float[] dist = {0.0f, 0.5f, 1.0f};
                Color[] colors = {
                    new Color(255, 223, 186),  // Amarillo suave
                    new Color(255, 182, 193),  // Rosa claro
                    new Color(255, 20, 147)    // Fucsia
                };
                LinearGradientPaint gp = new LinearGradientPaint(0, 0, w, h, dist, colors);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        panel.setLayout(null);

        // Botón de cerrar
        JButton btnCerrar = new JButton("×");
        btnCerrar.setBounds(360, 10, 30, 30);
        btnCerrar.setFont(new Font("Arial", Font.BOLD, 18));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorder(null);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());
        btnCerrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnCerrar.setForeground(new Color(255, 182, 193));  // Rosa claro
            }
            public void mouseExited(MouseEvent e) {
                btnCerrar.setForeground(Color.WHITE);
            }
        });
        panel.add(btnCerrar);

        // Título
        JLabel lblTitle = new JLabel("Registro de Usuario", SwingConstants.CENTER);
        lblTitle.setBounds(0, 20, 400, 30);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);

        // Campos del formulario
        int yPos = 70;
        int labelWidth = 150;
        int fieldWidth = 200;
        int height = 30;
        int gap = 35; // Reducido para acomodar más campos

        // Nombre
        JLabel lblNombre = new JLabel("Nombre:", SwingConstants.RIGHT);
        lblNombre.setBounds(10, yPos, labelWidth, height);
        lblNombre.setForeground(Color.BLACK);
        panel.add(lblNombre);

        txtNombre = new JTextField();
        txtNombre.setBounds(170, yPos, fieldWidth, height);
        customizeField(txtNombre);
        panel.add(txtNombre);

        // Apellido
        JLabel lblApellido = new JLabel("Apellido:", SwingConstants.RIGHT);
        lblApellido.setBounds(10, yPos + gap, labelWidth, height);
        lblApellido.setForeground(Color.BLACK);
        panel.add(lblApellido);

        txtApellido = new JTextField();
        txtApellido.setBounds(170, yPos + gap, fieldWidth, height);
        customizeField(txtApellido);
        panel.add(txtApellido);

        // Usuario
        JLabel lblUsuario = new JLabel("Usuario:", SwingConstants.RIGHT);
        lblUsuario.setBounds(10, yPos + gap * 2, labelWidth, height);
        lblUsuario.setForeground(Color.BLACK);
        panel.add(lblUsuario);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(170, yPos + gap * 2, fieldWidth, height);
        customizeField(txtUsuario);
        panel.add(txtUsuario);

        // Teléfono
        JLabel lblTelefono = new JLabel("Teléfono:", SwingConstants.RIGHT);
        lblTelefono.setBounds(10, yPos + gap * 3, labelWidth, height);
        lblTelefono.setForeground(Color.BLACK);
        panel.add(lblTelefono);

        txtTelefono = new JTextField();
        txtTelefono.setBounds(170, yPos + gap * 3, fieldWidth, height);
        customizeField(txtTelefono);
        panel.add(txtTelefono);

        // Dirección
        JLabel lblDireccion = new JLabel("Dirección:", SwingConstants.RIGHT);
        lblDireccion.setBounds(10, yPos + gap * 4, labelWidth, height);
        lblDireccion.setForeground(Color.BLACK);
        panel.add(lblDireccion);

        txtDireccion = new JTextField();
        txtDireccion.setBounds(170, yPos + gap * 4, fieldWidth, height);
        customizeField(txtDireccion);
        panel.add(txtDireccion);

        // Rol
        JLabel lblRol = new JLabel("Rol:", SwingConstants.RIGHT);
        lblRol.setBounds(10, yPos + gap * 5, labelWidth, height);
        lblRol.setForeground(Color.BLACK);
        panel.add(lblRol);

        cmbRol = new JComboBox<>(new String[]{"Empleado", "Gerente", "Admin"});
        cmbRol.setBounds(170, yPos + gap * 5, fieldWidth, height);
        cmbRol.setBackground(Color.WHITE);
        cmbRol.setForeground(Color.BLACK);
        panel.add(cmbRol);

        // Contraseña
        JLabel lblPassword = new JLabel("Contraseña:", SwingConstants.RIGHT);
        lblPassword.setBounds(10, yPos + gap * 6, labelWidth, height);
        lblPassword.setForeground(Color.BLACK);
        panel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(170, yPos + gap * 6, fieldWidth, height);
        customizeField(txtPassword);
        panel.add(txtPassword);

        // Confirmar Contraseña
        JLabel lblConfirmar = new JLabel("Confirmar Contraseña:", SwingConstants.RIGHT);
        lblConfirmar.setBounds(10, yPos + gap * 7, labelWidth, height);
        lblConfirmar.setForeground(Color.BLACK);
        panel.add(lblConfirmar);

        txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setBounds(170, yPos + gap * 7, fieldWidth, height);
        customizeField(txtConfirmarPassword);
        panel.add(txtConfirmarPassword);

        // Checkbox mostrar contraseña
        chkMostrarPassword = new JCheckBox("Mostrar contraseña");
        chkMostrarPassword.setBounds(170, yPos + gap * 8, fieldWidth, height);
        chkMostrarPassword.setOpaque(false);
        chkMostrarPassword.setForeground(Color.BLACK);
        chkMostrarPassword.addActionListener(e -> togglePasswordVisibility());
        panel.add(chkMostrarPassword);

        // Botones
        btnRegistrar = new JButton("Registrar");
        btnRegistrar.setBounds(100, yPos + gap * 9, 100, height);
        customizeButton(btnRegistrar);
        btnRegistrar.addActionListener(e -> registrar());
        panel.add(btnRegistrar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(210, yPos + gap * 9, 100, height);
        customizeButton(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnCancelar);

        setContentPane(panel);
    }

    private void customizeField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setOpaque(true);
        field.setBackground(new Color(255, 255, 255, 220)); // Fondo blanco semi-transparente
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void togglePasswordVisibility() {
        if (chkMostrarPassword.isSelected()) {
            txtPassword.setEchoChar((char) 0);
            txtConfirmarPassword.setEchoChar((char) 0);
        } else {
            txtPassword.setEchoChar('•');
            txtConfirmarPassword.setEchoChar('•');
        }
    }

    private void setupDialog() {
        setSize(400, 500); // Aumentado para acomodar los nuevos campos
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 400, 500, 20, 20));

        // Agregar la capacidad de arrastrar la ventana
        FrameDragListener frameDragListener = new FrameDragListener(this);
        getContentPane().addMouseListener(frameDragListener);
        getContentPane().addMouseMotionListener(frameDragListener);
    }

    private void customizeButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 20, 147));  // Fucsia
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 105, 180));  // Rosa más claro
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 20, 147));  // Fucsia
            }
        });
    }

    private void registrar() {
        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() || 
            txtUsuario.getText().isEmpty() || txtTelefono.getText().isEmpty() ||
            txtDireccion.getText().isEmpty() || new String(txtPassword.getPassword()).isEmpty() || 
            new String(txtConfirmarPassword.getPassword()).isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Todos los campos son obligatorios",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!new String(txtPassword.getPassword()).equals(new String(txtConfirmarPassword.getPassword()))) {
            JOptionPane.showMessageDialog(this,
                "Las contraseñas no coinciden",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el usuario ya existe
        if (userDAO.findByUsuario(txtUsuario.getText()) != null) {
            JOptionPane.showMessageDialog(this,
                "El nombre de usuario ya está en uso",
                "Error de Registro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear y guardar el nuevo usuario
        User newUser = new User(
            txtNombre.getText(),
            txtApellido.getText(),
            txtUsuario.getText(),
            new String(txtPassword.getPassword())
        );
        newUser.setRol(cmbRol.getSelectedItem().toString());
        newUser.setTelefono(txtTelefono.getText());
        newUser.setDireccion(txtDireccion.getText());

        if (userDAO.save(newUser)) {
            JOptionPane.showMessageDialog(this,
                "Usuario registrado exitosamente",
                "Registro Exitoso",
                JOptionPane.INFORMATION_MESSAGE);
            // Si fue llamado desde AdminUsuariosDialog, actualizar la tabla
            if (adminDialog != null) {
                adminDialog.recargarUsuarios();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al registrar el usuario",
                "Error de Registro",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clase para permitir arrastrar la ventana
    public static class FrameDragListener extends MouseAdapter {
        private final JDialog frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JDialog frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }
}
