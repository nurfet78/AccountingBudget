package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.dto.ReportDTO;

import java.time.LocalDate;

public interface ReportService {

    ReportDTO generateDetailedReport(LocalDate startDate, LocalDate endDate);
}
