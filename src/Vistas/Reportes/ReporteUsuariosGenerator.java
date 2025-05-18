package Vistas.Reportes;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import basedatos.Conexion;
import java.util.Date;
import java.util.Calendar;

public class ReporteUsuariosGenerator {
    
    public void generarReporte(Date fechaInicio, Date fechaFin, String formato, Integer idEmpleado) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Usuario");
        
        String extension = formato.toLowerCase();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            formato.toUpperCase() + " Files", extension));
            
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith("." + extension)) {
                file = new File(file.getAbsolutePath() + "." + extension);
            }
            
            try {
                switch (formato.toUpperCase()) {
                    case "PDF":
                        generarPDF(file, fechaInicio, fechaFin, idEmpleado);
                        break;
                    case "XML":
                        generarXML(file, fechaInicio, fechaFin, idEmpleado);
                        break;
                    case "JSON":
                        generarJSON(file, fechaInicio, fechaFin, idEmpleado);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Error al generar el reporte: " + e.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void generarPDF(File file, Date fechaInicio, Date fechaFin, Integer idEmpleado) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Obtener información del empleado
        String nombreEmpleado = "Todos los empleados";
        if (idEmpleado != null) {
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo FROM Empleados WHERE id_empleado = ?")) {
                stmt.setInt(1, idEmpleado);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nombreEmpleado = rs.getString("nombre_completo");
                }
            }
        }
        
        Paragraph titulo = new Paragraph("Reporte de Ventas por Usuario")
            .setBold()
            .setFontSize(18)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);
        
        Paragraph empleado = new Paragraph("Empleado: " + nombreEmpleado)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(empleado);
            
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Paragraph periodo = new Paragraph("Período: " + sdf.format(fechaInicio) + " - " + sdf.format(fechaFin))
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(periodo);
        
        float[] columnWidths = {2, 3, 4, 2, 2, 2};
        Table tabla = new Table(UnitValue.createPercentArray(columnWidths));
        tabla.setWidth(UnitValue.createPercentValue(100));
        
        tabla.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("N° Ticket").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Precio Unit.").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));
        
        StringBuilder sql = new StringBuilder(
            "SELECT v.fecha, v.numero_ticket, p.nombre, dv.cantidad, dv.precio_unitario, " +
            "(dv.cantidad * dv.precio_unitario - ISNULL(dv.descuento_unitario, 0)) as subtotal " +
            "FROM Ventas v " +
            "INNER JOIN DetallesVenta dv ON v.id_venta = dv.id_venta " +
            "INNER JOIN Productos p ON dv.id_producto = p.id_producto " +
            "WHERE v.estado = 'COMPLETADA' " +
            "AND CONVERT(date, v.fecha) BETWEEN CONVERT(date, ?) AND CONVERT(date, ?) "
        );
        
        if (idEmpleado != null) {
            sql.append("AND v.id_empleado = ? ");
        }
        sql.append("ORDER BY v.fecha DESC, v.numero_ticket");
                    
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Configurar fechas
            Calendar cal = Calendar.getInstance();
            
            cal.setTime(fechaInicio);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            stmt.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            
            cal.setTime(fechaFin);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            stmt.setTimestamp(2, new Timestamp(cal.getTimeInMillis()));
            
            if (idEmpleado != null) {
                stmt.setInt(3, idEmpleado);
            }
            
            ResultSet rs = stmt.executeQuery();
            double totalVentas = 0;
            SimpleDateFormat sdfCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String ticketActual = "";
            int ventasCount = 0;
            
            while (rs.next()) {
                String numeroTicket = rs.getString("numero_ticket");
                if (!numeroTicket.equals(ticketActual)) {
                    ventasCount++;
                    ticketActual = numeroTicket;
                }
                
                tabla.addCell(new Cell().add(new Paragraph(sdfCompleto.format(rs.getTimestamp("fecha")))
                    .setFontSize(10)));
                tabla.addCell(new Cell().add(new Paragraph(numeroTicket)));
                tabla.addCell(new Cell().add(new Paragraph(rs.getString("nombre"))));
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(rs.getInt("cantidad")))
                    .setTextAlignment(TextAlignment.RIGHT)));
                tabla.addCell(new Cell().add(new Paragraph(String.format("$%.2f", rs.getDouble("precio_unitario")))
                    .setTextAlignment(TextAlignment.RIGHT)));
                
                double subtotal = rs.getDouble("subtotal");
                totalVentas += subtotal;
                tabla.addCell(new Cell().add(new Paragraph(String.format("$%.2f", subtotal))
                    .setTextAlignment(TextAlignment.RIGHT)));
            }
            
            if (ventasCount == 0) {
                tabla.addCell(new Cell(1, 6)
                    .add(new Paragraph("No se encontraron ventas en el período seleccionado")
                    .setTextAlignment(TextAlignment.CENTER)));
            }
            
            document.add(tabla);
            
            // Agregar resumen
            document.add(new Paragraph("\nResumen")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.LEFT));
            
            document.add(new Paragraph("Total de ventas realizadas: " + ventasCount)
                .setFontSize(12));
            
            document.add(new Paragraph("Total monetario: $" + String.format("%.2f", totalVentas))
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT));
        }
        
        document.close();
    }
    
    private void generarXML(File file, Date fechaInicio, Date fechaFin, Integer idEmpleado) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        org.w3c.dom.Document doc = docBuilder.newDocument();
        org.w3c.dom.Element rootElement = doc.createElement("reporte_ventas_usuario");
        doc.appendChild(rootElement);
        
        // Agregar información del empleado
        if (idEmpleado != null) {
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo FROM Empleados WHERE id_empleado = ?")) {
                stmt.setInt(1, idEmpleado);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    org.w3c.dom.Element empleado = doc.createElement("empleado");
                    empleado.setTextContent(rs.getString("nombre_completo"));
                    rootElement.appendChild(empleado);
                }
            }
        }
        
        org.w3c.dom.Element periodo = doc.createElement("periodo");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        periodo.setAttribute("inicio", sdf.format(fechaInicio));
        periodo.setAttribute("fin", sdf.format(fechaFin));
        rootElement.appendChild(periodo);
        
        StringBuilder sql = new StringBuilder(
            "SELECT v.fecha, v.numero_ticket, p.nombre, dv.cantidad, dv.precio_unitario, " +
            "(dv.cantidad * dv.precio_unitario - ISNULL(dv.descuento_unitario, 0)) as subtotal " +
            "FROM Ventas v " +
            "INNER JOIN DetallesVenta dv ON v.id_venta = dv.id_venta " +
            "INNER JOIN Productos p ON dv.id_producto = p.id_producto " +
            "WHERE v.estado = 'COMPLETADA' " +
            "AND CONVERT(date, v.fecha) BETWEEN CONVERT(date, ?) AND CONVERT(date, ?) "
        );
        
        if (idEmpleado != null) {
            sql.append("AND v.id_empleado = ? ");
        }
        sql.append("ORDER BY v.fecha DESC, v.numero_ticket");
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            Calendar cal = Calendar.getInstance();
            
            cal.setTime(fechaInicio);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            stmt.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            
            cal.setTime(fechaFin);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            stmt.setTimestamp(2, new Timestamp(cal.getTimeInMillis()));
            
            if (idEmpleado != null) {
                stmt.setInt(3, idEmpleado);
            }
            
            ResultSet rs = stmt.executeQuery();
            double totalVentas = 0;
            int ventasCount = 0;
            String ticketActual = "";
            
            org.w3c.dom.Element ventas = doc.createElement("ventas");
            rootElement.appendChild(ventas);
            
            while (rs.next()) {
                String numeroTicket = rs.getString("numero_ticket");
                if (!numeroTicket.equals(ticketActual)) {
                    ventasCount++;
                    ticketActual = numeroTicket;
                }
                
                org.w3c.dom.Element venta = doc.createElement("venta");
                venta.setAttribute("fecha", sdf.format(rs.getTimestamp("fecha")));
                venta.setAttribute("ticket", numeroTicket);
                
                org.w3c.dom.Element producto = doc.createElement("producto");
                producto.setTextContent(rs.getString("nombre"));
                venta.appendChild(producto);
                
                org.w3c.dom.Element cantidad = doc.createElement("cantidad");
                cantidad.setTextContent(String.valueOf(rs.getInt("cantidad")));
                venta.appendChild(cantidad);
                
                org.w3c.dom.Element precioUnit = doc.createElement("precio_unitario");
                precioUnit.setTextContent(String.format("%.2f", rs.getDouble("precio_unitario")));
                venta.appendChild(precioUnit);
                
                org.w3c.dom.Element subtotal = doc.createElement("subtotal");
                double subtotalValue = rs.getDouble("subtotal");
                totalVentas += subtotalValue;
                subtotal.setTextContent(String.format("%.2f", subtotalValue));
                venta.appendChild(subtotal);
                
                ventas.appendChild(venta);
            }
            
            org.w3c.dom.Element resumen = doc.createElement("resumen");
            
            org.w3c.dom.Element totalVentasElement = doc.createElement("total_ventas");
            totalVentasElement.setTextContent(String.valueOf(ventasCount));
            resumen.appendChild(totalVentasElement);
            
            org.w3c.dom.Element totalMonetario = doc.createElement("total_monetario");
            totalMonetario.setTextContent(String.format("%.2f", totalVentas));
            resumen.appendChild(totalMonetario);
            
            rootElement.appendChild(resumen);
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
    
    private void generarJSON(File file, Date fechaInicio, Date fechaFin, Integer idEmpleado) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        // Agregar información del empleado
        if (idEmpleado != null) {
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo FROM Empleados WHERE id_empleado = ?")) {
                stmt.setInt(1, idEmpleado);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    rootNode.put("empleado", rs.getString("nombre_completo"));
                }
            }
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        ObjectNode periodoNode = mapper.createObjectNode();
        periodoNode.put("inicio", sdf.format(fechaInicio));
        periodoNode.put("fin", sdf.format(fechaFin));
        rootNode.set("periodo", periodoNode);
        
        StringBuilder sql = new StringBuilder(
            "SELECT v.fecha, v.numero_ticket, p.nombre, dv.cantidad, dv.precio_unitario, " +
            "(dv.cantidad * dv.precio_unitario - ISNULL(dv.descuento_unitario, 0)) as subtotal " +
            "FROM Ventas v " +
            "INNER JOIN DetallesVenta dv ON v.id_venta = dv.id_venta " +
            "INNER JOIN Productos p ON dv.id_producto = p.id_producto " +
            "WHERE v.estado = 'COMPLETADA' " +
            "AND CONVERT(date, v.fecha) BETWEEN CONVERT(date, ?) AND CONVERT(date, ?) "
        );
        
        if (idEmpleado != null) {
            sql.append("AND v.id_empleado = ? ");
        }
        sql.append("ORDER BY v.fecha DESC, v.numero_ticket");
                    
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            Calendar cal = Calendar.getInstance();
            
            cal.setTime(fechaInicio);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            stmt.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            
            cal.setTime(fechaFin);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            stmt.setTimestamp(2, new Timestamp(cal.getTimeInMillis()));
            
            if (idEmpleado != null) {
                stmt.setInt(3, idEmpleado);
            }
            
            ResultSet rs = stmt.executeQuery();
            ArrayNode ventasArray = mapper.createArrayNode();
            double totalVentas = 0;
            int ventasCount = 0;
            String ticketActual = "";
            
            while (rs.next()) {
                String numeroTicket = rs.getString("numero_ticket");
                if (!numeroTicket.equals(ticketActual)) {
                    ventasCount++;
                    ticketActual = numeroTicket;
                }
                
                ObjectNode ventaNode = mapper.createObjectNode();
                ventaNode.put("fecha", sdf.format(rs.getTimestamp("fecha")));
                ventaNode.put("ticket", numeroTicket);
                ventaNode.put("producto", rs.getString("nombre"));
                ventaNode.put("cantidad", rs.getInt("cantidad"));
                ventaNode.put("precio_unitario", rs.getDouble("precio_unitario"));
                
                double subtotal = rs.getDouble("subtotal");
                totalVentas += subtotal;
                ventaNode.put("subtotal", subtotal);
                
                ventasArray.add(ventaNode);
            }
            
            rootNode.set("ventas", ventasArray);
            
            ObjectNode resumenNode = mapper.createObjectNode();
            resumenNode.put("total_ventas", ventasCount);
            resumenNode.put("total_monetario", totalVentas);
            rootNode.set("resumen", resumenNode);
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
    }
}
