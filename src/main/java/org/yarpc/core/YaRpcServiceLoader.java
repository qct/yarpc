package org.yarpc.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.MapMaker;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Created by qdd on 2022/4/9.
 */
public final class YaRpcServiceLoader {

    public static final String SPI_RESOURCE_LOCATION = "META-INF/yarpc.spi";

    private static final Logger logger = LoggerFactory.getLogger(YaRpcServiceLoader.class);
    static final Map<ClassLoader, Map<String, List<String>>> cache =
            new MapMaker().weakKeys().makeMap();

    private YaRpcServiceLoader() {}

    public static <T> List<T> loadYaRpcSPIs(Class<T> spiType, @Nullable ClassLoader classLoader) {
        Preconditions.checkNotNull(spiType, "'spiType' must not be null");
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = YaRpcServiceLoader.class.getClassLoader();
        }
        List<String> implementationNames = loadSpiNames(spiType, classLoaderToUse);
        if (logger.isTraceEnabled()) {
            logger.trace("Loaded [" + spiType.getName() + "] names: " + implementationNames);
        }
        List<T> result = new ArrayList<>(implementationNames.size());
        for (String implementationName : implementationNames) {
            result.add(instantiateSpi(implementationName, spiType, classLoaderToUse));
        }
        // TODO support order
        // AnnotationAwareOrderComparator.sort(result);
        return result;
    }

    /**
     * Load the fully qualified class names of SPI implementations of the given type from {@value
     * #SPI_RESOURCE_LOCATION}, using the given class loader.
     * <p>As of Spring Framework 5.3, if a particular implementation class name
     * is discovered more than once for the given SPI type, duplicates will be ignored.
     *
     * @param spiType the interface or abstract class representing the SPI
     * @param classLoader the ClassLoader to use for loading resources; can be {@code null} to use the default
     * @throws IllegalArgumentException if an error occurs while loading SPI names
     */
    public static List<String> loadSpiNames(Class<?> spiType, @Nullable ClassLoader classLoader) {
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = YaRpcServiceLoader.class.getClassLoader();
        }
        String spiTypeName = spiType.getName();
        return loadYaRpcSPIs(classLoaderToUse).getOrDefault(spiTypeName, Collections.emptyList());
    }

    private static Map<String, List<String>> loadYaRpcSPIs(ClassLoader classLoader) {
        Map<String, List<String>> result = cache.get(classLoader);
        if (result != null) {
            return result;
        }

        result = new HashMap<>();
        try {
            Enumeration<URL> urls = classLoader.getResources(SPI_RESOURCE_LOCATION);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Properties properties = new Properties();
                ByteSource byteSource = Resources.asByteSource(url);
                try (InputStream inputStream = byteSource.openBufferedStream()) {
                    properties.load(inputStream);
                }
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    String spiTypeName = ((String) entry.getKey()).trim();
                    List<String> spiImplementationNames =
                            Splitter.on(",").trimResults().splitToList((String) entry.getValue());
                    for (String spiImplementationName : spiImplementationNames) {
                        result.computeIfAbsent(spiTypeName, key -> new ArrayList<>())
                                .add(spiImplementationName);
                    }
                }
            }

            // Replace all lists with unmodifiable lists containing unique elements
            result.replaceAll((spiType, implementations) -> implementations.stream()
                    .distinct()
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));
            cache.put(classLoader, result);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load SPIs from location [" + SPI_RESOURCE_LOCATION + "]", ex);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiateSpi(String spiImplementationName, Class<T> spiType, ClassLoader classLoader) {
        try {
            Class<?> spiImplementationClass = Class.forName(spiImplementationName, false, classLoader);
            if (!spiType.isAssignableFrom(spiImplementationClass)) {
                throw new IllegalArgumentException("Class [" + spiImplementationName
                        + "] is not assignable to SPI type [" + spiType.getName() + "]");
            }
            Constructor<?> ctor = spiImplementationClass.getDeclaredConstructor();
            if ((!Modifier.isPublic(ctor.getModifiers())
                            || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers()))
                    && !ctor.isAccessible()) {
                ctor.setAccessible(true);
            }
            return (T) ctor.newInstance();
        } catch (Throwable ex) {
            throw new IllegalArgumentException(
                    "Unable to instantiate SPI class [" + spiImplementationName + "] for SPI type [" + spiType.getName()
                            + "]",
                    ex);
        }
    }
}
