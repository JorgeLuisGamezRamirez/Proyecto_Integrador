package main;

import Vistas.Login;
import basedatos.Conexion;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;


public class Main {
    public static void main(String[] args) {
        // Crear y mostrar el splash screen
        JWindow splash = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(255, 255, 255));
        
        // Cargar y mostrar el logo
        try {            ImageIcon imgLogo = new ImageIcon("src/Img/Logo_Empresa.jpg");
            Image scaled = imgLogo.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(scaled));
            content.add(logo, BorderLayout.CENTER);
            
            // Agregar texto de carga
            JLabel loading = new JLabel("Iniciando aplicación...", SwingConstants.CENTER);
            loading.setFont(new Font("Segoe UI", Font.BOLD, 14));
            content.add(loading, BorderLayout.SOUTH);
            
            splash.setContentPane(content);
            splash.pack();
            splash.setLocationRelativeTo(null);
            splash.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error cargando el logo: " + e.getMessage());
        }

        // Configurar el look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Verificar la conexión a la base de datos
        Connection conn = Conexion.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo establecer la conexión con la base de datos.\nPor favor, verifique la configuración.", 
                "Error de Conexión", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Conexion.closeConnection(conn);        // Iniciar la interfaz de usuario en el EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Esperar un momento para mostrar el splash screen
                Thread.sleep(2000);
                splash.dispose();
                
                // Mostrar la ventana de login
                Login login = new Login();
                login.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar la aplicación: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
