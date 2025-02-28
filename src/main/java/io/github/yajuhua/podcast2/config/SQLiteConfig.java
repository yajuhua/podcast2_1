package io.github.yajuhua.podcast2.config;

import com.zaxxer.hikari.HikariDataSource;
import io.github.yajuhua.podcast2.common.properties.DataPathProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Configuration
@Slf4j
public class SQLiteConfig {

    @Autowired
    private DataPathProperties dataPathProperties;



    @Bean
    public DataSource dataSource(){
        File file = new File(dataPathProperties.getSqliteFilePath());
        String[] dirs = new String[]{"cert","config","database","logs","plugin","resources","tmp"};
        for (String dir : dirs) {
            File d = new File(dataPathProperties.getDataPath() + File.separator + dir);
            if (!d.exists()){
                log.info("创建目录:{}",d.getAbsolutePath());
                d.mkdirs();
            }
        }
        if (!file.exists()){
            log.info("初始化数据库文件...");
            try {
                InputStream inputStream = new ClassPathResource("static/template/db.db").getInputStream();
                FileOutputStream outputStream = new FileOutputStream(file);
                IOUtils.copyLarge(inputStream,outputStream);
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                // 复制过程中出现异常
                e.printStackTrace();
            }
        }
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(10);
        dataSource.setMaxLifetime(0);
        dataSource.setIdleTimeout(60000);
        dataSource.setConnectionTimeout(60000);
        dataSource.setJdbcUrl(dataPathProperties.getSqliteUrl());
        return dataSource;
    }
}
