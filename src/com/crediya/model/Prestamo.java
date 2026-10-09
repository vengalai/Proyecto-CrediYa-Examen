package com.crediya.model;

import com.crediya.exception.DatoInvalidoException;
import com.crediya.util.MoneyUtil;
import java.time.LocalDate;


public class Prestamo {
    public static final String ACTIVO = "Activo";
    public static final String PAGADO = "Pagado";
    public static final String MORA = "Mora";

    private int id;
    private int clienteId;
    private double monto;
    private double interesMensual;
    private int plazoMeses;
    private String estado = ACTIVO;

    private int empleadoId;
    private LocalDate fechaInicio = LocalDate.now();

    public Prestamo() { }

    public Prestamo(int id, int clienteId, double monto, double interesMensual, int plazoMeses, String estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.monto = monto;
        this.interesMensual = interesMensual;
        this.plazoMeses = plazoMeses;
        setEstado(estado);
    }

    public Prestamo(int id, int clienteId, double monto, double interesMensual, int plazoMeses, String estado,
                    int empleadoId, LocalDate fechaInicio) {
        this(id, clienteId, monto, interesMensual, plazoMeses, estado);
        this.empleadoId = empleadoId;
        this.fechaInicio = fechaInicio;
    }


    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }
    public int getClienteId()                { return clienteId; }
    public void setClienteId(int c)          { this.clienteId = c; }
    public double getMonto()                 { return monto; }
    public void setMonto(double m)           { this.monto = m; }
    public double getInteresMensual()        { return interesMensual; }
    public void setInteresMensual(double i)  { this.interesMensual = i; }
    public int getPlazoMeses()               { return plazoMeses; }
    public void setPlazoMeses(int p)         { this.plazoMeses = p; }
    public String getEstado()                { return estado; }
    public int getEmpleadoId()               { return empleadoId; }
    public void setEmpleadoId(int e)         { this.empleadoId = e; }
    public LocalDate getFechaInicio()        { return fechaInicio; }
    public void setFechaInicio(LocalDate f)  { this.fechaInicio = f; }


    public void setEstado(String estado) { this.estado = normalizarEstado(estado); }


    public static String normalizarEstado(String estado) {
        if (estado == null) throw new DatoInvalidoException("El estado es obligatorio.");
        for (String valido : new String[] { ACTIVO, PAGADO, MORA }) {
            if (valido.equalsIgnoreCase(estado.trim())) return valido;
        }
        throw new DatoInvalidoException("Estado invalido: '" + estado + "'. Use Activo, Pagado o Mora.");
    }


    public double calcularTotalAPagar() {
        if (monto <= 0)          throw new DatoInvalidoException("El monto debe ser mayor que cero.");
        if (plazoMeses <= 0)     throw new DatoInvalidoException("El plazo debe ser mayor que cero.");
        if (interesMensual < 0)  throw new DatoInvalidoException("La tasa de interes no puede ser negativa.");
        return Loan.calcularTotal(monto, interesMensual, plazoMeses);
    }


    public static String traducirEstado(LoanStatus s) {
        switch (s) {
            case ACTIVE:  return ACTIVO;
            case PAID:    return PAGADO;
            default:      return MORA;
        }
    }

    public static Prestamo fromLoan(Loan l) {
        return new Prestamo(l.getId(), l.getClientId(), l.getPrincipal(), l.getMonthlyRate(), l.getTermMonths(),
                traducirEstado(l.getStatus()), l.getEmployeeId(), l.getStartDate());
    }

    @Override
    public String toString() {
        String total;
        try { total = MoneyUtil.format(calcularTotalAPagar()); }
        catch (DatoInvalidoException e) { total = "n/d"; }
        return String.format("Prestamo #%-3d | cliente #%-3d | monto %-15s | %5.2f%% mensual | plazo %2d meses | %-6s | total a pagar %s",
                id, clienteId, MoneyUtil.format(monto), interesMensual, plazoMeses, estado, total);
    }
}