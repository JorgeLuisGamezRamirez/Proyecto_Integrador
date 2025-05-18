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

public class ReporteDevolucionesGenerator {
    
    public void generarReporte(Date fechaInicio, Date fechaFin, String formato) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Devoluciones");
        
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
                        generarPDF(file, fechaInicio, fechaFin);
                        break;
                    case "XML":
                        generarXML(file, fechaInicio, fechaFin);
                        break;
                    case "JSON":
                        generarJSON(file, fechaInicio, fechaFin);
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
    
    private void generarPDF(File file, Date fechaInicio, Date fechaFin) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Título del reporte
        Paragraph titulo = new Paragraph("Reporte de Devoluciones")
            .setBold()
            .setFontSize(18)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);
            
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Paragraph periodo = new Paragraph("Período: " + sdf.format(fechaInicio) + " - " + sdf.format(fechaFin))
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(periodo);
        
        // Crear tabla con columnas para los detalles de la devolución
        float[] columnWidths = {2, 2, 3, 4, 2, 2, 2};
        Table tabla = new Table(UnitValue.createPercentArray(columnWidths));
        tabla.setWidth(UnitValue.createPercentValue(100));
        
        // Encabezados de la tabla
        tabla.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Ticket Dev.").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Ticket Original").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Precio Unit.").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));
        
        String sql = "SELECT d.fecha, d.numero_ticket, v.numero_ticket as ticket_original, " +
                    "p.nombre as producto, dd.cantidad, dd.precio_unitario, " +
                    "dd.subtotal, d.motivo, CONCAT(e.nombre, ' ', e.apellido) as empleado " +
                    "FROM Devoluciones d " +
                    "INNER JOIN DetallesDevolucion dd ON d.id_devolucion = dd.id_devolucion " +
                    "INNER JOIN Productos p ON dd.id_producto = p.id_producto " +
                    "INNER JOIN Ventas v ON d.id_venta = v.id_venta " +
                    "INNER JOIN Empleados e ON v.id_empleado = e.id_empleado " +
                    "WHERE CONVERT(date, d.fecha) BETWEEN CONVERT(date, ?) AND CONVERT(date, ?) " +
                    "AND d.estado = 'COMPLETADA' " +
                    "ORDER BY d.fecha DESC, d.numero_ticket";
                    
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
            
            ResultSet rs = stmt.executeQuery();
            double totalDevoluciones = 0;
            SimpleDateFormat sdfCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String ticketActual = "";
            int devolucionesCount = 0;
            String empleadoActual = "";
            String motivoActual = "";
            
            while (rs.next()) {
                String numeroTicket = rs.getString("numero_ticket");
                String empleado = rs.getString("empleado");
                String motivo = rs.getString("motivo");
                
                // Si cambia el ticket, agregar información de la devolución
                if (!numeroTicket.equals(ticketActual)) {
                    if (!ticketActual.isEmpty()) {
                        // Agregar línea separadora entre devoluciones
                        tabla.addCell(new Cell(1, 7)
                            .setBorder(null)
                            .add(new Paragraph(" ")));
                    }
                    
                    // Agregar información del empleado y motivo
                    tabla.addCell(new Cell(1, 7)
                        .add(new Paragraph("Empleado: " + empleado + " - Motivo: " + motivo)
                        .setFontSize(10)
                        .setItalic()));
                    
                    devolucionesCount++;
                    ticketActual = numeroTicket;
                    empleadoActual = empleado;
                    motivoActual = motivo;
                }
                
                tabla.addCell(new Cell().add(new Paragraph(sdfCompleto.format(rs.getTimestamp("fecha")))
                    .setFontSize(10)));
                tabla.addCell(new Cell().add(new Paragraph(numeroTicket)));
                tabla.addCell(new Cell().add(new Paragraph(rs.getString("ticket_original"))));
                tabla.addCell(new Cell().add(new Paragraph(rs.getString("producto"))));
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(rs.getInt("cantidad")))
                    .setTextAlignment(TextAlignment.RIGHT)));
                tabla.addCell(new Cell().add(new Paragraph(String.format("$%.2f", rs.getDouble("precio_unitario")))
                    .setTextAlignment(TextAlignment.RIGHT)));
                
                double subtotal = rs.getDouble("subtotal");
                totalDevoluciones += subtotal;
                tabla.addCell(new Cell().add(new Paragraph(String.format("$%.2f", subtotal))
                    .setTextAlignment(TextAlignment.RIGHT)));
            }
            
            if (devolucionesCount == 0) {
                tabla.addCell(new Cell(1, 7)
                    .add(new Paragraph("No se encontraron devoluciones en el período seleccionado")
                    .setTextAlignment(TextAlignment.CENTER)));
            }
            
            document.add(tabla);
            
            // Agregar resumen
            document.add(new Paragraph("\nResumen")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.LEFT));
            
            document.add(new Paragraph("Total de devoluciones realizadas: " + devolucionesCount)
                .setFontSize(12));
            
            document.add(new Paragraph("Total monetario devuelto: $" + String.format("%.2f", totalDevoluciones))
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT));
        }
        
        document.close();
    }
    
    private void generarXML(File file, Date fechaInicio, Date fechaFin) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        org.w3c.dom.Document doc = docBuilder.newDocument();
        org.w3c.dom.Element rootElement = doc.createElement("reporte_devoluciones");
        doc.appendChild(rootElement);
        
        org.w3c.dom.Element periodo = doc.createElement("periodo");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        periodo.setAttribute("inicio", sdf.format(fechaInicio));
        periodo.setAttribute("fin", sdf.format(fechaFin));
        rootElement.appendChild(periodo);
        
        String sql = "SELECT d.fecha, d.numero_ticket, v.numero_ticket as ticket_original, " +
                    "p.nombre as producto, dd.cantidad, dd.precio_unitario, " +
                    "dd.subtotal, d.motivo, CONCAT(e.nombre, ' ', e.apellido) as empleado " +
                    "FROM Devoluciones d " +
                    "INNER JOIN DetallesDevolucion dd ON d.id_devolucion = dd.id_devolucion " +
                    "INNER JOIN Productos p ON dd.id_producto = p.id_producto " +
                    "INNER JOIN Ventas v ON d.id_venta = v.id_venta " +
                    "INNER JOIN Empleados e ON v.id_empleado = e.id_empleado " +
                    "WHERE CONVERT(date, d.fecha) BETWEEN CONVERT(date, ?) AND CONVERT(date, ?) " +
                    "AND d.estado = 'COMPLETADA' " +
                    "ORDER BY d.fecha DESC, d.numero_ticket";
        
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
            
            ResultSet rs = stmt.executeQuery();
            double totalDevoluciones = 0;
            int devolucionesCount = 0;
            String ticketActual = "";
            
            org.w3c.dom.Element devoluciones = doc.createElement("devoluciones");
            rootElement.appendChild(devoluciones);
            org.w3c.dom.Element devolucionActual = null;
            
            while (rs.next()) {
                String numeroTicket = rs.getString("numero_ticket");
                
                if (!numeroTicket.equals(ticketActual)) {
                    devolucionActual = doc.createElement("devolucion");
                    devolucionActual.setAttribute("fecha", sdf.format(rs.getTimestamp("fecha")));
                    devolucionActual.setAttribute("ticket", numeroTicket);
                    devolucionActual.setAttribute("ticket_original", rs.getString("ticket_original"));
                    devolucionActual.setAttribute("empleado", rs.getString("empleado"));
                    devolucionActual.setAttribute("motivo", rs.getString("motivo"));
                    
                    devoluciones.appendChild(devolucionActual);
                    devolucionesCount++;
                    ticketActual = numeroTicket;
                }
                
                org.w3c.dom.Element detalle = doc.createElement("detalle");
                
                org.w3c.dom.Element producto = doc.createElement("producto");
                producto.setTextContent(rs.getString("producto"));
                detalle.appendChild(producto);
                
                org.w3c.dom.Element cantidad = doc.createElement("cantidad");
                cantidad.setTextContent(String.valueOf(rs.getInt("cantidad")));
                detalle.appendChild(cantidad);
                
                org.w3c.dom.Element precioUnit = doc.createElement("precio_unitario");
                precioUnit.setTextContent(String.format("%.2f", rs.getDouble("precio_unitario")));
                detalle.appendChild(precioUnit);
                
                org.w3c.dom.Element subtotal = doc.createElement("subtotal");
                double subtotalValue = rs.getDouble("subtotal");
                totalDevoluciones += subtotalValue;
                subtotal.setTextContent(String.format("%.2f", subtotalValue));
                detalle.appendChild(subtotal);
                
                devolucionActual.appendChild(detalle);
            }
            
            org.w3c.dom.Element resumen = doc.createElement("resumen");
            
            org.w3c.dom.Element totalDevolucionesElement = doc.createElement("total_devoluciones");
            totalDevolucionesElement.setTextContent(String.valueOf(devolucionesCount));
            resumen.appendChild(totalDevolucionesElement);
            
            org.w3c.dom.Element totalMonetario = doc.createElement("total_monetario");
            totalMonetario.setTextContent(String.format("%.2f", totalDevoluciones));
            resumen.appendChild(totalMonetario);
            
            rootElement.appendChild(resumen);
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
    
    private void generarJSON(File file, Date fechaInicio, Date fechaFin) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        ObjectNode periodoNode = mapper.createObjectNode();
        periodoNode.put("inicio", sdf.format(fechaInicio));
        periodoNode.put("fin", sdf.format(fechaFin));
        rootNode.set("periodo", periodoNode);
        
        String sql = "SELECT d.fecha, d.numero_ticket, v.numero_ticket as ticket_original, " +
                    "p.nombre as producto, dd.cantidad, dd.precio_unitario, " +
                    "dd.subtotal, d.motivo, CONCAT(e.nombre, ' ', e.apellido) as empleado " +
                    "FROM Devoluciones d " +
                    "INNER JOIN DetallesDevolucion dd ON d.id_devolucion = dd.id_devolucion " +
                    "INNER JOIN Productos p ON dd.id_producto = p.id_producto " +
                    "INNER JOIN Ventas v ON d.id_venta = v.id_venta " +
                    "INNER JOIN Empleados e ON v.id_empleado = e.id_empleado " +
                    "WHERE CONVERT(date, d.fecha) BETWEEN CONVERT(date, ?) AND CONVERT(date, ?) " +
                    "AND d.estado = 'COMPLETADA' " +
                    "ORDER BY d.fecha DESC, d.numero_ticket";
                    
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
            
            ResultSet rs = stmt.executeQuery();
            ArrayNode devolucionesArray = mapper.createArrayNode();
            double totalDevoluciones = 0;
            int devolucionesCount = 0;
            String ticketActual = "";
            ObjectNode devolucionActual = null;
            ArrayNode detallesActual = null;
            
            while (rs.next()) {
                String numeroTicket = rs.getString("numero_ticket");
                
                if (!numeroTicket.equals(ticketActual)) {
                    if (devolucionActual != null) {
                        devolucionActual.set("detalles", detallesActual);
                        devolucionesArray.add(devolucionActual);
                    }
                    
                    devolucionActual = mapper.createObjectNode();
                    detallesActual = mapper.createArrayNode();
                    
                    devolucionActual.put("fecha", sdf.format(rs.getTimestamp("fecha")));
                    devolucionActual.put("ticket_devolucion", numeroTicket);
                    devolucionActual.put("ticket_original", rs.getString("ticket_original"));
                    devolucionActual.put("empleado", rs.getString("empleado"));
                    devolucionActual.put("motivo", rs.getString("motivo"));
                    
                    devolucionesCount++;
                    ticketActual = numeroTicket;
                }
                
                ObjectNode detalleNode = mapper.createObjectNode();
                detalleNode.put("producto", rs.getString("producto"));
                detalleNode.put("cantidad", rs.getInt("cantidad"));
                detalleNode.put("precio_unitario", rs.getDouble("precio_unitario"));
                
                double subtotal = rs.getDouble("subtotal");
                totalDevoluciones += subtotal;
                detalleNode.put("subtotal", subtotal);
                
                detallesActual.add(detalleNode);
            }
            
            // Agregar la última devolución
            if (devolucionActual != null) {
                devolucionActual.set("detalles", detallesActual);
                devolucionesArray.add(devolucionActual);
            }
            
            rootNode.set("devoluciones", devolucionesArray);
            
            ObjectNode resumenNode = mapper.createObjectNode();
            resumenNode.put("total_devoluciones", devolucionesCount);
            resumenNode.put("total_monetario", totalDevoluciones);
            rootNode.set("resumen", resumenNode);
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
    }
}
