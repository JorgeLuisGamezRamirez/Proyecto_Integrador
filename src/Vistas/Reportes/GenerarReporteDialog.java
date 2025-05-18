package Vistas.Reportes;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class GenerarReporteDialog extends JDialog {
    private JComboBox<String> periodoCombo;
    private JComboBox<String> formatoCombo;
    private JDateChooser fechaInicio;
    private JDateChooser fechaFin;
    private boolean confirmed = false;

    public GenerarReporteDialog(Frame parent) {
        super(parent, "Generar Reporte", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal con GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Periodo
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Período:"), gbc);

        String[] periodos = {"Hoy", "Esta semana", "Este mes", "Histórico", "Personalizado"};
        periodoCombo = new JComboBox<>(periodos);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(periodoCombo, gbc);

        // Fechas
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Fecha inicio:"), gbc);

        fechaInicio = new JDateChooser();
        fechaInicio.setEnabled(false);
        gbc.gridx = 1;
        mainPanel.add(fechaInicio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Fecha fin:"), gbc);

        fechaFin = new JDateChooser();
        fechaFin.setEnabled(false);
        gbc.gridx = 1;
        mainPanel.add(fechaFin, gbc);

        // Formato
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Formato:"), gbc);

        String[] formatos = {"PDF", "XML", "JSON"};
        formatoCombo = new JComboBox<>(formatos);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        mainPanel.add(formatoCombo, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Generar");
        JButton cancelButton = new JButton("Cancelar");

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Agregar los paneles al diálogo
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event Listeners
        periodoCombo.addActionListener(e -> actualizarFechas());
        
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());

        // Configuración final del diálogo
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Inicializar las fechas
        actualizarFechas();
    }

    private void actualizarFechas() {
        boolean esPersonalizado = "Personalizado".equals(periodoCombo.getSelectedItem());
        fechaInicio.setEnabled(esPersonalizado);
        fechaFin.setEnabled(esPersonalizado);

        if (!esPersonalizado) {
            LocalDate inicio = LocalDate.now();
            LocalDate fin = LocalDate.now();

            switch (periodoCombo.getSelectedItem().toString()) {
                case "Hoy":
                    // Inicio y fin son el día actual
                    break;
                case "Esta semana":
                    inicio = inicio.with(DayOfWeek.MONDAY);
                    fin = fin.with(DayOfWeek.SUNDAY);
                    break;
                case "Este mes":
                    inicio = inicio.withDayOfMonth(1);
                    fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
                    break;
                case "Histórico":
                    inicio = LocalDate.of(2000, 1, 1); // Una fecha muy anterior
                    break;
            }

            fechaInicio.setDate(java.sql.Date.valueOf(inicio));
            fechaFin.setDate(java.sql.Date.valueOf(fin));
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Date getFechaInicio() {
        return fechaInicio.getDate();
    }

    public Date getFechaFin() {
        return fechaFin.getDate();
    }

    public String getFormato() {
        return formatoCombo.getSelectedItem().toString();
    }

    public String getPeriodo() {
        return periodoCombo.getSelectedItem().toString();
    }
}
