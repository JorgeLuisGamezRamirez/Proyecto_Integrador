package Vistas;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import Models.User;
import Models.UserDAO;

public class Login extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JCheckBox chkMostrarPassword;
    private JButton btnLogin;
    private JLabel lblRecuperarPassword;
    private GradientPanel mainPanel;
    private JLabel lblLogo;
    private JPanel titleBar;
    private JButton btnMinimizar;
    private JButton btnCerrar;
    private UserDAO userDAO;
    private Point dragStart = null;
    private JLabel lblRegistrar;

    public Login() {
        userDAO = new UserDAO();
        setupUIComponents();
        setupWindow();
    }

    private void setupUIComponents() {
        mainPanel = new GradientPanel();
        mainPanel.setLayout(null);

        // Barra de título personalizada
        titleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        titleBar.setOpaque(false);
        titleBar.setBounds(0, 0, 400, 35);

        // Botón minimizar
        btnMinimizar = new JButton("−");
        btnMinimizar.setForeground(Color.WHITE);
        btnMinimizar.setFont(new Font("Arial", Font.BOLD, 18));
        btnMinimizar.setPreferredSize(new Dimension(50, 35));
        btnMinimizar.setBorder(null);
        btnMinimizar.setFocusPainted(false);
        btnMinimizar.setContentAreaFilled(false);
        btnMinimizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMinimizar.addActionListener(e -> setState(Frame.ICONIFIED));
        titleBar.add(btnMinimizar);

        // Botón cerrar
        btnCerrar = new JButton("×");
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Arial", Font.BOLD, 18));
        btnCerrar.setPreferredSize(new Dimension(50, 35));
        btnCerrar.setBorder(null);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> System.exit(0));
        titleBar.add(btnCerrar);

        // Eventos hover para los botones
        setupButtonHover(btnMinimizar);
        setupButtonHover(btnCerrar);

        mainPanel.add(titleBar);

        // Logo
        try {            ImageIcon imgLogo = new ImageIcon("src/Img/Logo_Empresa.jpg");
            Image scaled = imgLogo.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblLogo = new JLabel(new ImageIcon(scaled));
            lblLogo.setBounds(140, 45, 120, 120);
            mainPanel.add(lblLogo);
        } catch (Exception e) {
            System.err.println("Error cargando el logo: " + e.getMessage());
        }

        // Título
        JLabel lblTitle = new JLabel("Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(0, 175, 400, 40);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle);

        // Labels y campos
        setupInputField("Usuario:", txtUsuario = new JTextField(), 230);
        setupInputField("Contraseña:", txtPassword = new JPasswordField(), 300);

        // Checkbox mostrar contraseña con iconos
        chkMostrarPassword = new JCheckBox();
        chkMostrarPassword.setBounds(75, 365, 35, 25);
        chkMostrarPassword.setOpaque(false);
        chkMostrarPassword.setIcon(new ImageIcon("src/Img/eye-closed.png"));
        chkMostrarPassword.setSelectedIcon(new ImageIcon("src/Img/eye-open.png"));
        chkMostrarPassword.setToolTipText("Mostrar/Ocultar contraseña");
        chkMostrarPassword.addActionListener(e -> togglePasswordVisibility());
        mainPanel.add(chkMostrarPassword);

        // Link "Recuperar contraseña"
        lblRecuperarPassword = new JLabel("Recuperar contraseña");
        lblRecuperarPassword.setBounds(75, 390, 150, 25);
        lblRecuperarPassword.setForeground(Color.BLACK);
        lblRecuperarPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRecuperarPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRecuperarPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarRecuperarPassword();
            }
        });
        mainPanel.add(lblRecuperarPassword);

        // Botón de login
        btnLogin = new JButton("LOGIN");
        btnLogin.setBounds(75, 425, 250, 45);
        customizeLoginButton(btnLogin);
        btnLogin.addActionListener(e -> autenticar());
        mainPanel.add(btnLogin);

        // Link "Registrar"
        lblRegistrar = new JLabel("Registrar nuevo usuario");
        lblRegistrar.setBounds(0, 480, 400, 25);
        lblRegistrar.setForeground(Color.BLACK);
        lblRegistrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRegistrar.setHorizontalAlignment(SwingConstants.CENTER);
        lblRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegistrar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarRegistro();
            }
            public void mouseEntered(MouseEvent e) {
                lblRegistrar.setForeground(new Color(255, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                lblRegistrar.setForeground(Color.WHITE);
            }
        });
        mainPanel.add(lblRegistrar);

        // Hacer la ventana arrastrable
        setupDraggableWindow();

        setContentPane(mainPanel);
    }

    private void setupInputField(String labelText, JTextField field, int yPos) {
        // Label
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);
        label.setBounds(75, yPos, 250, 25);
        mainPanel.add(label);

        // Campo
        field.setBounds(75, yPos + 25, 250, 35);
        customizeField(field);
        mainPanel.add(field);
    }

    private void setupButtonHover(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(255, 80, 80));
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
    }

    private void customizeLoginButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 20, 147));  // Fucsia
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 105, 180));  // Rosa más claro
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 20, 147));  // Fucsia
            }
        });
    }

    private void setupWindow() {
        setSize(400, 530);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 400, 530, 20, 20));
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
        } else {
            txtPassword.setEchoChar('•');
        }
    }

    private void mostrarRegistro() {
        RegistroDialog dialog = new RegistroDialog(this);
        dialog.setVisible(true);
    }

    private void mostrarRecuperarPassword() {
        RecuperarPasswordDialog dialog = new RecuperarPasswordDialog(this);
        dialog.setVisible(true);
    }

    private void autenticar() {
        String usuario = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor ingrese usuario y contraseña",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }        User user = userDAO.findByUsuario(usuario);        if (user != null && user.getContraseña().equals(password)) {            dispose(); // Cerrar ventana de login
            SwingUtilities.invokeLater(() -> {
                VentanaPrincipal ventana = new VentanaPrincipal(user.getRol(), usuario);
                ventana.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                "Usuario o contraseña incorrectos",
                "Error de Autenticación",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupDraggableWindow() {
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point point = e.getPoint();
                    point.x += titleBar.getX();
                    point.y += titleBar.getY();
                    dragStart = point;
                }
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    Point point = e.getPoint();
                    point.x += titleBar.getX();
                    point.y += titleBar.getY();
                    Point location = getLocation();
                    location.x += point.x - dragStart.x;
                    location.y += point.y - dragStart.y;
                    setLocation(location);
                    dragStart = point;
                }
            }
        });
    }

    // Panel con gradiente para el fondo
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Definir los colores del gradiente del logo
            Color color1 = new Color(255, 223, 186);  // Amarillo suave
            Color color2 = new Color(255, 182, 193);  // Rosa claro
            Color color3 = new Color(255, 20, 147);   // Fucsia
            
            // Crear el gradiente de tres colores
            int w = getWidth();
            int h = getHeight();
            float[] dist = {0.0f, 0.5f, 1.0f};
            Color[] colors = {color1, color2, color3};
            LinearGradientPaint gp = new LinearGradientPaint(0, 0, w, h, dist, colors);
            
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }
        
        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            login.setVisible(true);
        });
    }
}
