package org.yarpc.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Created by qdd on 2022/4/10.
 */
public class HelloImpl implements Hello {

    private static final Logger logger = LoggerFactory.getLogger(HelloImpl.class);

    @Override
    public String sayHi(String hi) {
        logger.info("hello " + hi);
        return "hello " + hi;
    }
}
