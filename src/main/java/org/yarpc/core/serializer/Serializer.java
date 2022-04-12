package org.yarpc.core.serializer;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public interface Serializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes);
}
