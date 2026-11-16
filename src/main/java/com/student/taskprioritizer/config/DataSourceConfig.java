package com.student.taskprioritizer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.net.URI;

/**
 * SQLite locally; PostgreSQL when {@code DATABASE_URL} is set (Render-style).
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String sqliteUrl) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank() && isPostgres(databaseUrl)) {
            PostgresConnection pg = parsePostgres(databaseUrl);
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(pg.jdbcUrl());
            dataSource.setUsername(pg.username());
            dataSource.setPassword(pg.password());
            return dataSource;
        }
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl(sqliteUrl);
        return dataSource;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernateDialectCustomizer() {
        return props -> {
            String databaseUrl = System.getenv("DATABASE_URL");
            if (databaseUrl != null && !databaseUrl.isBlank() && isPostgres(databaseUrl)) {
                props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            } else {
                props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
            }
        };
    }

    private static boolean isPostgres(String url) {
        return url.startsWith("postgres://")
                || url.startsWith("postgresql://")
                || url.startsWith("jdbc:postgresql:");
    }

    private static PostgresConnection parsePostgres(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:postgresql:")) {
            return new PostgresConnection(databaseUrl, null, null);
        }
        URI uri = URI.create(databaseUrl.replaceFirst("^postgres://", "postgresql://"));
        String userInfo = uri.getUserInfo();
        String username = "";
        String password = "";
        if (userInfo != null) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = userInfo.substring(0, colon);
                password = userInfo.substring(colon + 1);
            } else {
                username = userInfo;
            }
        }
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbcUrl += "?" + uri.getQuery();
        }
        return new PostgresConnection(jdbcUrl, username, password);
    }

    private record PostgresConnection(String jdbcUrl, String username, String password) {}
}
