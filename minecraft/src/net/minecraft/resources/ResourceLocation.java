package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.GsonHelper;

public final class ResourceLocation implements Comparable<ResourceLocation> {
	public static final Codec<ResourceLocation> CODEC = Codec.STRING.<ResourceLocation>comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
	public static final StreamCodec<ByteBuf, ResourceLocation> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ResourceLocation::parse, ResourceLocation::toString);
	public static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));
	public static final char NAMESPACE_SEPARATOR = ':';
	public static final String DEFAULT_NAMESPACE = "minecraft";
	public static final String REALMS_NAMESPACE = "realms";
	private final String namespace;
	private final String path;

	private ResourceLocation(String string, String string2) {
		assert isValidNamespace(string);

		assert isValidPath(string2);

		this.namespace = string;
		this.path = string2;
	}

	private static ResourceLocation createUntrusted(String string, String string2) {
		return new ResourceLocation(assertValidNamespace(string, string2), assertValidPath(string, string2));
	}

	public static ResourceLocation fromNamespaceAndPath(String string, String string2) {
		return createUntrusted(string, string2);
	}

	public static ResourceLocation parse(String string) {
		return bySeparator(string, ':');
	}

	public static ResourceLocation withDefaultNamespace(String string) {
		return new ResourceLocation("minecraft", assertValidPath("minecraft", string));
	}

	@Nullable
	public static ResourceLocation tryParse(String string) {
		return tryBySeparator(string, ':');
	}

	@Nullable
	public static ResourceLocation tryBuild(String string, String string2) {
		return isValidNamespace(string) && isValidPath(string2) ? new ResourceLocation(string, string2) : null;
	}

	public static ResourceLocation bySeparator(String string, char c) {
		int i = string.indexOf(c);
		if (i >= 0) {
			String string2 = string.substring(i + 1);
			if (i != 0) {
				String string3 = string.substring(0, i);
				return createUntrusted(string3, string2);
			} else {
				return withDefaultNamespace(string2);
			}
		} else {
			return withDefaultNamespace(string);
		}
	}

	@Nullable
	public static ResourceLocation tryBySeparator(String string, char c) {
		int i = string.indexOf(c);
		if (i >= 0) {
			String string2 = string.substring(i + 1);
			if (!isValidPath(string2)) {
				return null;
			} else if (i != 0) {
				String string3 = string.substring(0, i);
				return isValidNamespace(string3) ? new ResourceLocation(string3, string2) : null;
			} else {
				return new ResourceLocation("minecraft", string2);
			}
		} else {
			return isValidPath(string) ? new ResourceLocation("minecraft", string) : null;
		}
	}

	public static DataResult<ResourceLocation> read(String string) {
		try {
			return DataResult.success(parse(string));
		} catch (ResourceLocationException var2) {
			return DataResult.error(() -> "Not a valid resource location: " + string + " " + var2.getMessage());
		}
	}

	public String getPath() {
		return this.path;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public ResourceLocation withPath(String string) {
		return new ResourceLocation(this.namespace, assertValidPath(this.namespace, string));
	}

	public ResourceLocation withPath(UnaryOperator<String> unaryOperator) {
		return this.withPath((String)unaryOperator.apply(this.path));
	}

	public ResourceLocation withPrefix(String string) {
		return this.withPath(string + this.path);
	}

	public ResourceLocation withSuffix(String string) {
		return this.withPath(this.path + string);
	}

	public String toString() {
		return this.namespace + ":" + this.path;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof ResourceLocation resourceLocation)
				? false
				: this.namespace.equals(resourceLocation.namespace) && this.path.equals(resourceLocation.path);
		}
	}

	public int hashCode() {
		return 31 * this.namespace.hashCode() + this.path.hashCode();
	}

	public int compareTo(ResourceLocation resourceLocation) {
		int i = this.path.compareTo(resourceLocation.path);
		if (i == 0) {
			i = this.namespace.compareTo(resourceLocation.namespace);
		}

		return i;
	}

	public String toDebugFileName() {
		return this.toString().replace('/', '_').replace(':', '_');
	}

	public String toLanguageKey() {
		return this.namespace + "." + this.path;
	}

	public String toShortLanguageKey() {
		return this.namespace.equals("minecraft") ? this.path : this.toLanguageKey();
	}

	public String toLanguageKey(String string) {
		return string + "." + this.toLanguageKey();
	}

	public String toLanguageKey(String string, String string2) {
		return string + "." + this.toLanguageKey() + "." + string2;
	}

	private static String readGreedy(StringReader stringReader) {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && isAllowedInResourceLocation(stringReader.peek())) {
			stringReader.skip();
		}

		return stringReader.getString().substring(i, stringReader.getCursor());
	}

	public static ResourceLocation read(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		String string = readGreedy(stringReader);

		try {
			return parse(string);
		} catch (ResourceLocationException var4) {
			stringReader.setCursor(i);
			throw ERROR_INVALID.createWithContext(stringReader);
		}
	}

	public static ResourceLocation readNonEmpty(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		String string = readGreedy(stringReader);
		if (string.isEmpty()) {
			throw ERROR_INVALID.createWithContext(stringReader);
		} else {
			try {
				return parse(string);
			} catch (ResourceLocationException var4) {
				stringReader.setCursor(i);
				throw ERROR_INVALID.createWithContext(stringReader);
			}
		}
	}

	public static boolean isAllowedInResourceLocation(char c) {
		return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
	}

	public static boolean isValidPath(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (!validPathChar(string.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	public static boolean isValidNamespace(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (!validNamespaceChar(string.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	private static String assertValidNamespace(String string, String string2) {
		if (!isValidNamespace(string)) {
			throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + string + ":" + string2);
		} else {
			return string;
		}
	}

	public static boolean validPathChar(char c) {
		return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.';
	}

	private static boolean validNamespaceChar(char c) {
		return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
	}

	private static String assertValidPath(String string, String string2) {
		if (!isValidPath(string2)) {
			throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + string + ":" + string2);
		} else {
			return string2;
		}
	}

	public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
		public ResourceLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return ResourceLocation.parse(GsonHelper.convertToString(jsonElement, "location"));
		}

		public JsonElement serialize(ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(resourceLocation.toString());
		}
	}
}
