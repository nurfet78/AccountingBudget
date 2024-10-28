package org.nurfet.accountingbudget.repository;

import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, Long> {

    @Query("SELECT e FROM ExpenseLimit e WHERE e.startDate <= :date OR e.startDate > :date ORDER BY e.startDate ASC")
    List<ExpenseLimit> findCurrentAndFutureLimits(@Param("date") LocalDate date);

}
