package Vistas;

import Models.Ticket;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class GenerarTicketDialog extends JDialog implements Printable {
    private Ticket ticket;
    private JTextArea txtTicket;
    private String contenidoTicket;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public GenerarTicketDialog(Frame parent, Ticket ticket) {
        super(parent, "Ticket de Venta", true);
        this.ticket = ticket;
        setupComponents();
        generarContenidoTicket();
        setupDialog();
    }

    private void setupComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Área de texto para mostrar el ticket
        txtTicket = new JTextArea();
        txtTicket.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtTicket.setEditable(false);
        txtTicket.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(txtTicket);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.addActionListener(e -> imprimirTicket());
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnImprimir);
        buttonPanel.add(btnCerrar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generarContenidoTicket() {        StringBuilder sb = new StringBuilder();
        
        // Encabezado
        sb.append("             OBKG STORE            \\n");
        sb.append("==================================\\n");
        sb.append("Ticket: ").append(ticket.getNumeroTicket()).append("\\n");
        sb.append("Fecha: ").append(sdf.format(ticket.getFecha())).append("\\n");
        sb.append("Cliente: ").append(ticket.getNombreCliente() != null ? ticket.getNombreCliente() : "Cliente General").append("\\n");
        sb.append("Le atendió: ").append(ticket.getNombreEmpleado() != null ? ticket.getNombreEmpleado() : "Empleado").append("\\n");
        sb.append("==================================\\n\\n");
        
        // Detalles de productos
        sb.append("PRODUCTOS:\\n");
        sb.append("----------------------------------\\n");
        for (Ticket.DetalleTicket detalle : ticket.getDetalles()) {
            sb.append(String.format("%-20s\\n", detalle.getNombreProducto()));
            sb.append(String.format("%3d x %8s = %10s\\n", 
                detalle.getCantidad(),
                formatoMoneda.format(detalle.getPrecioUnitario()),
                formatoMoneda.format(detalle.getSubtotal())));
            if (detalle.getDescuentoUnitario() > 0) {
                sb.append(String.format("    Descuento: %17s\\n", 
                    formatoMoneda.format(detalle.getDescuentoUnitario() * detalle.getCantidad())));
            }
        }
        
        // Totales
        sb.append("\\n==================================\\n");
        sb.append(String.format("Subtotal: %22s\\n", formatoMoneda.format(ticket.getSubtotal())));
        sb.append(String.format("Impuestos: %21s\\n", formatoMoneda.format(ticket.getImpuestos())));
        sb.append(String.format("Total: %25s\\n", formatoMoneda.format(ticket.getTotal())));
        sb.append("==================================\\n");
        sb.append("Método de pago: ").append(ticket.getMetodoPago()).append("\\n\\n");
        
        // Pie de ticket
        sb.append("       ¡Gracias por su compra!      \\n");
        sb.append("         Vuelva pronto :)          \\n");
        
        contenidoTicket = sb.toString().replace("\\n", "\n");
        txtTicket.setText(contenidoTicket);
    }

    private void setupDialog() {
        setSize(400, 600);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void imprimirTicket() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + e.getMessage(),
                    "Error de impresión",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));

        float lineHeight = g2d.getFontMetrics().getHeight();
        float y = 0;

        for (String line : contenidoTicket.split("\\n")) {
            y += lineHeight;
            g2d.drawString(line, 0, y);
        }

        return PAGE_EXISTS;
    }
}
