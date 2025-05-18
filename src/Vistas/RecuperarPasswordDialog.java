package Vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.*;
import Models.User;
import Models.UserDAO;

public class RecuperarPasswordDialog extends JDialog {
    private JTextField txtUsuario;
    private JTextField txtTelefono;
    private JButton btnRecuperar;
    private JButton btnCancelar;

    public RecuperarPasswordDialog(Frame parent) {
        super(parent, "Recuperar Contraseña", true);
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
                
                // Gradiente de tres colores
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
        JLabel lblTitle = new JLabel("Recuperar Contraseña", SwingConstants.CENTER);
        lblTitle.setBounds(0, 20, 400, 30);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);

        // Usuario
        JLabel lblUsuario = new JLabel("Usuario:", SwingConstants.RIGHT);
        lblUsuario.setBounds(20, 80, 100, 30);
        panel.add(lblUsuario);

        txtUsuario = new JTextField();
        txtUsuario.setBounds(130, 80, 220, 30);
        panel.add(txtUsuario);

        // Teléfono
        JLabel lblTelefono = new JLabel("Teléfono:", SwingConstants.RIGHT);
        lblTelefono.setBounds(20, 120, 100, 30);
        panel.add(lblTelefono);

        txtTelefono = new JTextField();
        txtTelefono.setBounds(130, 120, 220, 30);
        panel.add(txtTelefono);

        // Botones
        btnRecuperar = new JButton("Recuperar");
        btnRecuperar.setBounds(100, 180, 100, 30);
        customizeButton(btnRecuperar);
        btnRecuperar.addActionListener(e -> recuperar());
        panel.add(btnRecuperar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(210, 180, 100, 30);
        customizeButton(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnCancelar);

        // Personalizar campos
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                field.setOpaque(true);
                field.setBackground(new Color(255, 255, 255, 220)); // Fondo blanco semi-transparente
                field.setForeground(Color.BLACK);
                field.setCaretColor(Color.BLACK);
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            } else if (comp instanceof JLabel && comp != lblTitle) {
                JLabel label = (JLabel) comp;
                label.setForeground(Color.BLACK);
            }
        }

        setContentPane(panel);
    }

    private void setupDialog() {
        setSize(400, 250);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 400, 250, 20, 20));

        // Agregar la capacidad de arrastrar la ventana
        FrameDragListener frameDragListener = new FrameDragListener(this);
        getContentPane().addMouseListener(frameDragListener);
        getContentPane().addMouseMotionListener(frameDragListener);
    }

    private void recuperar() {
        if (txtUsuario.getText().isEmpty() || txtTelefono.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor complete todos los campos",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear una nueva instancia de UserDAO para asegurar una consulta fresca
        UserDAO dao = new UserDAO();
        String usuario = txtUsuario.getText().trim();
        String telefono = txtTelefono.getText().trim();
        
        User user = dao.findByUsuario(usuario);
        if (user == null) {
            JOptionPane.showMessageDialog(this,
                "Usuario no encontrado",
                "Error de Recuperación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!user.getTelefono().equals(telefono)) {
            JOptionPane.showMessageDialog(this,
                "El teléfono no coincide con el registrado",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mostrar la contraseña actual del usuario - sin generar una nueva
        JOptionPane.showMessageDialog(this,
            "Su contraseña es: " + user.getContraseña(),
            "Recuperación Exitosa",
            JOptionPane.INFORMATION_MESSAGE);
        dispose();
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
