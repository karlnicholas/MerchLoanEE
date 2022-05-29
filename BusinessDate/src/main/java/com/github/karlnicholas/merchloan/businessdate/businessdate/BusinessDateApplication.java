package com.github.karlnicholas.merchloan.businessdate.businessdate;

import com.github.karlnicholas.merchloan.businessdate.businessdate.service.BusinessDateService;
import com.github.karlnicholas.merchloan.sqlutil.SqlInitialization;

import javax.sql.DataSource;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

    @ApplicationPath("/api")
public class BusinessDateApplication {

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
//
}
