package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Settings<T extends Settings<T>> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Properties properties;

	public Settings(Properties properties) {
		this.properties = properties;
	}

	public static Properties loadFromFile(Path path) {
		Properties properties = new Properties();

		try {
			InputStream inputStream = Files.newInputStream(path);

			try {
				properties.load(inputStream);
			} catch (Throwable var6) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException var7) {
			LOGGER.error("Failed to load properties from file: {}", path);
		}

		return properties;
	}

	public void store(Path path) {
		try {
			OutputStream outputStream = Files.newOutputStream(path);

			try {
				this.properties.store(outputStream, "Minecraft server properties");
			} catch (Throwable var6) {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException var7) {
			LOGGER.error("Failed to store properties to file: {}", path);
		}
	}

	private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> function) {
		return string -> {
			try {
				return (Number)function.apply(string);
			} catch (NumberFormatException var3) {
				return null;
			}
		};
	}

	protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> intFunction, Function<String, V> function) {
		return string -> {
			try {
				return intFunction.apply(Integer.parseInt(string));
			} catch (NumberFormatException var4) {
				return function.apply(string);
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
		} else {
			this.properties.remove(string);
			return (V)function.apply(string2);
		}
	}

	protected <V> V get(String string, Function<String, V> function, Function<V, String> function2, V object) {
		String string2 = this.getStringRaw(string);
		V object2 = MoreObjects.firstNonNull((V)(string2 != null ? function.apply(string2) : null), object);
		this.properties.put(string, function2.apply(object2));
		return object2;
	}

	protected <V> Settings<T>.MutableValue<V> getMutable(String string, Function<String, V> function, Function<V, String> function2, V object) {
		String string2 = this.getStringRaw(string);
		V object2 = MoreObjects.firstNonNull((V)(string2 != null ? function.apply(string2) : null), object);
		this.properties.put(string, function2.apply(object2));
		return new Settings.MutableValue<>(string, object2, function2);
	}

	protected <V> V get(String string, Function<String, V> function, UnaryOperator<V> unaryOperator, Function<V, String> function2, V object) {
		return this.get(string, stringx -> {
			V objectx = (V)function.apply(stringx);
			return objectx != null ? unaryOperator.apply(objectx) : null;
		}, function2, object);
	}

	protected <V> V get(String string, Function<String, V> function, V object) {
		return this.get(string, function, Objects::toString, object);
	}

	protected <V> Settings<T>.MutableValue<V> getMutable(String string, Function<String, V> function, V object) {
		return this.getMutable(string, function, Objects::toString, object);
	}

	protected String get(String string, String string2) {
		return this.get(string, Function.identity(), Function.identity(), string2);
	}

	@Nullable
	protected String getLegacyString(String string) {
		return this.getLegacy(string, Function.identity());
	}

	protected int get(String string, int i) {
		return this.get(string, wrapNumberDeserializer(Integer::parseInt), Integer.valueOf(i));
	}

	protected Settings<T>.MutableValue<Integer> getMutable(String string, int i) {
		return this.getMutable(string, wrapNumberDeserializer(Integer::parseInt), i);
	}

	protected int get(String string, UnaryOperator<Integer> unaryOperator, int i) {
		return this.get(string, wrapNumberDeserializer(Integer::parseInt), unaryOperator, Objects::toString, i);
	}

	protected long get(String string, long l) {
		return this.get(string, wrapNumberDeserializer(Long::parseLong), l);
	}

	protected boolean get(String string, boolean bl) {
		return this.get(string, Boolean::valueOf, bl);
	}

	protected Settings<T>.MutableValue<Boolean> getMutable(String string, boolean bl) {
		return this.getMutable(string, Boolean::valueOf, bl);
	}

	@Nullable
	protected Boolean getLegacyBoolean(String string) {
		return this.getLegacy(string, Boolean::valueOf);
	}

	protected Properties cloneProperties() {
		Properties properties = new Properties();
		properties.putAll(this.properties);
		return properties;
	}

	protected abstract T reload(RegistryAccess registryAccess, Properties properties);

	public class MutableValue<V> implements Supplier<V> {
		private final String key;
		private final V value;
		private final Function<V, String> serializer;

		MutableValue(String string, V object, Function<V, String> function) {
			this.key = string;
			this.value = object;
			this.serializer = function;
		}

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
