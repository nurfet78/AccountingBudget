package org.nurfet.accountingbudget.repository;

import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCategory(Category category);

    List<Transaction> findByDateBetweenAndType(LocalDate startDate, LocalDate endDate, TransactionType type);
}
