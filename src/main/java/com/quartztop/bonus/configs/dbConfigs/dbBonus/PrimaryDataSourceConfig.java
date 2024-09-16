package com.quartztop.bonus.configs.dbConfigs.dbBonus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;


@EnableJpaRepositories(
        entityManagerFactoryRef = PrimaryDataSourceConfig.ENTITY_MANAGER_FACTORY,
        transactionManagerRef = PrimaryDataSourceConfig.TRANSACTION_MANAGER,
        basePackages = PrimaryDataSourceConfig.JPA_REPOSITORY_PACKAGE
)

@Slf4j
@Configuration
public class PrimaryDataSourceConfig {

    public static final String PROPERTY_PREFIX = "spring.datasource.primary";
    public static final String JPA_REPOSITORY_PACKAGE = "com.quartztop.bonus.repositoriesBonus";
    public static final String ENTITY_PACKAGE_USER = "com.quartztop.bonus.user";
    public static final String ENTITY_PACKAGE_TOKENS = "com.quartztop.bonus.tokens";
    public static final String ENTITY_PACKAGE_ORDERS = "com.quartztop.bonus.orders";
    public static final String ENTITY_MANAGER_FACTORY = "oneEntityManagerFactory";
    public static final String DATA_SOURCE = "oneDataSource";
    public static final String DATABASE_PROPERTY = "oneDatabaseProperty";
    public static final String TRANSACTION_MANAGER = "oneTransactionManager";

    @Bean(DATABASE_PROPERTY)
    @ConfigurationProperties(prefix = PROPERTY_PREFIX)
    public DatabaseProperty appDatabaseProperty() {
        DatabaseProperty databaseProperty = new DatabaseProperty();
        log.info("Loading Database Properties: {}", databaseProperty);
        return new DatabaseProperty();
    }


    @Bean(DATA_SOURCE)
    public DataSource appDataSource(
            @Qualifier(DATABASE_PROPERTY) DatabaseProperty databaseProperty
    ) {

        log.info("Database URL: {}", databaseProperty.getUrl());
        log.info("Database Username: {}", databaseProperty.getUsername());
        log.info("Database Driver: {}", databaseProperty.getClassDriver());

        return DataSourceBuilder
                .create()
                .username(databaseProperty.getUsername())
                .password(databaseProperty.getPassword())
                .url(databaseProperty.getUrl())
                .driverClassName(databaseProperty.getClassDriver())
                .build();
    }

    @Bean(ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean appEntityManager(
            @Qualifier(DATA_SOURCE) DataSource dataSource
    ) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPersistenceUnitName(ENTITY_MANAGER_FACTORY);
        em.setPackagesToScan(ENTITY_PACKAGE_USER, ENTITY_PACKAGE_TOKENS,ENTITY_PACKAGE_ORDERS);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        final HashMap<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(TRANSACTION_MANAGER)
    @Primary
    public PlatformTransactionManager sqlSessionTemplate(
            @Qualifier(ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean entityManager,
            @Qualifier(DATA_SOURCE) DataSource dataSource
    ) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManager.getObject());
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

}

