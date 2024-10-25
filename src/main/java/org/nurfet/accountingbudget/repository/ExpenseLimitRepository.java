package org.nurfet.accountingbudget.repository;

import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, Long> {

    @Query("SELECT e FROM ExpenseLimit e WHERE e.startDate > :date ORDER BY e.startDate ASC")
    ExpenseLimit findFutureLimit(@Param("date") LocalDate date);

    @Query("SELECT e FROM ExpenseLimit e WHERE e.startDate <= :date AND e.endDate >= :date ORDER BY e.startDate DESC")
    ExpenseLimit findCurrentLimit(@Param("date") LocalDate date);

    Optional<ExpenseLimit> findTopByOrderByStartDateDesc();
}
