/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Properties properties;

    public Settings(Properties properties) {
        this.properties = properties;
    }

    public static Properties loadFromFile(Path path) {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
            properties.load(inputStream);
        } catch (IOException iOException) {
            LOGGER.error("Failed to load properties from file: {}", (Object)path);
        }
        return properties;
    }

    public void store(Path path) {
        try (OutputStream outputStream = Files.newOutputStream(path, new OpenOption[0]);){
            this.properties.store(outputStream, "Minecraft server properties");
        } catch (IOException iOException) {
            LOGGER.error("Failed to store properties to file: {}", (Object)path);
        }
    }

    private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> function) {
        return string -> {
            try {
                return (Number)function.apply((String)string);
            } catch (NumberFormatException numberFormatException) {
                return null;
            }
        };
    }

    protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> intFunction, Function<String, V> function) {
        return string -> {
            try {
                return intFunction.apply(Integer.parseInt(string));
            } catch (NumberFormatException numberFormatException) {
                return function.apply((String)string);
            }
        };
    }

    @Nullable
    private String getStringRaw(String string) {
        return (String)this.properties.get(string);
    }

    @Nullable
    protected <V> V getLegacy(String string, Function<String, V> function) {
        String string2 = this.getStringRaw(string);
        if (string2 == null) {
            return null;
        }
        this.properties.remove(string);
        return function.apply(string2);
    }

    protected <V> V get(String string, Function<String, V> function, Function<V, String> function2, V object) {
        String string2 = this.getStringRaw(string);
        V object2 = MoreObjects.firstNonNull(string2 != null ? (Object)function.apply(string2) : null, object);
        this.properties.put(string, function2.apply(object2));
        return object2;
    }

    protected <V> MutableValue<V> getMutable(String string, Function<String, V> function, Function<V, String> function2, V object) {
        String string2 = this.getStringRaw(string);
        Object object2 = MoreObjects.firstNonNull(string2 != null ? (Object)function.apply(string2) : null, object);
        this.properties.put(string, function2.apply(object2));
        return new MutableValue<Object>(string, object2, (Function<Object, String>)function2);
    }

    protected <V> V get(String string2, Function<String, V> function, UnaryOperator<V> unaryOperator, Function<V, String> function2, V object) {
        return (V)this.get(string2, string -> {
            Object object = function.apply((String)string);
            return object != null ? unaryOperator.apply(object) : null;
        }, function2, object);
    }

    protected <V> V get(String string, Function<String, V> function, V object) {
        return (V)this.get(string, function, Objects::toString, object);
    }

    protected <V> MutableValue<V> getMutable(String string, Function<String, V> function, V object) {
        return this.getMutable(string, function, Objects::toString, object);
    }

    protected String get(String string, String string2) {
        return this.get(string, Function.identity(), Function.identity(), string2);
    }

    @Nullable
    protected String getLegacyString(String string) {
        return (String)this.getLegacy(string, Function.identity());
    }

    protected int get(String string, int i) {
        return this.get(string, Settings.wrapNumberDeserializer(Integer::parseInt), Integer.valueOf(i));
    }

    protected MutableValue<Integer> getMutable(String string, int i) {
        return this.getMutable(string, Settings.wrapNumberDeserializer(Integer::parseInt), i);
    }

    protected int get(String string, UnaryOperator<Integer> unaryOperator, int i) {
        return this.get(string, Settings.wrapNumberDeserializer(Integer::parseInt), unaryOperator, Objects::toString, i);
    }

    protected long get(String string, long l) {
        return this.get(string, Settings.wrapNumberDeserializer(Long::parseLong), l);
    }

    protected boolean get(String string, boolean bl) {
        return this.get(string, Boolean::valueOf, bl);
    }

    protected MutableValue<Boolean> getMutable(String string, boolean bl) {
        return this.getMutable(string, Boolean::valueOf, bl);
    }

    @Nullable
    protected Boolean getLegacyBoolean(String string) {
        return this.getLegacy(string, Boolean::valueOf);
    }

    protected Properties cloneProperties() {
        Properties properties = new Properties();
        properties.putAll((Map<?, ?>)this.properties);
        return properties;
    }

    protected abstract T reload(RegistryAccess var1, Properties var2);

    public class MutableValue<V>
    implements Supplier<V> {
        private final String key;
        private final V value;
        private final Function<V, String> serializer;

        MutableValue(String string, V object, Function<V, String> function) {
            this.key = string;
            this.value = object;
            this.serializer = function;
        }

        @Override
        public V get() {
            return this.value;
        }

        public T update(RegistryAccess registryAccess, V object) {
            Properties properties = Settings.this.cloneProperties();
            properties.put(this.key, this.serializer.apply(object));
            return Settings.this.reload(registryAccess, properties);
        }
    }
}

