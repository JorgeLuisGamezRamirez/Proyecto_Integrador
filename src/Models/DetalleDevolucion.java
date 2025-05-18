package Models;

public class DetalleDevolucion {
    private int idDetalle;
    private int idDevolucion;
    private String idProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private String nombreProducto; // Para mostrar en la interfaz

    public DetalleDevolucion() {}

    public DetalleDevolucion(int idDetalle, int idDevolucion, String idProducto, 
                           int cantidad, double precioUnitario, String nombreProducto) {
        this.idDetalle = idDetalle;
        this.idDevolucion = idDevolucion;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.nombreProducto = nombreProducto;
        this.calcularSubtotal();
    }

    // Getters
    public int getIdDetalle() { return idDetalle; }
    public int getIdDevolucion() { return idDevolucion; }
    public String getIdProducto() { return idProducto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal() { return subtotal; }
    public String getNombreProducto() { return nombreProducto; }
    
    // Setters
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public void setIdDevolucion(int idDevolucion) { this.idDevolucion = idDevolucion; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad;
        calcularSubtotal();
    }
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    private void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
}
