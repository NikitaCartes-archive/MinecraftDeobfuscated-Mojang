package net.minecraft.client.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record ShaderProgramConfig(
	ResourceLocation vertex,
	ResourceLocation fragment,
	List<ShaderProgramConfig.Sampler> samplers,
	List<ShaderProgramConfig.Uniform> uniforms,
	ShaderDefines defines
) {
	public static final Codec<ShaderProgramConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("vertex").forGetter(ShaderProgramConfig::vertex),
					ResourceLocation.CODEC.fieldOf("fragment").forGetter(ShaderProgramConfig::fragment),
					ShaderProgramConfig.Sampler.CODEC.listOf().optionalFieldOf("samplers", List.of()).forGetter(ShaderProgramConfig::samplers),
					ShaderProgramConfig.Uniform.CODEC.listOf().optionalFieldOf("uniforms", List.of()).forGetter(ShaderProgramConfig::uniforms),
					ShaderDefines.CODEC.optionalFieldOf("defines", ShaderDefines.EMPTY).forGetter(ShaderProgramConfig::defines)
				)
				.apply(instance, ShaderProgramConfig::new)
	);

	@Environment(EnvType.CLIENT)
	public static record Sampler(String name) {
		public static final Codec<ShaderProgramConfig.Sampler> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(Codec.STRING.fieldOf("name").forGetter(ShaderProgramConfig.Sampler::name)).apply(instance, ShaderProgramConfig.Sampler::new)
		);
	}

	@Environment(EnvType.CLIENT)
	public static record Uniform(String name, String type, int count, List<Float> values) {
		public static final Codec<ShaderProgramConfig.Uniform> CODEC = RecordCodecBuilder.<ShaderProgramConfig.Uniform>create(
				instance -> instance.group(
							Codec.STRING.fieldOf("name").forGetter(ShaderProgramConfig.Uniform::name),
							Codec.STRING.fieldOf("type").forGetter(ShaderProgramConfig.Uniform::type),
							Codec.INT.fieldOf("count").forGetter(ShaderProgramConfig.Uniform::count),
							Codec.FLOAT.listOf().fieldOf("values").forGetter(ShaderProgramConfig.Uniform::values)
						)
						.apply(instance, ShaderProgramConfig.Uniform::new)
			)
			.validate(ShaderProgramConfig.Uniform::validate);

		private static DataResult<ShaderProgramConfig.Uniform> validate(ShaderProgramConfig.Uniform uniform) {
			int i = uniform.count;
			int j = uniform.values.size();
			return j != i && j > 1
				? DataResult.error(() -> "Invalid amount of uniform values specified (expected " + i + ", found " + j + ")")
				: DataResult.success(uniform);
		}
	}
}
