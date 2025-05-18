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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
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
import java.util.HashMap;
import java.util.Map;

public class ReporteProductosGenerator {
    
    public void generarReporte(String formato, Date fechaInicio, Date fechaFin) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Productos");
        
        String extension = formato.toLowerCase();
        if (extension.equals("xls")) {
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel Files (*.xls)", "xls"));
        } else {
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                formato.toUpperCase() + " Files", extension));
        }
            
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
                    case "XLS":
                        generarExcel(file, fechaInicio, fechaFin);
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

    private Map<String, Object> obtenerDatosProducto(String idProducto, Date fechaInicio, Date fechaFin) throws SQLException {
        Map<String, Object> datos = new HashMap<>();
        
        try (Connection conn = Conexion.getConnection()) {
            // Información básica del producto
            String sqlProducto = "SELECT p.*, c.nombre as categoria FROM Productos p " +
                               "LEFT JOIN Categorias c ON p.id_categoria = c.id_categoria " +
                               "WHERE p.id_producto = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlProducto)) {
                pstmt.setString(1, idProducto);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    datos.put("id", rs.getString("id_producto"));
                    datos.put("nombre", rs.getString("nombre"));
                    datos.put("categoria", rs.getString("categoria"));
                    datos.put("existencias", rs.getInt("existencias"));
                    datos.put("stock_minimo", rs.getInt("stock_minimo"));
                    datos.put("precio_unitario", rs.getDouble("precio_unitario"));
                }
            }
            
            // Estadísticas de ventas
            String sqlVentas = "SELECT COUNT(DISTINCT dv.id_detalle) as total_ventas, " +
                             "SUM(dv.cantidad) as unidades_vendidas, " +
                             "SUM(dv.subtotal) as total_ingresos " +
                             "FROM DetallesVenta dv " +
                             "INNER JOIN Ventas v ON dv.id_venta = v.id_venta " +
                             "WHERE dv.id_producto = ? " +
                             "AND v.fecha BETWEEN ? AND ? " +
                             "AND v.estado = 'COMPLETADA'";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVentas)) {
                pstmt.setString(1, idProducto);
                pstmt.setTimestamp(2, new Timestamp(fechaInicio.getTime()));
                pstmt.setTimestamp(3, new Timestamp(fechaFin.getTime()));
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    datos.put("total_ventas", rs.getInt("total_ventas"));
                    datos.put("unidades_vendidas", rs.getInt("unidades_vendidas"));
                    datos.put("total_ingresos", rs.getDouble("total_ingresos"));
                }
            }
            
            // Última venta
            String sqlUltimaVenta = "SELECT MAX(v.fecha) as ultima_venta " +
                                  "FROM DetallesVenta dv " +
                                  "INNER JOIN Ventas v ON dv.id_venta = v.id_venta " +
                                  "WHERE dv.id_producto = ? " +
                                  "AND v.estado = 'COMPLETADA'";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUltimaVenta)) {
                pstmt.setString(1, idProducto);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    datos.put("ultima_venta", rs.getTimestamp("ultima_venta"));
                }
            }
        }
        
        return datos;
    }

    private void generarPDF(File file, Date fechaInicio, Date fechaFin) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Título del reporte
        document.add(new Paragraph("Reporte Detallado de Productos")
            .setBold()
            .setFontSize(18)
            .setTextAlignment(TextAlignment.CENTER));
        
        // Período del reporte
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        document.add(new Paragraph("Período: " + sdf.format(fechaInicio) + " - " + sdf.format(fechaFin))
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER));
        
        // Obtener todos los productos
        String sql = "SELECT id_producto FROM Productos WHERE estado = 'Activo' ORDER BY nombre";
        
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String idProducto = rs.getString("id_producto");
                Map<String, Object> datosProducto = obtenerDatosProducto(idProducto, fechaInicio, fechaFin);
                
                // Agregar sección por producto
                document.add(new Paragraph("\n" + datosProducto.get("nombre"))
                    .setBold()
                    .setFontSize(14));
                
                // Tabla de información básica
                float[] columnWidths = {2, 4};
                Table tabla = new Table(UnitValue.createPercentArray(columnWidths));
                tabla.setWidth(UnitValue.createPercentValue(100));
                
                agregarFilaTabla(tabla, "ID:", datosProducto.get("id").toString());
                agregarFilaTabla(tabla, "Categoría:", datosProducto.get("categoria").toString());
                agregarFilaTabla(tabla, "Stock Actual:", datosProducto.get("existencias").toString());
                agregarFilaTabla(tabla, "Stock Mínimo:", datosProducto.get("stock_minimo").toString());
                agregarFilaTabla(tabla, "Precio Unitario:", String.format("$%.2f", datosProducto.get("precio_unitario")));
                agregarFilaTabla(tabla, "Total Ventas:", datosProducto.get("total_ventas").toString());
                agregarFilaTabla(tabla, "Unidades Vendidas:", datosProducto.get("unidades_vendidas").toString());
                agregarFilaTabla(tabla, "Total Ingresos:", String.format("$%.2f", datosProducto.get("total_ingresos")));
                
                if (datosProducto.get("ultima_venta") != null) {
                    agregarFilaTabla(tabla, "Última Venta:", sdf.format(datosProducto.get("ultima_venta")));
                }
                
                document.add(tabla);
            }
        }
        
        document.close();
    }

    private void agregarFilaTabla(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell().add(new Paragraph(etiqueta).setBold()));
        tabla.addCell(new Cell().add(new Paragraph(valor)));
    }

    private void generarExcel(File file, Date fechaInicio, Date fechaFin) throws Exception {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");
        
        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        
        // Encabezados
        String[] headers = {"ID", "Nombre", "Categoría", "Stock Actual", "Stock Mínimo", 
                          "Precio Unitario", "Total Ventas", "Unidades Vendidas", 
                          "Total Ingresos", "Última Venta"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Datos
        String sql = "SELECT id_producto FROM Productos WHERE estado = 'Activo' ORDER BY nombre";
        int rowNum = 1;
        
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                String idProducto = rs.getString("id_producto");
                Map<String, Object> datos = obtenerDatosProducto(idProducto, fechaInicio, fechaFin);
                
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(datos.get("id").toString());
                row.createCell(1).setCellValue(datos.get("nombre").toString());
                row.createCell(2).setCellValue(datos.get("categoria").toString());
                row.createCell(3).setCellValue((Integer)datos.get("existencias"));
                row.createCell(4).setCellValue((Integer)datos.get("stock_minimo"));
                
                org.apache.poi.ss.usermodel.Cell precioCell = row.createCell(5);
                precioCell.setCellValue((Double)datos.get("precio_unitario"));
                precioCell.setCellStyle(currencyStyle);
                
                row.createCell(6).setCellValue((Integer)datos.get("total_ventas"));
                row.createCell(7).setCellValue((Integer)datos.get("unidades_vendidas"));
                
                org.apache.poi.ss.usermodel.Cell ingresosCell = row.createCell(8);
                ingresosCell.setCellValue((Double)datos.get("total_ingresos"));
                ingresosCell.setCellStyle(currencyStyle);
                
                if (datos.get("ultima_venta") != null) {
                    row.createCell(9).setCellValue(sdf.format(datos.get("ultima_venta")));
                }
            }
            
            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        }
        
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private void generarXML(File file, Date fechaInicio, Date fechaFin) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        org.w3c.dom.Document doc = docBuilder.newDocument();
        org.w3c.dom.Element rootElement = doc.createElement("reporte_productos");
        doc.appendChild(rootElement);
        
        // Agregar período del reporte
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        org.w3c.dom.Element periodo = doc.createElement("periodo");
        periodo.setAttribute("fecha_inicio", sdf.format(fechaInicio));
        periodo.setAttribute("fecha_fin", sdf.format(fechaFin));
        rootElement.appendChild(periodo);
        
        // Obtener y agregar productos
        String sql = "SELECT id_producto FROM Productos WHERE estado = 'Activo' ORDER BY nombre";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            org.w3c.dom.Element productos = doc.createElement("productos");
            rootElement.appendChild(productos);
            
            while (rs.next()) {
                String idProducto = rs.getString("id_producto");
                Map<String, Object> datos = obtenerDatosProducto(idProducto, fechaInicio, fechaFin);
                
                org.w3c.dom.Element producto = doc.createElement("producto");
                productos.appendChild(producto);
                
                agregarElementoXML(doc, producto, "id", datos.get("id").toString());
                agregarElementoXML(doc, producto, "nombre", datos.get("nombre").toString());
                agregarElementoXML(doc, producto, "categoria", datos.get("categoria").toString());
                agregarElementoXML(doc, producto, "stock_actual", datos.get("existencias").toString());
                agregarElementoXML(doc, producto, "stock_minimo", datos.get("stock_minimo").toString());
                agregarElementoXML(doc, producto, "precio_unitario", String.format("%.2f", datos.get("precio_unitario")));
                agregarElementoXML(doc, producto, "total_ventas", datos.get("total_ventas").toString());
                agregarElementoXML(doc, producto, "unidades_vendidas", datos.get("unidades_vendidas").toString());
                agregarElementoXML(doc, producto, "total_ingresos", String.format("%.2f", datos.get("total_ingresos")));
                
                if (datos.get("ultima_venta") != null) {
                    agregarElementoXML(doc, producto, "ultima_venta", sdf.format(datos.get("ultima_venta")));
                }
            }
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private void agregarElementoXML(org.w3c.dom.Document doc, org.w3c.dom.Element parent, 
                                  String nombre, String valor) {
        org.w3c.dom.Element elemento = doc.createElement(nombre);
        elemento.setTextContent(valor);
        parent.appendChild(elemento);
    }

    private void generarJSON(File file, Date fechaInicio, Date fechaFin) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        // Agregar período del reporte
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        rootNode.put("fecha_inicio", sdf.format(fechaInicio));
        rootNode.put("fecha_fin", sdf.format(fechaFin));
        
        // Obtener y agregar productos
        ArrayNode productosArray = mapper.createArrayNode();
        
        String sql = "SELECT id_producto FROM Productos WHERE estado = 'Activo' ORDER BY nombre";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String idProducto = rs.getString("id_producto");
                Map<String, Object> datos = obtenerDatosProducto(idProducto, fechaInicio, fechaFin);
                
                ObjectNode productoNode = mapper.createObjectNode();
                productoNode.put("id", datos.get("id").toString());
                productoNode.put("nombre", datos.get("nombre").toString());
                productoNode.put("categoria", datos.get("categoria").toString());
                productoNode.put("stock_actual", (Integer)datos.get("existencias"));
                productoNode.put("stock_minimo", (Integer)datos.get("stock_minimo"));
                productoNode.put("precio_unitario", (Double)datos.get("precio_unitario"));
                productoNode.put("total_ventas", (Integer)datos.get("total_ventas"));
                productoNode.put("unidades_vendidas", (Integer)datos.get("unidades_vendidas"));
                productoNode.put("total_ingresos", (Double)datos.get("total_ingresos"));
                
                if (datos.get("ultima_venta") != null) {
                    productoNode.put("ultima_venta", sdf.format(datos.get("ultima_venta")));
                }
                
                productosArray.add(productoNode);
            }
        }
        
        rootNode.set("productos", productosArray);
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
    }
}
