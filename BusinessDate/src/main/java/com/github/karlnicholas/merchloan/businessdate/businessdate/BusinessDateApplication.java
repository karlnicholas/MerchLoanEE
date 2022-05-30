package com.github.karlnicholas.merchloan.businessdate.businessdate;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
public class BusinessDateApplication extends Application {

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
