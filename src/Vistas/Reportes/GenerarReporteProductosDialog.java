package Vistas.Reportes;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.border.EmptyBorder;

public class GenerarReporteProductosDialog extends JDialog {
    private JDateChooser fechaInicio;
    private JDateChooser fechaFin;
    private JComboBox<String> formatoCombo;
    private boolean reporteGenerado = false;
    
    public GenerarReporteProductosDialog(Frame parent) {
        super(parent, "Generar Reporte de Productos", true);
        initComponents();
    }
    
    private void initComponents() {
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel de contenido
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Fecha de inicio
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Fecha de inicio:"), gbc);
        
        gbc.gridx = 1;
        fechaInicio = new JDateChooser();
        fechaInicio.setPreferredSize(new Dimension(150, 25));
        contentPanel.add(fechaInicio, gbc);
        
        // Fecha de fin
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Fecha de fin:"), gbc);
        
        gbc.gridx = 1;
        fechaFin = new JDateChooser();
        fechaFin.setPreferredSize(new Dimension(150, 25));
        contentPanel.add(fechaFin, gbc);
        
        // Formato
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Formato:"), gbc);
        
        gbc.gridx = 1;
        formatoCombo = new JComboBox<>(new String[]{"PDF", "Excel", "XML", "JSON"});
        contentPanel.add(formatoCombo, gbc);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton generarButton = new JButton("Generar");
        generarButton.addActionListener(e -> generarReporte());
        
        JButton cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(e -> dispose());
        
        buttonPanel.add(generarButton);
        buttonPanel.add(cancelarButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void generarReporte() {
        if (fechaInicio.getDate() == null || fechaFin.getDate() == null) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione ambas fechas",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (fechaInicio.getDate().after(fechaFin.getDate())) {
            JOptionPane.showMessageDialog(this,
                "La fecha de inicio no puede ser posterior a la fecha de fin",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String formato = formatoCombo.getSelectedItem().toString();
        if (formato.equals("Excel")) formato = "XLS";
        
        ReporteProductosGenerator generator = new ReporteProductosGenerator();
        generator.generarReporte(formato, fechaInicio.getDate(), fechaFin.getDate());
        
        reporteGenerado = true;
        dispose();
    }
    
    public boolean isReporteGenerado() {
        return reporteGenerado;
    }
}
