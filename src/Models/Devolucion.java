package Models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Devolucion {
    private int idDevolucion;
    private int idVenta;
    private Date fecha;
    private String motivo;
    private double totalDevolucion;
    private String estado;
    private List<DetalleDevolucion> detalles;

    public Devolucion() {
        this.detalles = new ArrayList<>();
    }

    public Devolucion(int idDevolucion, int idVenta, Date fecha, String motivo, double totalDevolucion, String estado) {
        this.idDevolucion = idDevolucion;
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.motivo = motivo;
        this.totalDevolucion = totalDevolucion;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    // Getters
    public int getIdDevolucion() { return idDevolucion; }
    public int getIdVenta() { return idVenta; }
    public Date getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public double getTotalDevolucion() { return totalDevolucion; }
    public String getEstado() { return estado; }
    public List<DetalleDevolucion> getDetalles() { return detalles; }

    // Setters
    public void setIdDevolucion(int idDevolucion) { this.idDevolucion = idDevolucion; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setTotalDevolucion(double totalDevolucion) { this.totalDevolucion = totalDevolucion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setDetalles(List<DetalleDevolucion> detalles) { this.detalles = detalles; }

    public void agregarDetalle(DetalleDevolucion detalle) {
        this.detalles.add(detalle);
        recalcularTotal();
    }

    public void recalcularTotal() {
        this.totalDevolucion = detalles.stream()
            .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario())
            .sum();
    }
}
