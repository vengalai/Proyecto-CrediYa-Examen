package com.crediya.service;

import com.crediya.exception.DatoInvalidoException;
import com.crediya.model.Loan;
import com.crediya.model.Prestamo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GestorPrestamos {
    private final List<Prestamo> prestamos = new ArrayList<>();
    private final LoanService loanService;

    public GestorPrestamos(LoanService loanService) { this.loanService = loanService; }

    public void registrarPrestamo(Prestamo p) {
        validar(p);
        prestamos.add(p);
    }

    public void guardarPrestamoEnBD(Prestamo p) {
        if (p != null && p.getId() > 0)
            throw new DatoInvalidoException("El prestamo ya esta guardado en la base de datos (id " + p.getId() + ").");
        Loan loan = validar(p);
        loanService.save(loan); 
        p.setId(loan.getId());
        p.setEstado(Prestamo.traducirEstado(loan.getStatus()));
    }

    public void registrarYGuardar(Prestamo p) {
        guardarPrestamoEnBD(p);          
        prestamos.add(p);
    }

    public int cargarDesdeBD() {
        prestamos.clear();
        loanService.findAll().stream().map(Prestamo::fromLoan).forEach(prestamos::add);
        return prestamos.size();
    }

    public List<String> listarPrestamos() {
        if (prestamos.isEmpty()) return List.of("No hay prestamos registrados.");
        return prestamos.stream().map(Prestamo::toString).collect(Collectors.toList());
    }

    public List<Prestamo> buscarPorEstado(String estado) {
        String buscado = Prestamo.normalizarEstado(estado);
        return prestamos.stream()
                .filter(p -> p.getEstado().equals(buscado))
                .collect(Collectors.toList());
    }

    public List<Prestamo> getPrestamos() { return Collections.unmodifiableList(prestamos); }

    private Loan validar(Prestamo p) {
        if (p == null) throw new DatoInvalidoException("El prestamo no puede ser nulo.");
        if (p.getFechaInicio() == null) throw new DatoInvalidoException("La fecha de inicio es obligatoria.");
        if (!Prestamo.ACTIVO.equals(p.getEstado()))
            throw new DatoInvalidoException("Un prestamo nuevo debe estar en estado Activo.");
        return loanService.buildLoan(p.getClienteId(), p.getEmpleadoId(), p.getMonto(),
                p.getInteresMensual(), p.getPlazoMeses(), p.getFechaInicio());
    }
}