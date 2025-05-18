package Vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;

public class PagoEfectivoDialog extends JDialog {    private JTextField txtEfectivo;
    private JTextField txtNombreCliente;
    private JLabel lblCambio;
    private JButton btnPagar;
    private JButton btnCancelar;
    private double totalPagar;
    private boolean pagoConfirmado = false;
    private String nombreCliente;

    public PagoEfectivoDialog(Frame parent, double total) {
        super(parent, "Pago en Efectivo", true);
        this.totalPagar = total;
        setupComponents();
        setupDialog();
    }

    private void setupComponents() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
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
        panel.add(btnCerrar);

        // Título
        JLabel lblTitle = new JLabel("Pago en Efectivo", SwingConstants.CENTER);
        lblTitle.setBounds(0, 20, 400, 30);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);

        // Total a Pagar
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        JLabel lblTotal = new JLabel("Total a Pagar: " + formatoMoneda.format(totalPagar), SwingConstants.CENTER);
        lblTotal.setBounds(0, 60, 400, 30);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(Color.BLACK);
        panel.add(lblTotal);

        // Nombre del Cliente
        JLabel lblNombreCliente = new JLabel("Nombre del Cliente:", SwingConstants.RIGHT);
        lblNombreCliente.setBounds(20, 100, 150, 30);
        lblNombreCliente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNombreCliente.setForeground(Color.BLACK);
        panel.add(lblNombreCliente);

        txtNombreCliente = new JTextField();
        txtNombreCliente.setBounds(180, 100, 180, 30);
        customizeField(txtNombreCliente);
        panel.add(txtNombreCliente);

        // Efectivo Recibido
        JLabel lblEfectivo = new JLabel("Efectivo Recibido:", SwingConstants.RIGHT);
        lblEfectivo.setBounds(20, 140, 150, 30);
        lblEfectivo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblEfectivo.setForeground(Color.BLACK);
        panel.add(lblEfectivo);

        txtEfectivo = new JTextField();
        txtEfectivo.setBounds(180, 140, 180, 30);
        customizeField(txtEfectivo);
        txtEfectivo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
        });
        panel.add(txtEfectivo);

        // Cambio
        JLabel lblCambioText = new JLabel("Cambio:", SwingConstants.RIGHT);
        lblCambioText.setBounds(20, 180, 150, 30);
        lblCambioText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCambioText.setForeground(Color.BLACK);
        panel.add(lblCambioText);

        lblCambio = new JLabel("$0.00", SwingConstants.LEFT);
        lblCambio.setBounds(180, 180, 180, 30);
        lblCambio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCambio.setForeground(new Color(0, 100, 0));
        panel.add(lblCambio);

        // Botones
        btnPagar = new JButton("PAGAR");
        btnPagar.setBounds(100, 230, 100, 35); // Aumentado altura
        customizeButton(btnPagar);
        btnPagar.addActionListener(e -> procesarPago());
        panel.add(btnPagar);

        btnCancelar = new JButton("CANCELAR");
        btnCancelar.setBounds(210, 230, 100, 35); // Aumentado altura
        customizeButton(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());
        panel.add(btnCancelar);

        setContentPane(panel);
    }

    private void setupDialog() {
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 400, 300, 20, 20)); // Ajustado altura
    }

    private void customizeField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setOpaque(true);
        field.setBackground(new Color(255, 255, 255, 200)); // Más transparente
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 20, 147), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.putClientProperty("JTextField.placeholderText", field == txtNombreCliente ? "Ingrese el nombre" : "0.00");
    }

    private void customizeButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 20, 147));  // Fucsia
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(255, 105, 180));  // Rosa más claro
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(255, 20, 147));  // Fucsia
            }
        });
    }    private void calcularCambio() {
        if (txtEfectivo.getText().trim().isEmpty()) {
            lblCambio.setText("$0.00");
            lblCambio.setForeground(Color.BLACK);
            btnPagar.setEnabled(false);
            return;
        }

        try {
            double efectivoRecibido = Double.parseDouble(txtEfectivo.getText().trim());
            double cambio = efectivoRecibido - totalPagar;
            NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

            if (efectivoRecibido < totalPagar) {
                double faltante = totalPagar - efectivoRecibido;
                lblCambio.setText("Falta: " + formatoMoneda.format(faltante));
                lblCambio.setForeground(new Color(255, 0, 0)); // Rojo
                btnPagar.setEnabled(false);
            } else {
                lblCambio.setText(formatoMoneda.format(cambio));
                lblCambio.setForeground(new Color(0, 128, 0)); // Verde oscuro
                btnPagar.setEnabled(true);
            }
        } catch (NumberFormatException e) {
            lblCambio.setText("Monto inválido");
            lblCambio.setForeground(new Color(255, 69, 0)); // Rojo-naranja
            btnPagar.setEnabled(false);
        }
    }private void procesarPago() {
        String nombreCliente = txtNombreCliente.getText().trim();
        if (nombreCliente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingrese el nombre del cliente",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            txtNombreCliente.requestFocus();
            return;
        }

        try {
            double efectivoRecibido = Double.parseDouble(txtEfectivo.getText().trim());
            if (efectivoRecibido < totalPagar) {
                JOptionPane.showMessageDialog(this,
                    "El efectivo recibido es insuficiente",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.nombreCliente = nombreCliente;
            pagoConfirmado = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingrese un monto válido",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPagoConfirmado() {
        return pagoConfirmado;
    }    public double getEfectivoRecibido() {
        try {
            return Double.parseDouble(txtEfectivo.getText().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getNombreCliente() {
        return nombreCliente;
    }
}
