package com.github.karlnicholas.merchloan.servicerequest;

import com.github.karlnicholas.merchloan.sqlutil.SqlInitialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
@EnableScheduling
@EnableAsync
public class ServiceRequestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRequestApplication.class, args);
    }

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() throws SQLException, IOException {
        try(Connection con = dataSource.getConnection()) {
            SqlInitialization.initialize(con, ServiceRequestApplication.class.getResourceAsStream("/sql/schema.sql"));
        }
    }

//    @Bean
//    public HttpMessageConverter<UUID> converter() {
//
//        return new HttpMessageConverter<>() {
//            @Override
//            public boolean canRead(Class<?> clazz, MediaType mediaType) {
//                return clazz == UUID.class;
//            }
//
//            @Override
//            public boolean canWrite(Class<?> clazz, MediaType mediaType) {
//                return clazz == UUID.class;
//            }
//
//            @Override
//            public List<MediaType> getSupportedMediaTypes() {
//                return Arrays.asList(MediaType.TEXT_PLAIN);
//            }
//
//            @Override
//            public UUID read(Class<? extends UUID> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
//                return UUID.fromString(new String(inputMessage.getBody().readAllBytes()));
//            }
//
//            @Override
//            public void write(UUID uuid, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
//                outputMessage.getBody().write(uuid.toString().getBytes());
//            }
//        };
//    }
}
