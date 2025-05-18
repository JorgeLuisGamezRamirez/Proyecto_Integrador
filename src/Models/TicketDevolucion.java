package Models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class TicketDevolucion {
    private int idDevolucion;
    private String numeroTicketOriginal;
    private String numeroTicketDevolucion;
    private Date fecha;
    private double totalDevuelto;
    private String motivo;
    private String estado;
    private String nombreEmpleado;
    private String nombreCliente;
    private List<DetalleDevolucion> detalles;

    public TicketDevolucion() {
        this.detalles = new ArrayList<>();
    }

    public TicketDevolucion(int idDevolucion, String numeroTicketOriginal, String numeroTicketDevolucion, 
                          Date fecha, double totalDevuelto, String motivo, String estado) {
        this.idDevolucion = idDevolucion;
        this.numeroTicketOriginal = numeroTicketOriginal;
        this.numeroTicketDevolucion = numeroTicketDevolucion;
        this.fecha = fecha;
        this.totalDevuelto = totalDevuelto;
        this.motivo = motivo;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    // Getters
    public int getIdDevolucion() { return idDevolucion; }
    public String getNumeroTicketOriginal() { return numeroTicketOriginal; }
    public String getNumeroTicketDevolucion() { return numeroTicketDevolucion; }
    public Date getFecha() { return fecha; }
    public double getTotalDevuelto() { return totalDevuelto; }
    public String getMotivo() { return motivo; }
    public String getEstado() { return estado; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public String getNombreCliente() { return nombreCliente; }
    public List<DetalleDevolucion> getDetalles() { return detalles; }

    // Setters
    public void setIdDevolucion(int idDevolucion) { this.idDevolucion = idDevolucion; }
    public void setNumeroTicketOriginal(String numeroTicketOriginal) { this.numeroTicketOriginal = numeroTicketOriginal; }
    public void setNumeroTicketDevolucion(String numeroTicketDevolucion) { this.numeroTicketDevolucion = numeroTicketDevolucion; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public void setTotalDevuelto(double totalDevuelto) { this.totalDevuelto = totalDevuelto; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public void setDetalles(List<DetalleDevolucion> detalles) { this.detalles = detalles; }

    public void agregarDetalle(DetalleDevolucion detalle) {
        this.detalles.add(detalle);
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.totalDevuelto = detalles.stream()
            .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioUnitario())
            .sum();
    }
}
