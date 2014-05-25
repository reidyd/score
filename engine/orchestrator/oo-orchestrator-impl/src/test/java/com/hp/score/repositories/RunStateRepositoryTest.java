package com.hp.score.repositories;

import com.hp.score.engine.data.SimpleHiloIdentifierGenerator;
import com.hp.score.entities.RunState;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.ejb.HibernatePersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import static org.fest.assertions.Assertions.assertThat;

/**
 * User: maromg
 * Date: 21/05/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RunStateRepositoryTest {

    @Autowired
    private RunStateRepository runStateRepository;

    @Test
    public void testFindRunIdByStatuses() {
        RunState canceledRunState = createRunState(ExecutionStatus.CANCELED);
        RunState completedRunState = createRunState(ExecutionStatus.COMPLETED);
        createRunState(ExecutionStatus.PENDING_CANCEL);

        List<String> runStates = runStateRepository.findRunIdByStatuses(Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.COMPLETED));

        assertThat(runStates).containsExactly(canceledRunState.getRunId(), completedRunState.getRunId());
    }

    private RunState createRunState(ExecutionStatus status) {
        RunState runState = new RunState();
        runState.setStatus(status);
        runState.setRunId(UUID.randomUUID().toString());
        runState.setBranchId(UUID.randomUUID().toString());
        runStateRepository.saveAndFlush(runState);
        return runState;
    }

    @Configuration
    @EnableJpaRepositories("com.hp.score")
    static class RunStateRepositoryTestContext {

        @Bean
        DataSource dataSource(){
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:test");
            ds.setUsername("sa");
            ds.setPassword("sa");
            ds.setDefaultAutoCommit(false);
            return ds;
        }

        @Bean(name="entityManagerFactory")
        @DependsOn({"liquibase", "dataSource"})
        FactoryBean<EntityManagerFactory> emf(JpaVendorAdapter jpaVendorAdapter) {
            SimpleHiloIdentifierGenerator.setDataSource(dataSource());
            LocalContainerEntityManagerFactoryBean fb = new LocalContainerEntityManagerFactoryBean();
            fb.setJpaProperties(hibernateProperties());
            fb.setDataSource(dataSource());
            fb.setPersistenceProviderClass(HibernatePersistence.class);
            fb.setPackagesToScan("com.hp.score");
            fb.setJpaVendorAdapter(jpaVendorAdapter);
            return fb;
        }

        @Bean
        Properties hibernateProperties() {
            return new Properties(){{
                setProperty("hibernate.format_sql", "true");
                setProperty("hibernate.hbm2ddl.auto", "create-drop");
                setProperty("hibernate.cache.use_query_cache", "false");
                setProperty("hibernate.generate_statistics", "false");
                setProperty("hibernate.cache.use_second_level_cache", "false");
                setProperty("hibernate.order_updates", "true");
                setProperty("hibernate.order_inserts", "true");
            }};
        }

        @Bean
        JpaVendorAdapter jpaVendorAdapter() {
            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            adapter.setShowSql(true);
            adapter.setGenerateDdl(true);
            return adapter;
        }

        @Bean
        SpringLiquibase liquibase(){
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource());
            liquibase.setChangeLog("classpath:/META-INF/database/test-changes.xml");
            return liquibase;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

    }
}
