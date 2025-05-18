package Models;

public class Producto {
    private String idProducto;
    private String nombre;
    private String descripcion;
    private double precioUnitario;
    private int existencias;
    private int stockMinimo;
    private int idCategoria;
    private String estado;
    private String fechaCreacion;
    private String nombreCategoria; // Para almacenar el nombre de la categoría

    // Constructor
    public Producto(String idProducto, String nombre, String descripcion, 
                   double precioUnitario, int existencias, int stockMinimo,
                   int idCategoria, String estado, String fechaCreacion,
                   String nombreCategoria) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.existencias = existencias;
        this.stockMinimo = stockMinimo;
        this.idCategoria = idCategoria;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.nombreCategoria = nombreCategoria;
    }

    // Getters
    public String getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getExistencias() { return existencias; }
    public int getStockMinimo() { return stockMinimo; }
    public int getIdCategoria() { return idCategoria; }
    public String getEstado() { return estado; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getNombreCategoria() { return nombreCategoria; }
}
