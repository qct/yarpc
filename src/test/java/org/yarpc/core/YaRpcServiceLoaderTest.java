package org.yarpc.core;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarpc.support.Hello;

/**
 * <p>Created by qdd on 2022/4/11.
 */
class YaRpcServiceLoaderTest {

    @Test
    void loadYaRpcSPIs() {
        List<Hello> list = YaRpcServiceLoader.loadYaRpcSPIs(Hello.class, null);
        Assertions.assertThat(list).hasSize(2);
    }
}