package Models;

import java.util.Date;
import java.util.List;

public class Ticket {
    private int id_venta;
    private String numero_ticket;
    private Date fecha;
    private double subtotal;
    private double impuestos;
    private double total;
    private String metodo_pago;
    private String estado;
    private int id_cliente;
    private int id_empleado;
    private List<DetalleTicket> detalles;
    
    private String nombre_cliente;
    private String nombre_empleado;
    
    // Clase interna para los detalles
    public static class DetalleTicket {
        private String id_producto;
        private String nombre_producto;
        private int cantidad;
        private double precio_unitario;
        private double descuento_unitario;
        private double subtotal;
        
        public DetalleTicket(String id_producto, String nombre_producto, int cantidad, 
                           double precio_unitario, double descuento_unitario, double subtotal) {
            this.id_producto = id_producto;
            this.nombre_producto = nombre_producto;
            this.cantidad = cantidad;
            this.precio_unitario = precio_unitario;
            this.descuento_unitario = descuento_unitario;
            this.subtotal = subtotal;
        }
        
        // Getters
        public String getIdProducto() { return id_producto; }
        public String getNombreProducto() { return nombre_producto; }
        public int getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precio_unitario; }
        public double getDescuentoUnitario() { return descuento_unitario; }
        public double getSubtotal() { return subtotal; }
    }
    
    // Constructor
    public Ticket(int id_venta, String numero_ticket, Date fecha, double subtotal,
                 double impuestos, double total, String metodo_pago, String estado,
                 int id_cliente, int id_empleado) {
        this.id_venta = id_venta;
        this.numero_ticket = numero_ticket;
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.impuestos = impuestos;
        this.total = total;
        this.metodo_pago = metodo_pago;
        this.estado = estado;
        this.id_cliente = id_cliente;
        this.id_empleado = id_empleado;
        this.nombre_cliente = null;
        this.nombre_empleado = null;
    }
    
    // Getters
    public int getIdVenta() { return id_venta; }
    public String getNumeroTicket() { return numero_ticket; }
    public Date getFecha() { return fecha; }
    public double getSubtotal() { return subtotal; }
    public double getImpuestos() { return impuestos; }
    public double getTotal() { return total; }
    public String getMetodoPago() { return metodo_pago; }
    public String getEstado() { return estado; }
    public int getIdCliente() { return id_cliente; }
    public int getIdEmpleado() { return id_empleado; }
    public List<DetalleTicket> getDetalles() { return detalles; }
    public String getNombreCliente() { return nombre_cliente; }
    public String getNombreEmpleado() { return nombre_empleado; }
    
    // Setters
    public void setDetalles(List<DetalleTicket> detalles) {
        this.detalles = detalles;
    }
    public void setNombreCliente(String nombre_cliente) { this.nombre_cliente = nombre_cliente; }
    public void setNombreEmpleado(String nombre_empleado) { this.nombre_empleado = nombre_empleado; }
}
