package com.github.karlnicholas.merchloan.accounts.config;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.UUID;

public class SqlUtils {
    public static final int DUPLICATE_ERROR = 23505;
    public static byte[] uuidToBytes(final UUID uuid) {
        if (Objects.isNull(uuid)) {
            return null;
        }

        final byte[] uuidAsBytes = new byte[16];

        ByteBuffer.wrap(uuidAsBytes)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());

        return uuidAsBytes;
    }

    public static UUID toUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
}
