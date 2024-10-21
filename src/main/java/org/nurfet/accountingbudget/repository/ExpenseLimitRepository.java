package org.nurfet.accountingbudget.repository;

import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, Long> {

    Optional<ExpenseLimit> findTopByOrderByStartDateDesc();
}
