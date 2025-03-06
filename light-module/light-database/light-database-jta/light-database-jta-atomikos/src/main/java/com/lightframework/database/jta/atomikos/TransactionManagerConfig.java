package com.lightframework.database.jta.atomikos;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.transaction.TransactionProperties;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/** 分布式事务管理
 * @author yg
 * @date 2023/4/11 16:28
 * @version 1.0
 */
@Configuration
@EnableTransactionManagement
public class TransactionManagerConfig {

    @Autowired
    private TransactionProperties transactionProperties;

    @Bean(name = "userTransaction")
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(300);
        return userTransactionImp;
    }

    @Bean(name = "atomikosTransactionManager")
    public TransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    @Bean(name = "transactionManager")
    @DependsOn({"userTransaction", "atomikosTransactionManager"})
    public PlatformTransactionManager transactionManager() throws Throwable {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction(), atomikosTransactionManager());
        jtaTransactionManager.setDefaultTimeout((int) transactionProperties.getDefaultTimeout().getSeconds());
        return jtaTransactionManager;
    }
}