package Vistas.Reportes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;

public class ReporteProductosProgressDialog extends JDialog {
    private SwingWorker<Void, Void> worker;

    public ReporteProductosProgressDialog(Frame parent) {
        super(parent, "Reporte de Productos", true);
        initComponents();
        iniciarGeneracion();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Icono de información
        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        panel.add(iconLabel, BorderLayout.WEST);

        // Mensaje
        JLabel messageLabel = new JLabel("Generando reporte de productos...");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(messageLabel, BorderLayout.CENTER);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Manejar el cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (worker != null) {
                    worker.cancel(true);
                }
                dispose();
            }
        });
    }

    private void iniciarGeneracion() {
        worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ReporteProductosGenerator generator = new ReporteProductosGenerator();
                
                // Obtener fechas para el último mes
                Calendar cal = Calendar.getInstance();
                Date fechaFin = cal.getTime();
                cal.add(Calendar.MONTH, -1);
                Date fechaInicio = cal.getTime();
                
                // Generar reporte en PDF por defecto
                generator.generarReporte("PDF", fechaInicio, fechaFin);
                return null;
            }

            @Override
            protected void done() {
                dispose();
            }
        };
        worker.execute();
    }

    public static void mostrarReporte(Frame parent) {
        ReporteProductosProgressDialog dialog = new ReporteProductosProgressDialog(parent);
        dialog.setVisible(true);
    }
}
