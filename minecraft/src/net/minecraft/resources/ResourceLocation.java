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
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ResourceLocation implements Comparable<ResourceLocation> {
	private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(new TranslatableComponent("argument.id.invalid"));
	protected final String namespace;
	protected final String path;

	protected ResourceLocation(String[] strings) {
		this.namespace = StringUtils.isEmpty(strings[0]) ? "minecraft" : strings[0];
		this.path = strings[1];
		if (!isValidNamespace(this.namespace)) {
			throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + this.namespace + ':' + this.path);
		} else if (!isValidPath(this.path)) {
			throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + this.namespace + ':' + this.path);
		}
	}

	public ResourceLocation(String string) {
		this(decompose(string, ':'));
	}

	public ResourceLocation(String string, String string2) {
		this(new String[]{string, string2});
	}

	public static ResourceLocation of(String string, char c) {
		return new ResourceLocation(decompose(string, c));
	}

	@Nullable
	public static ResourceLocation tryParse(String string) {
		try {
			return new ResourceLocation(string);
		} catch (ResourceLocationException var2) {
			return null;
		}
	}

	protected static String[] decompose(String string, char c) {
		String[] strings = new String[]{"minecraft", string};
		int i = string.indexOf(c);
		if (i >= 0) {
			strings[1] = string.substring(i + 1, string.length());
			if (i >= 1) {
				strings[0] = string.substring(0, i);
			}
		}

		return strings;
	}

	public String getPath() {
		return this.path;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public String toString() {
		return this.namespace + ':' + this.path;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof ResourceLocation)) {
			return false;
		} else {
			ResourceLocation resourceLocation = (ResourceLocation)object;
			return this.namespace.equals(resourceLocation.namespace) && this.path.equals(resourceLocation.path);
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

	public static ResourceLocation read(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && isAllowedInResourceLocation(stringReader.peek())) {
			stringReader.skip();
		}

		String string = stringReader.getString().substring(i, stringReader.getCursor());

		try {
			return new ResourceLocation(string);
		} catch (ResourceLocationException var4) {
			stringReader.setCursor(i);
			throw ERROR_INVALID.createWithContext(stringReader);
		}
	}

	public static boolean isAllowedInResourceLocation(char c) {
		return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
	}

	private static boolean isValidPath(String string) {
		return string.chars().allMatch(i -> i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 48 && i <= 57 || i == 47 || i == 46);
	}

	private static boolean isValidNamespace(String string) {
		return string.chars().allMatch(i -> i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 48 && i <= 57 || i == 46);
	}

	@Environment(EnvType.CLIENT)
	public static boolean isValidResourceLocation(String string) {
		String[] strings = decompose(string, ':');
		return isValidNamespace(StringUtils.isEmpty(strings[0]) ? "minecraft" : strings[0]) && isValidPath(strings[1]);
	}

	public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
		public ResourceLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return new ResourceLocation(GsonHelper.convertToString(jsonElement, "location"));
		}

		public JsonElement serialize(ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
			return new JsonPrimitive(resourceLocation.toString());
		}
	}
}
