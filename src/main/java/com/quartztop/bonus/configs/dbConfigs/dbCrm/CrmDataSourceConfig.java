package com.quartztop.bonus.configs.dbConfigs.dbCrm;


import com.quartztop.bonus.configs.dbConfigs.dbBonus.DatabaseProperty;
import com.quartztop.bonus.configs.dbConfigs.dbBonus.PrimaryDataSourceConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@EnableJpaRepositories(
        entityManagerFactoryRef = CrmDataSourceConfig.ENTITY_MANAGER_FACTORY,
        transactionManagerRef = CrmDataSourceConfig.TRANSACTION_MANAGER,
        basePackages = CrmDataSourceConfig.JPA_REPOSITORY_PACKAGE
)

@Configuration
public class CrmDataSourceConfig {


    public static final String PROPERTY_PREFIX = "spring.datasource.crm";
    public static final String JPA_REPOSITORY_PACKAGE = "com.quartztop.bonus.repositoriesCrm";
    public static final String ENTITY_PACKAGE_CRM = "com.quartztop.bonus.crm";
    public static final String ENTITY_MANAGER_FACTORY = "crmEntityManagerFactory";
    public static final String DATA_SOURCE = "crmDataSource";
    public static final String DATABASE_PROPERTY = "crmDatabaseProperty";
    public static final String TRANSACTION_MANAGER = "crmTransactionManager";

    @Bean(DATABASE_PROPERTY)
    @ConfigurationProperties(prefix = PROPERTY_PREFIX)
    public com.quartztop.bonus.configs.dbConfigs.dbBonus.DatabaseProperty appDatabaseProperty() {
        com.quartztop.bonus.configs.dbConfigs.dbBonus.DatabaseProperty databaseProperty = new com.quartztop.bonus.configs.dbConfigs.dbBonus.DatabaseProperty();
        return new com.quartztop.bonus.configs.dbConfigs.dbBonus.DatabaseProperty();
    }


    @Bean(DATA_SOURCE)
    public DataSource appDataSource(
            @Qualifier(DATABASE_PROPERTY) DatabaseProperty databaseProperty
    ) {
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
        em.setPackagesToScan(ENTITY_PACKAGE_CRM);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        final HashMap<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        properties.put("hibernate.hbm2ddl.auto", "validate");
        //properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(TRANSACTION_MANAGER)
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
