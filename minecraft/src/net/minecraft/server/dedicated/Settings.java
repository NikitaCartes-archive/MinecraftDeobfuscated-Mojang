package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
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
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final Properties properties;

	public Settings(Properties properties) {
		this.properties = properties;
	}

	public static Properties loadFromFile(Path path) {
		try {
			try {
				InputStream inputStream = Files.newInputStream(path);

				Properties var13;
				try {
					CharsetDecoder charsetDecoder = StandardCharsets.UTF_8
						.newDecoder()
						.onMalformedInput(CodingErrorAction.REPORT)
						.onUnmappableCharacter(CodingErrorAction.REPORT);
					Properties properties = new Properties();
					properties.load(new InputStreamReader(inputStream, charsetDecoder));
					var13 = properties;
				} catch (Throwable var8) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var6) {
							var8.addSuppressed(var6);
						}
					}

					throw var8;
				}

				if (inputStream != null) {
					inputStream.close();
				}

				return var13;
			} catch (CharacterCodingException var9) {
				LOGGER.info("Failed to load properties as UTF-8 from file {}, trying ISO_8859_1", path);
				Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1);

				Properties var4;
				try {
					Properties properties = new Properties();
					properties.load(reader);
					var4 = properties;
				} catch (Throwable var7) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var5) {
							var7.addSuppressed(var5);
						}
					}

					throw var7;
				}

				if (reader != null) {
					reader.close();
				}

				return var4;
			}
		} catch (IOException var10) {
			LOGGER.error("Failed to load properties from file: {}", path, var10);
			return new Properties();
		}
	}

	public void store(Path path) {
		try {
			Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

			try {
				this.properties.store(writer, "Minecraft server properties");
			} catch (Throwable var6) {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (writer != null) {
				writer.close();
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

		MutableValue(final String string, final V object, final Function<V, String> function) {
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
