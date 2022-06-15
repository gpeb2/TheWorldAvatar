package com.cmclinnovations.services;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cmclinnovations.FileUtils;
import com.cmclinnovations.services.config.Connection;
import com.cmclinnovations.services.config.ServiceConfig;

public abstract class AbstractService implements Service {

    private static final Map<String, Class<? extends Service>> typeMap = new HashMap<>();

    static {
        try {
            Collection<URI> listFiles = FileUtils.listFiles(AbstractService.class
                    .getResource("/" + AbstractService.class.getPackage().getName().replace('.', '/')), ".class");
            for (URI classFile : listFiles) {
                String path = classFile.toURL().getPath();
                path = path.replaceFirst(".*!/([^.]*)\\.class", "$1").replace("/", ".");
                Class<?> clazz = AbstractService.class.getClassLoader().loadClass(path);
                if (!Modifier.isAbstract(clazz.getModifiers()) && AbstractService.class.isAssignableFrom(clazz)) {
                    addTypeClass(clazz);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void addTypeClass(Class<?> clazz) {
        try {
            Field typeField = clazz.getField("TYPE");
            typeMap.put(((String) typeField.get(null)).toLowerCase(), clazz.asSubclass(Service.class));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                | SecurityException ex) {
            throw new RuntimeException("The class '" + clazz.getSimpleName()
                    + "' extends AbstractService but does not have a static String field called 'TYPE", ex);
        }
    }

    static <S extends Service> Class<S> getTypeClass(String type) {
        return (Class<S>) typeMap.get(type.toLowerCase());
    }

    private final ServiceConfig config;
    private final ServiceManager serviceManager;

    AbstractService(ServiceManager serviceManager, ServiceConfig config) {
        Objects.requireNonNull(config, "Services require a 'ServiceConfig' to be specified.");
        this.serviceManager = serviceManager;
        this.config = config;
    }

    protected final ServiceConfig getConfig() {
        return config;
    }

    public final String getName() {
        return checkPropertyNonNull("name", config.getName());
    }

    public final String getUsername() {
        return checkPropertyNonNull("username", config.getUsername());
    }

    public final String getPasswordFile() {
        return checkPropertyNonNull("passwordFile", config.getPasswordFile());
    }

    public final String getPassword() {
        return config.getPassword();
    }

    public final Map<String, Connection> getEndpoints() {
        return config.getEndpoints();
    }

    public final Connection getEndpoint(String endpointName) {
        return getEndpoints().get(endpointName);
    }

    private final <T> T checkPropertyNonNull(String propertyName, T value) {
        Objects.requireNonNull(value,
                "The service '" + config.getName() + "' requires the '" + propertyName
                        + "' property to be specified in its config file.");
        return value;
    }

    final <S extends Service> S getService(String otherServiceName) {
        return serviceManager.getService(otherServiceName);
    }
}
