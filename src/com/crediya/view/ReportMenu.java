package com.crediya.view;

import com.crediya.service.ReportService;
import com.crediya.util.FileManager;
import com.crediya.util.InputHelper;
import java.util.List;

public class ReportMenu extends Menu {
    private final ReportService service;

    public ReportMenu(ReportService service) { this.service = service; }

    @Override protected String title() { return "REPORTES"; }

    @Override
    protected List<String> options() {
        return List.of("Prestamos activos", "Prestamos vencidos", "Clientes morosos",
                "Resumen de cartera", "Ver auditoria (archivo de texto)");
    }

    @Override
    protected void execute(int option) {
        switch (option) {
            case 1 -> show(service.activeLoansReport(), "report_prestamos_activos.txt");
            case 2 -> show(service.overdueLoansReport(), "report_prestamos_vencidos.txt");
            case 3 -> show(service.delinquentClientsReport(), "report_clientes_morosos.txt");
            case 4 -> show(service.summaryReport(), "report_resumen.txt");
            case 5 -> showLog();
        }
    }

    private void show(List<String> lines, String fileName) {
        System.out.println();
        lines.forEach(System.out::println);
        if (InputHelper.confirm("\nExportar este reporte a data/" + fileName + "?")) {
            FileManager.overwrite(fileName, lines);
            System.out.println("Reporte guardado.");
        }
    }

    private void showLog() {
        List<String> log = FileManager.readAll("audit_log.txt");
        if (log.isEmpty()) { System.out.println("El registro de auditoria esta vacio."); return; }
        int from = Math.max(0, log.size() - 20);
        System.out.println("Ultimas " + (log.size() - from) + " entradas de data/audit_log.txt:");
        log.subList(from, log.size()).forEach(System.out::println);
    }
}