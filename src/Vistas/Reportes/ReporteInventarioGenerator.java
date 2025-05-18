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

public class ReporteInventarioGenerator {
    
    public void generarReporte(String formato) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Inventario");
        
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
                        generarPDF(file);
                        break;
                    case "XML":
                        generarXML(file);
                        break;
                    case "JSON":
                        generarJSON(file);
                        break;
                    case "XLS":
                        generarExcel(file);
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
    
    private void generarPDF(File file) throws Exception {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        Paragraph titulo = new Paragraph("Reporte de Inventario")
            .setBold()
            .setFontSize(18)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Paragraph fecha = new Paragraph("Fecha de generación: " + sdf.format(new Date()))
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER);
        document.add(fecha);
        
        // Tabla de productos
        float[] columnWidths = {2, 4, 2, 2, 2, 2, 3};
        Table tabla = new Table(UnitValue.createPercentArray(columnWidths));
        tabla.setWidth(UnitValue.createPercentValue(100));
        
        tabla.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Categoría").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Stock").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Mínimo").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Precio").setBold()));
        tabla.addHeaderCell(new Cell().add(new Paragraph("Estado Stock").setBold()));
        
        String sql = "SELECT p.id_producto, p.nombre, c.nombre as categoria, " +
                    "p.existencias, p.stock_minimo, p.precio_unitario, " +
                    "CASE " +
                    "   WHEN p.existencias <= p.stock_minimo THEN 'Bajo Stock' " +
                    "   WHEN p.existencias = 0 THEN 'Sin Stock' " +
                    "   ELSE 'OK' " +
                    "END as estado_stock " +
                    "FROM Productos p " +
                    "INNER JOIN Categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.estado = 'Activo' " +
                    "ORDER BY c.nombre, p.nombre";
                    
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int totalProductos = 0;
            int productosMinStock = 0;
            double valorTotal = 0;
            
            while (rs.next()) {
                tabla.addCell(new Cell().add(new Paragraph(rs.getString("id_producto"))));
                tabla.addCell(new Cell().add(new Paragraph(rs.getString("nombre"))));
                tabla.addCell(new Cell().add(new Paragraph(rs.getString("categoria"))));
                
                int existencias = rs.getInt("existencias");
                int stockMinimo = rs.getInt("stock_minimo");
                double precioUnitario = rs.getDouble("precio_unitario");
                String estadoStock = rs.getString("estado_stock");
                
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(existencias))
                    .setTextAlignment(TextAlignment.RIGHT)));
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(stockMinimo))
                    .setTextAlignment(TextAlignment.RIGHT)));
                tabla.addCell(new Cell().add(new Paragraph(String.format("$%.2f", precioUnitario))
                    .setTextAlignment(TextAlignment.RIGHT)));
                
                Cell estadoCell = new Cell().add(new Paragraph(estadoStock));
                if (estadoStock.equals("Bajo Stock")) {
                    estadoCell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.YELLOW);
                    productosMinStock++;
                } else if (estadoStock.equals("Sin Stock")) {
                    estadoCell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.RED);
                    productosMinStock++;
                }
                tabla.addCell(estadoCell);
                
                totalProductos++;
                valorTotal += existencias * precioUnitario;
            }
            
            document.add(tabla);
            
            // Resumen
            document.add(new Paragraph("\nResumen del Inventario")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.LEFT));
            
            document.add(new Paragraph("Total de productos: " + totalProductos)
                .setFontSize(12));
            document.add(new Paragraph("Productos en bajo stock: " + productosMinStock)
                .setFontSize(12));
            document.add(new Paragraph("Valor total del inventario: $" + String.format("%.2f", valorTotal))
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT));
        }
        
        document.close();
    }
      private void generarExcel(File file) throws Exception {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario");
        
        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.RIGHT);
        
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
        
        CellStyle warningStyle = workbook.createCellStyle();
        warningStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle dangerStyle = workbook.createCellStyle();
        dangerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        dangerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Producto", "Categoría", "Stock", "Mínimo", "Precio", "Estado Stock"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        String sql = "SELECT p.id_producto, p.nombre, c.nombre as categoria, " +
                    "p.existencias, p.stock_minimo, p.precio_unitario, " +
                    "CASE " +
                    "   WHEN p.existencias <= p.stock_minimo THEN 'Bajo Stock' " +
                    "   WHEN p.existencias = 0 THEN 'Sin Stock' " +
                    "   ELSE 'OK' " +
                    "END as estado_stock " +
                    "FROM Productos p " +
                    "INNER JOIN Categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.estado = 'Activo' " +
                    "ORDER BY c.nombre, p.nombre";
                    
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int rowNum = 1;
            double valorTotal = 0;
            int productosMinStock = 0;
            
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(rs.getString("id_producto"));
                row.createCell(1).setCellValue(rs.getString("nombre"));
                row.createCell(2).setCellValue(rs.getString("categoria"));
                
                org.apache.poi.ss.usermodel.Cell stockCell = row.createCell(3);
                stockCell.setCellValue(rs.getInt("existencias"));
                stockCell.setCellStyle(numberStyle);
                
                org.apache.poi.ss.usermodel.Cell minStockCell = row.createCell(4);
                minStockCell.setCellValue(rs.getInt("stock_minimo"));
                minStockCell.setCellStyle(numberStyle);
                
                org.apache.poi.ss.usermodel.Cell priceCell = row.createCell(5);
                double precio = rs.getDouble("precio_unitario");
                priceCell.setCellValue(precio);
                priceCell.setCellStyle(currencyStyle);
                
                org.apache.poi.ss.usermodel.Cell estadoCell = row.createCell(6);
                String estadoStock = rs.getString("estado_stock");
                estadoCell.setCellValue(estadoStock);
                
                if (estadoStock.equals("Bajo Stock")) {
                    estadoCell.setCellStyle(warningStyle);
                    productosMinStock++;
                } else if (estadoStock.equals("Sin Stock")) {
                    estadoCell.setCellStyle(dangerStyle);
                    productosMinStock++;
                }
                
                valorTotal += rs.getInt("existencias") * precio;
            }
            
            // Ajustar anchos de columna
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Agregar resumen
            rowNum += 2;
            Row resumenRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell resumenCell = resumenRow.createCell(0);
            resumenCell.setCellValue("Resumen del Inventario");
            resumenCell.setCellStyle(headerStyle);
            
            Row totalProdRow = sheet.createRow(rowNum++);
            totalProdRow.createCell(0).setCellValue("Total de productos:");
            totalProdRow.createCell(1).setCellValue(rowNum - 5);  // -5 por las filas de espacio y encabezados
            
            Row minStockRow = sheet.createRow(rowNum++);
            minStockRow.createCell(0).setCellValue("Productos en bajo stock:");
            minStockRow.createCell(1).setCellValue(productosMinStock);
            
            Row valorRow = sheet.createRow(rowNum++);
            valorRow.createCell(0).setCellValue("Valor total del inventario:");
            org.apache.poi.ss.usermodel.Cell valorCell = valorRow.createCell(1);
            valorCell.setCellValue(valorTotal);
            valorCell.setCellStyle(currencyStyle);
        }
        
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }
    
    private void generarXML(File file) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        org.w3c.dom.Document doc = docBuilder.newDocument();
        org.w3c.dom.Element rootElement = doc.createElement("reporte_inventario");
        doc.appendChild(rootElement);
        
        // Agregar fecha de generación
        org.w3c.dom.Element fecha = doc.createElement("fecha_generacion");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        fecha.setTextContent(sdf.format(new Date()));
        rootElement.appendChild(fecha);
        
        String sql = "SELECT p.id_producto, p.nombre, c.nombre as categoria, " +
                    "p.existencias, p.stock_minimo, p.precio_unitario, " +
                    "CASE " +
                    "   WHEN p.existencias <= p.stock_minimo THEN 'Bajo Stock' " +
                    "   WHEN p.existencias = 0 THEN 'Sin Stock' " +
                    "   ELSE 'OK' " +
                    "END as estado_stock " +
                    "FROM Productos p " +
                    "INNER JOIN Categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.estado = 'Activo' " +
                    "ORDER BY c.nombre, p.nombre";
        
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            org.w3c.dom.Element productos = doc.createElement("productos");
            rootElement.appendChild(productos);
            
            int totalProductos = 0;
            int productosMinStock = 0;
            double valorTotal = 0;
            
            while (rs.next()) {
                org.w3c.dom.Element producto = doc.createElement("producto");
                
                org.w3c.dom.Element id = doc.createElement("id");
                id.setTextContent(rs.getString("id_producto"));
                producto.appendChild(id);
                
                org.w3c.dom.Element nombre = doc.createElement("nombre");
                nombre.setTextContent(rs.getString("nombre"));
                producto.appendChild(nombre);
                
                org.w3c.dom.Element categoria = doc.createElement("categoria");
                categoria.setTextContent(rs.getString("categoria"));
                producto.appendChild(categoria);
                
                org.w3c.dom.Element existencias = doc.createElement("existencias");
                existencias.setTextContent(String.valueOf(rs.getInt("existencias")));
                producto.appendChild(existencias);
                
                org.w3c.dom.Element stockMinimo = doc.createElement("stock_minimo");
                stockMinimo.setTextContent(String.valueOf(rs.getInt("stock_minimo")));
                producto.appendChild(stockMinimo);
                
                org.w3c.dom.Element precio = doc.createElement("precio");
                double precioUnitario = rs.getDouble("precio_unitario");
                precio.setTextContent(String.format("%.2f", precioUnitario));
                producto.appendChild(precio);
                
                org.w3c.dom.Element estado = doc.createElement("estado_stock");
                String estadoStock = rs.getString("estado_stock");
                estado.setTextContent(estadoStock);
                producto.appendChild(estado);
                
                productos.appendChild(producto);
                
                totalProductos++;
                if (!estadoStock.equals("OK")) {
                    productosMinStock++;
                }
                valorTotal += rs.getInt("existencias") * precioUnitario;
            }
            
            // Agregar resumen
            org.w3c.dom.Element resumen = doc.createElement("resumen");
            
            org.w3c.dom.Element totalProductosElement = doc.createElement("total_productos");
            totalProductosElement.setTextContent(String.valueOf(totalProductos));
            resumen.appendChild(totalProductosElement);
            
            org.w3c.dom.Element productosMinStockElement = doc.createElement("productos_bajo_stock");
            productosMinStockElement.setTextContent(String.valueOf(productosMinStock));
            resumen.appendChild(productosMinStockElement);
            
            org.w3c.dom.Element valorTotalElement = doc.createElement("valor_total");
            valorTotalElement.setTextContent(String.format("%.2f", valorTotal));
            resumen.appendChild(valorTotalElement);
            
            rootElement.appendChild(resumen);
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
    
    private void generarJSON(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        rootNode.put("fecha_generacion", sdf.format(new Date()));
        
        String sql = "SELECT p.id_producto, p.nombre, c.nombre as categoria, " +
                    "p.existencias, p.stock_minimo, p.precio_unitario, " +
                    "CASE " +
                    "   WHEN p.existencias <= p.stock_minimo THEN 'Bajo Stock' " +
                    "   WHEN p.existencias = 0 THEN 'Sin Stock' " +
                    "   ELSE 'OK' " +
                    "END as estado_stock " +
                    "FROM Productos p " +
                    "INNER JOIN Categorias c ON p.id_categoria = c.id_categoria " +
                    "WHERE p.estado = 'Activo' " +
                    "ORDER BY c.nombre, p.nombre";
                    
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ArrayNode productosArray = mapper.createArrayNode();
            int totalProductos = 0;
            int productosMinStock = 0;
            double valorTotal = 0;
            
            while (rs.next()) {
                ObjectNode productoNode = mapper.createObjectNode();
                productoNode.put("id", rs.getString("id_producto"));
                productoNode.put("nombre", rs.getString("nombre"));
                productoNode.put("categoria", rs.getString("categoria"));
                
                int existencias = rs.getInt("existencias");
                double precioUnitario = rs.getDouble("precio_unitario");
                String estadoStock = rs.getString("estado_stock");
                
                productoNode.put("existencias", existencias);
                productoNode.put("stock_minimo", rs.getInt("stock_minimo"));
                productoNode.put("precio", precioUnitario);
                productoNode.put("estado_stock", estadoStock);
                
                productosArray.add(productoNode);
                
                totalProductos++;
                if (!estadoStock.equals("OK")) {
                    productosMinStock++;
                }
                valorTotal += existencias * precioUnitario;
            }
            
            rootNode.set("productos", productosArray);
            
            ObjectNode resumenNode = mapper.createObjectNode();
            resumenNode.put("total_productos", totalProductos);
            resumenNode.put("productos_bajo_stock", productosMinStock);
            resumenNode.put("valor_total", valorTotal);
            rootNode.set("resumen", resumenNode);
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
    }
}
