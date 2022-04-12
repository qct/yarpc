package org.yarpc.support;

/**
 * <p>Created by qdd on 2022/4/11.
 */
public class HelloImpl2 implements Hello{

    @Override
    public String sayHi(String hi) {
        return "hello2 " + hi;
    }
}
