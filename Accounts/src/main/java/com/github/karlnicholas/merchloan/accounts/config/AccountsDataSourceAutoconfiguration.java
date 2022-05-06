package com.github.karlnicholas.merchloan.accounts.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AccountsDataSourceAutoconfiguration {
	@Value("${accountsdb.host:localhost}")
	private String accountsdbHost;

	@Value("${maximumPoolSize:100}")
	private String maximumPoolSize;
	@Value("${minimumIdle:2}")
	private String minimumIdle;
	@Value("${useServerPrepStmts:true}")
	private String useServerPrepStmts;
	@Value("${cachePrepStmts:true}")
	private String cachePrepStmts;
	@Value("${prepStmtCacheSize:256}")
	private String prepStmtCacheSize;
	@Value("${prepStmtCacheSqlLimit:2048}")
	private String prepStmtCacheSqlLimit;

	@Bean
	public DataSource getDataSource() {
		HikariConfig config = new HikariConfig();
		String databaseUrl = "jdbc:h2:tcp://" + accountsdbHost + ":9101/mem:accounts;DB_CLOSE_DELAY=-1";
		config.setJdbcUrl(databaseUrl);

		config.addDataSourceProperty("maximumPoolSize", maximumPoolSize);
		config.addDataSourceProperty("minimumIdle", minimumIdle);
		config.addDataSourceProperty("useServerPrepStmts", useServerPrepStmts);
		config.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
		config.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
		config.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);
		return new HikariDataSource(config);
	}
}
