/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ResourceLocation
implements Comparable<ResourceLocation> {
    public static final Codec<ResourceLocation> CODEC = Codec.STRING.comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    protected ResourceLocation(String string, String string2, @Nullable Dummy dummy) {
        this.namespace = string;
        this.path = string2;
    }

    public ResourceLocation(String string, String string2) {
        this(ResourceLocation.assertValidNamespace(string, string2), ResourceLocation.assertValidPath(string, string2), null);
    }

    private ResourceLocation(String[] strings) {
        this(strings[0], strings[1]);
    }

    public ResourceLocation(String string) {
        this(ResourceLocation.decompose(string, ':'));
    }

    public static ResourceLocation of(String string, char c) {
        return new ResourceLocation(ResourceLocation.decompose(string, c));
    }

    @Nullable
    public static ResourceLocation tryParse(String string) {
        try {
            return new ResourceLocation(string);
        } catch (ResourceLocationException resourceLocationException) {
            return null;
        }
    }

    @Nullable
    public static ResourceLocation tryBuild(String string, String string2) {
        try {
            return new ResourceLocation(string, string2);
        } catch (ResourceLocationException resourceLocationException) {
            return null;
        }
    }

    protected static String[] decompose(String string, char c) {
        String[] strings = new String[]{DEFAULT_NAMESPACE, string};
        int i = string.indexOf(c);
        if (i >= 0) {
            strings[1] = string.substring(i + 1);
            if (i >= 1) {
                strings[0] = string.substring(0, i);
            }
        }
        return strings;
    }

    public static DataResult<ResourceLocation> read(String string) {
        try {
            return DataResult.success(new ResourceLocation(string));
        } catch (ResourceLocationException resourceLocationException) {
            return DataResult.error("Not a valid resource location: " + string + " " + resourceLocationException.getMessage());
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public ResourceLocation withPath(String string) {
        return new ResourceLocation(this.namespace, ResourceLocation.assertValidPath(this.namespace, string), null);
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
        }
        if (object instanceof ResourceLocation) {
            ResourceLocation resourceLocation = (ResourceLocation)object;
            return this.namespace.equals(resourceLocation.namespace) && this.path.equals(resourceLocation.path);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    @Override
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
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toLanguageKey();
    }

    public String toLanguageKey(String string) {
        return string + "." + this.toLanguageKey();
    }

    public static ResourceLocation read(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && ResourceLocation.isAllowedInResourceLocation(stringReader.peek())) {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        try {
            return new ResourceLocation(string);
        } catch (ResourceLocationException resourceLocationException) {
            stringReader.setCursor(i);
            throw ERROR_INVALID.createWithContext(stringReader);
        }
    }

    public static boolean isAllowedInResourceLocation(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    private static boolean isValidPath(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (ResourceLocation.validPathChar(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static boolean isValidNamespace(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (ResourceLocation.validNamespaceChar(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static String assertValidNamespace(String string, String string2) {
        if (!ResourceLocation.isValidNamespace(string)) {
            throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + string + ":" + string2);
        }
        return string;
    }

    public static boolean validPathChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.';
    }

    private static boolean validNamespaceChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    public static boolean isValidResourceLocation(String string) {
        String[] strings = ResourceLocation.decompose(string, ':');
        return ResourceLocation.isValidNamespace(StringUtils.isEmpty(strings[0]) ? DEFAULT_NAMESPACE : strings[0]) && ResourceLocation.isValidPath(strings[1]);
    }

    private static String assertValidPath(String string, String string2) {
        if (!ResourceLocation.isValidPath(string2)) {
            throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + string + ":" + string2);
        }
        return string2;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((ResourceLocation)object);
    }

    protected static interface Dummy {
    }

    public static class Serializer
    implements JsonDeserializer<ResourceLocation>,
    JsonSerializer<ResourceLocation> {
        @Override
        public ResourceLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new ResourceLocation(GsonHelper.convertToString(jsonElement, "location"));
        }

        @Override
        public JsonElement serialize(ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(resourceLocation.toString());
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((ResourceLocation)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

