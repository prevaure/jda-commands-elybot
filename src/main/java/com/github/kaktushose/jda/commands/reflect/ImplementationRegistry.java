package com.github.kaktushose.jda.commands.reflect;

import com.github.kaktushose.jda.commands.annotations.Component;
import com.github.kaktushose.jda.commands.permissions.DefaultPermissionsProvider;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import com.github.kaktushose.jda.commands.settings.SettingsProvider;
import com.github.kaktushose.jda.commands.settings.DefaultSettingsProvider;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class ImplementationRegistry {

    private static final Logger log = LoggerFactory.getLogger(ImplementationRegistry.class);
    private SettingsProvider settingsProvider;
    private PermissionsProvider permissionsProvider;

    public ImplementationRegistry() {
        settingsProvider = new DefaultSettingsProvider();
        permissionsProvider = new DefaultPermissionsProvider();
    }

    public void index(String... packages) {
        log.debug("Indexing custom implementations...");
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setScanners(new SubTypesScanner())
                .setUrls(ClasspathHelper.forClass(getClass()))
                .filterInputsBy(new FilterBuilder().includePackage(packages));
        Reflections reflections = new Reflections(config);

        Set<Class<? extends SettingsProvider>> settingsProviders = reflections.getSubTypesOf(SettingsProvider.class);
        for (Class<?> clazz : settingsProviders) {
            if (!clazz.isAnnotationPresent(Component.class)) {
                continue;
            }
            log.debug("Found {}", clazz.getName());
            try {
                settingsProvider = (SettingsProvider) clazz.getConstructor().newInstance();
                break;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Unable to create an instance of the custom implementation!", e);
            }
        }

        Set<Class<? extends PermissionsProvider>> permissionsProviders = reflections.getSubTypesOf(PermissionsProvider.class);
        for (Class<?> clazz : permissionsProviders) {
            if (!clazz.isAnnotationPresent(Component.class)) {
                continue;
            }
            log.debug("Found {}", clazz.getName());
            try {
                permissionsProvider = (PermissionsProvider) clazz.getConstructor().newInstance();
                break;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Unable to create an instance of the custom implementation!", e);
            }
        }

    }

    public void setSettingsProvider(SettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public SettingsProvider getSettingsProvider() {
        return settingsProvider;
    }

    public PermissionsProvider getPermissionsProvider() {
        return permissionsProvider;
    }

    public void setPermissionsProvider(PermissionsProvider permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
    }
}
