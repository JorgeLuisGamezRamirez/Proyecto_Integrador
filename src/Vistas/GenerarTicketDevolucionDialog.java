package Vistas;

import Models.TicketDevolucion;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class GenerarTicketDevolucionDialog extends JDialog {
    private TicketDevolucion ticketDevolucion;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private JTextArea txtTicket;

    public GenerarTicketDevolucionDialog(Frame parent, TicketDevolucion ticketDevolucion) {
        super(parent, "Ticket de Devolución", true);
        this.ticketDevolucion = ticketDevolucion;
        setupComponents();
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Crear el contenido del ticket
        StringBuilder ticketContent = new StringBuilder();
        ticketContent.append("============================================\n");
        ticketContent.append("            TICKET DE DEVOLUCIÓN            \n");
        ticketContent.append("============================================\n\n");
        ticketContent.append(String.format("Ticket Devolución: %s\n", ticketDevolucion.getNumeroTicketDevolucion()));
        ticketContent.append(String.format("Ticket Original: %s\n", ticketDevolucion.getNumeroTicketOriginal()));
        ticketContent.append(String.format("Fecha: %s\n", sdf.format(ticketDevolucion.getFecha())));
        ticketContent.append(String.format("Atendido por: %s\n", ticketDevolucion.getNombreEmpleado()));
        ticketContent.append(String.format("Cliente: %s\n\n", ticketDevolucion.getNombreCliente()));
        
        ticketContent.append("--------------------------------------------\n");
        ticketContent.append("PRODUCTOS DEVUELTOS:\n");
        ticketContent.append("--------------------------------------------\n");
        
        for (Models.DetalleDevolucion detalle : ticketDevolucion.getDetalles()) {
            ticketContent.append(String.format(
                "%-35s\n%dx%s\n", 
                detalle.getNombreProducto(),
                detalle.getCantidad(),
                formatoMoneda.format(detalle.getPrecioUnitario())
            ));
            ticketContent.append(String.format(
                "Subtotal: %s\n", 
                formatoMoneda.format(detalle.getCantidad() * detalle.getPrecioUnitario())
            ));
            ticketContent.append("--------------------------------------------\n");
        }

        ticketContent.append(String.format("\nTOTAL DEVUELTO: %s\n\n", formatoMoneda.format(ticketDevolucion.getTotalDevuelto())));
        ticketContent.append("Motivo de la devolución:\n");
        ticketContent.append(ticketDevolucion.getMotivo()).append("\n\n");
        
        ticketContent.append("============================================\n");
        ticketContent.append("          ¡Gracias por su visita!          \n");
        ticketContent.append("============================================\n");        // Crear el área de texto con el contenido del ticket
        txtTicket = new JTextArea(ticketContent.toString());
        txtTicket.setEditable(false);
        txtTicket.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtTicket.setBackground(Color.WHITE);

        // Agregar el área de texto a un scroll pane
        JScrollPane scrollPane = new JScrollPane(txtTicket);
        scrollPane.setPreferredSize(new Dimension(400, 500));
        mainPanel.add(scrollPane);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> imprimirTicket());

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        buttonPanel.add(btnImprimir);
        buttonPanel.add(btnCerrar);
        mainPanel.add(buttonPanel);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void imprimirTicket() {
        try {
            txtTicket.print();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al imprimir el ticket: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
