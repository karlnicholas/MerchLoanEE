package com.github.karlnicholas.merchloan.redis.component;

import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.time.LocalDate;

public class LocalLocalDateRedisCodec implements RedisCodec<Long, LocalDate> {
    @Override
    public Long decodeKey(ByteBuffer byteBuffer) {
        return byteBuffer.rewind().getLong();
    }

    @Override
    public LocalDate decodeValue(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        return LocalDate.of(byteBuffer.getInt(), byteBuffer.getInt(), byteBuffer.getInt());
    }

    @Override
    public ByteBuffer encodeKey(Long aLong) {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        return bb.putLong(aLong);
    }

    @Override
    public ByteBuffer encodeValue(LocalDate localDate) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES*3);
        bb.putInt(localDate.getYear());
        bb.putInt(localDate.getMonthValue());
        return bb.putInt(localDate.getDayOfMonth());
    }
}
