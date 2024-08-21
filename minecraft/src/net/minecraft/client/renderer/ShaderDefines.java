package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ShaderDefines(Map<String, String> values, Set<String> flags) {
	public static final ShaderDefines EMPTY = new ShaderDefines(Map.of(), Set.of());
	public static final Codec<ShaderDefines> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("values", Map.of()).forGetter(ShaderDefines::values),
					Codec.STRING.listOf().<Set>xmap(Set::copyOf, List::copyOf).optionalFieldOf("flags", Set.of()).forGetter(ShaderDefines::flags)
				)
				.apply(instance, ShaderDefines::new)
	);

	public static ShaderDefines.Builder builder() {
		return new ShaderDefines.Builder();
	}

	public ShaderDefines withOverrides(ShaderDefines shaderDefines) {
		if (this.isEmpty()) {
			return shaderDefines;
		} else if (shaderDefines.isEmpty()) {
			return this;
		} else {
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builderWithExpectedSize(this.values.size() + shaderDefines.values.size());
			builder.putAll(this.values);
			builder.putAll(shaderDefines.values);
			ImmutableSet.Builder<String> builder2 = ImmutableSet.builderWithExpectedSize(this.flags.size() + shaderDefines.flags.size());
			builder2.addAll(this.flags);
			builder2.addAll(shaderDefines.flags);
			return new ShaderDefines(builder.buildKeepingLast(), builder2.build());
		}
	}

	public String asSourceDirectives() {
		StringBuilder stringBuilder = new StringBuilder();

		for (Entry<String, String> entry : this.values.entrySet()) {
			String string = (String)entry.getKey();
			String string2 = (String)entry.getValue();
			stringBuilder.append("#define ").append(string).append(" ").append(string2).append('\n');
		}

		for (String string3 : this.flags) {
			stringBuilder.append("#define ").append(string3).append('\n');
		}

		return stringBuilder.toString();
	}

	public boolean isEmpty() {
		return this.values.isEmpty() && this.flags.isEmpty();
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final ImmutableMap.Builder<String, String> values = ImmutableMap.builder();
		private final ImmutableSet.Builder<String> flags = ImmutableSet.builder();

		Builder() {
		}

		public ShaderDefines.Builder define(String string, String string2) {
			if (string2.isBlank()) {
				throw new IllegalArgumentException("Cannot define empty string");
			} else {
				this.values.put(string, escapeNewLines(string2));
				return this;
			}
		}

		private static String escapeNewLines(String string) {
			return string.replaceAll("\n", "\\\\\n");
		}

		public ShaderDefines.Builder define(String string, float f) {
			this.values.put(string, String.valueOf(f));
			return this;
		}

		public ShaderDefines.Builder define(String string) {
			this.flags.add(string);
			return this;
		}

		public ShaderDefines build() {
			return new ShaderDefines(this.values.build(), this.flags.build());
		}
	}
}
