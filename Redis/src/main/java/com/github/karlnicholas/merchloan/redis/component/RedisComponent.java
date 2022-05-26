package com.github.karlnicholas.merchloan.redis.component;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;

import javax.enterprise.context.ApplicationScoped;
import java.nio.ByteBuffer;
import java.time.LocalDate;

@ApplicationScoped
public class RedisComponent {
    private final RedisClient client;

    public RedisComponent() {
        client = RedisClient.create("redis://localhost");
    }


    public void updateBusinessDate(LocalDate businessDate) {
        try ( StatefulRedisConnection<Long, LocalDate> connection = client.connect(new LocalLocalDateRedisCodec()) ) {
            RedisCommands<Long, LocalDate> commands = connection.sync();
            commands.getStatefulConnection().sync().set(1L, businessDate);
        }
    }

    public LocalDate getBusinessDate() {
        try ( StatefulRedisConnection<Long, LocalDate> connection = client.connect(new LocalLocalDateRedisCodec()) ) {
            RedisCommands<Long, LocalDate> commands = connection.sync();
            return commands.getStatefulConnection().sync().get(1L);
        }
    }

}
