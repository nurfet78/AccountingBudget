package org.nurfet.accountingbudget;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class AccountingBudgetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountingBudgetApplication.class, args);
    }

}
