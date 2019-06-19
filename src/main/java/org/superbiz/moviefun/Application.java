package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${vcap.services}") String vCapServices) {
        return new DatabaseServiceCredentials(vCapServices);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Primary
    @Bean(name = "moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {

//        MysqlDataSource moviesBean = new MysqlDataSource();
//        moviesBean.setURL(serviceCredentials.jdbcUrl("movies-mysql"));

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));

        return hikariDataSource;
    }

    @Bean(name = "albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return hikariDataSource;
    }

    @Bean(name = "moviesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean moviesLocalContainerEntityManagerFactoryBean( @Qualifier("moviesDataSource") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter  ){
        LocalContainerEntityManagerFactoryBean moviesLocalContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        moviesLocalContainerEntityManagerFactoryBean.setDataSource(dataSource);
        moviesLocalContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        moviesLocalContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        moviesLocalContainerEntityManagerFactoryBean.setPersistenceUnitName("movies");
        return moviesLocalContainerEntityManagerFactoryBean;
    }

    @Bean(name = "albumsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean albumsLocalContainerEntityManagerFactoryBean( @Qualifier("albumsDataSource") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter  ){
        LocalContainerEntityManagerFactoryBean albumsLocalContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        albumsLocalContainerEntityManagerFactoryBean.setDataSource(dataSource);
        albumsLocalContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        albumsLocalContainerEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        albumsLocalContainerEntityManagerFactoryBean.setPersistenceUnitName("albums");
        return albumsLocalContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager( @Qualifier("moviesEntityManagerFactory") EntityManagerFactory entityManagerFactory ){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager( @Qualifier("albumsEntityManagerFactory") EntityManagerFactory entityManagerFactory ){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }

}
