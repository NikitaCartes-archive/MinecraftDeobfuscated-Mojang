package net.minecraft.client.renderer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
public record PostChainConfig(Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets, List<PostChainConfig.Pass> passes) {
	public static final Codec<PostChainConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.unboundedMap(ResourceLocation.CODEC, PostChainConfig.InternalTarget.CODEC)
						.optionalFieldOf("targets", Map.of())
						.forGetter(PostChainConfig::internalTargets),
					PostChainConfig.Pass.CODEC.listOf().optionalFieldOf("passes", List.of()).forGetter(PostChainConfig::passes)
				)
				.apply(instance, PostChainConfig::new)
	);

	@Environment(EnvType.CLIENT)
	public static record FixedSizedTarget(int width, int height) implements PostChainConfig.InternalTarget {
		public static final Codec<PostChainConfig.FixedSizedTarget> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(PostChainConfig.FixedSizedTarget::width),
						ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(PostChainConfig.FixedSizedTarget::height)
					)
					.apply(instance, PostChainConfig.FixedSizedTarget::new)
		);
	}

	@Environment(EnvType.CLIENT)
	public static record FullScreenTarget() implements PostChainConfig.InternalTarget {
		public static final Codec<PostChainConfig.FullScreenTarget> CODEC = Codec.unit(PostChainConfig.FullScreenTarget::new);
	}

	@Environment(EnvType.CLIENT)
	public sealed interface Input permits PostChainConfig.TextureInput, PostChainConfig.TargetInput {
		Codec<PostChainConfig.Input> CODEC = Codec.xor(PostChainConfig.TextureInput.CODEC, PostChainConfig.TargetInput.CODEC)
			.xmap(either -> either.map(Function.identity(), Function.identity()), input -> {
				Objects.requireNonNull(input);

				return switch (input) {
					case PostChainConfig.TextureInput textureInput -> Either.left(textureInput);
					case PostChainConfig.TargetInput targetInput -> Either.right(targetInput);
					default -> throw new MatchException(null, null);
				};
			});

		String samplerName();

		Set<ResourceLocation> referencedTargets();
	}

	@Environment(EnvType.CLIENT)
	public sealed interface InternalTarget permits PostChainConfig.FullScreenTarget, PostChainConfig.FixedSizedTarget {
		Codec<PostChainConfig.InternalTarget> CODEC = Codec.xor(PostChainConfig.FullScreenTarget.CODEC, PostChainConfig.FixedSizedTarget.CODEC)
			.xmap(either -> either.map(Function.identity(), Function.identity()), internalTarget -> {
				Objects.requireNonNull(internalTarget);

				return switch (internalTarget) {
					case PostChainConfig.FullScreenTarget fullScreenTarget -> Either.left(fullScreenTarget);
					case PostChainConfig.FixedSizedTarget fixedSizedTarget -> Either.right(fixedSizedTarget);
					default -> throw new MatchException(null, null);
				};
			});
	}

	@Environment(EnvType.CLIENT)
	public static record Pass(ResourceLocation program, List<PostChainConfig.Input> inputs, ResourceLocation outputTarget, List<PostChainConfig.Uniform> uniforms) {
		private static final Codec<List<PostChainConfig.Input>> INPUTS_CODEC = PostChainConfig.Input.CODEC.listOf().validate(list -> {
			Set<String> set = new ObjectArraySet<>(list.size());

			for (PostChainConfig.Input input : list) {
				if (!set.add(input.samplerName())) {
					return DataResult.error(() -> "Encountered repeated sampler name: " + input.samplerName());
				}
			}

			return DataResult.success(list);
		});
		public static final Codec<PostChainConfig.Pass> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceLocation.CODEC.fieldOf("program").forGetter(PostChainConfig.Pass::program),
						INPUTS_CODEC.optionalFieldOf("inputs", List.of()).forGetter(PostChainConfig.Pass::inputs),
						ResourceLocation.CODEC.fieldOf("output").forGetter(PostChainConfig.Pass::outputTarget),
						PostChainConfig.Uniform.CODEC.listOf().optionalFieldOf("uniforms", List.of()).forGetter(PostChainConfig.Pass::uniforms)
					)
					.apply(instance, PostChainConfig.Pass::new)
		);
	}

	@Environment(EnvType.CLIENT)
	public static record TargetInput(String samplerName, ResourceLocation targetId, boolean useDepthBuffer, boolean bilinear) implements PostChainConfig.Input {
		public static final Codec<PostChainConfig.TargetInput> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.STRING.fieldOf("sampler_name").forGetter(PostChainConfig.TargetInput::samplerName),
						ResourceLocation.CODEC.fieldOf("target").forGetter(PostChainConfig.TargetInput::targetId),
						Codec.BOOL.optionalFieldOf("use_depth_buffer", Boolean.valueOf(false)).forGetter(PostChainConfig.TargetInput::useDepthBuffer),
						Codec.BOOL.optionalFieldOf("bilinear", Boolean.valueOf(false)).forGetter(PostChainConfig.TargetInput::bilinear)
					)
					.apply(instance, PostChainConfig.TargetInput::new)
		);

		@Override
		public Set<ResourceLocation> referencedTargets() {
			return Set.of(this.targetId);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record TextureInput(String samplerName, ResourceLocation location, int width, int height, boolean bilinear) implements PostChainConfig.Input {
		public static final Codec<PostChainConfig.TextureInput> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.STRING.fieldOf("sampler_name").forGetter(PostChainConfig.TextureInput::samplerName),
						ResourceLocation.CODEC.fieldOf("location").forGetter(PostChainConfig.TextureInput::location),
						ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(PostChainConfig.TextureInput::width),
						ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(PostChainConfig.TextureInput::height),
						Codec.BOOL.optionalFieldOf("bilinear", Boolean.valueOf(false)).forGetter(PostChainConfig.TextureInput::bilinear)
					)
					.apply(instance, PostChainConfig.TextureInput::new)
		);

		@Override
		public Set<ResourceLocation> referencedTargets() {
			return Set.of();
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Uniform(String name, List<Float> values) {
		public static final Codec<PostChainConfig.Uniform> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.STRING.fieldOf("name").forGetter(PostChainConfig.Uniform::name),
						Codec.FLOAT.sizeLimitedListOf(4).fieldOf("values").forGetter(PostChainConfig.Uniform::values)
					)
					.apply(instance, PostChainConfig.Uniform::new)
		);
	}
}
