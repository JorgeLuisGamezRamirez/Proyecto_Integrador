package Models;

public class User {
    private int id_empleado;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private String rol;
    private String usuario;
    private String contraseña;

    public User(String nombre, String apellido, String usuario, String contraseña) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.contraseña = contraseña;
    }

    public User(int id_empleado, String nombre, String apellido, String telefono, 
                String direccion, String rol, String usuario, String contraseña) {
        this.id_empleado = id_empleado;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rol = rol;
        this.usuario = usuario;
        this.contraseña = contraseña;
    }

    // Getters
    public int getId() { return id_empleado; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getRol() { return rol; }
    public String getUsuario() { return usuario; }
    public String getContraseña() { return contraseña; }

    // Setters
    public void setId(int id_empleado) { this.id_empleado = id_empleado; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setRol(String rol) { this.rol = rol; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }
}
