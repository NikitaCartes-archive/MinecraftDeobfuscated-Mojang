package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class WeatherEffectRenderer {
	private static final int RAIN_RADIUS = 10;
	private static final int RAIN_DIAMETER = 21;
	private static final ResourceLocation RAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/rain.png");
	private static final ResourceLocation SNOW_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/snow.png");
	private static final int RAIN_TABLE_SIZE = 32;
	private static final int HALF_RAIN_TABLE_SIZE = 16;
	private int rainSoundTime;
	private final float[] columnSizeX = new float[1024];
	private final float[] columnSizeZ = new float[1024];

	public WeatherEffectRenderer() {
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				float f = (float)(j - 16);
				float g = (float)(i - 16);
				float h = Mth.length(f, g);
				this.columnSizeX[i * 32 + j] = -g / h;
				this.columnSizeZ[i * 32 + j] = f / h;
			}
		}
	}

	public void render(Level level, LightTexture lightTexture, int i, float f, Vec3 vec3) {
		float g = level.getRainLevel(f);
		if (!(g <= 0.0F)) {
			int j = Minecraft.useFancyGraphics() ? 10 : 5;
			List<WeatherEffectRenderer.ColumnInstance> list = new ArrayList();
			List<WeatherEffectRenderer.ColumnInstance> list2 = new ArrayList();
			this.collectColumnInstances(level, i, f, vec3, j, list, list2);
			if (!list.isEmpty() || !list2.isEmpty()) {
				this.render(lightTexture, vec3, j, g, list, list2);
			}
		}
	}

	private void collectColumnInstances(
		Level level, int i, float f, Vec3 vec3, int j, List<WeatherEffectRenderer.ColumnInstance> list, List<WeatherEffectRenderer.ColumnInstance> list2
	) {
		int k = Mth.floor(vec3.x);
		int l = Mth.floor(vec3.y);
		int m = Mth.floor(vec3.z);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		RandomSource randomSource = RandomSource.create();

		for (int n = m - j; n <= m + j; n++) {
			for (int o = k - j; o <= k + j; o++) {
				int p = level.getHeight(Heightmap.Types.MOTION_BLOCKING, o, n);
				int q = Math.max(l - j, p);
				int r = Math.max(l + j, p);
				if (r - q != 0) {
					Biome.Precipitation precipitation = this.getPrecipitationAt(level, mutableBlockPos.set(o, l, n));
					if (precipitation != Biome.Precipitation.NONE) {
						int s = o * o * 3121 + o * 45238971 ^ n * n * 418711 + n * 13761;
						randomSource.setSeed((long)s);
						int t = Math.max(l, p);
						int u = LevelRenderer.getLightColor(level, mutableBlockPos.set(o, t, n));
						if (precipitation == Biome.Precipitation.RAIN) {
							list.add(this.createRainColumnInstance(randomSource, i, o, q, r, n, u, f));
						} else if (precipitation == Biome.Precipitation.SNOW) {
							list2.add(this.createSnowColumnInstance(randomSource, i, o, q, r, n, u, f));
						}
					}
				}
			}
		}
	}

	private void render(
		LightTexture lightTexture, Vec3 vec3, int i, float f, List<WeatherEffectRenderer.ColumnInstance> list, List<WeatherEffectRenderer.ColumnInstance> list2
	) {
		lightTexture.turnOnLightLayer();
		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(Minecraft.useShaderTransparency());
		RenderSystem.setShader(CoreShaders.PARTICLE);
		if (!list.isEmpty()) {
			RenderSystem.setShaderTexture(0, RAIN_LOCATION);
			this.renderInstances(tesselator, list, vec3, 1.0F, i, f);
		}

		if (!list2.isEmpty()) {
			RenderSystem.setShaderTexture(0, SNOW_LOCATION);
			this.renderInstances(tesselator, list2, vec3, 0.8F, i, f);
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		lightTexture.turnOffLightLayer();
	}

	private WeatherEffectRenderer.ColumnInstance createRainColumnInstance(RandomSource randomSource, int i, int j, int k, int l, int m, int n, float f) {
		int o = i & 131071;
		int p = j * j * 3121 + j * 45238971 + m * m * 418711 + m * 13761 & 0xFF;
		float g = 3.0F + randomSource.nextFloat();
		float h = -((float)(o + p) + f) / 32.0F * g;
		float q = h % 32.0F;
		return new WeatherEffectRenderer.ColumnInstance(j, m, k, l, 0.0F, q, n);
	}

	private WeatherEffectRenderer.ColumnInstance createSnowColumnInstance(RandomSource randomSource, int i, int j, int k, int l, int m, int n, float f) {
		float g = (float)i + f;
		float h = (float)(randomSource.nextDouble() + (double)(g * 0.01F * (float)randomSource.nextGaussian()));
		float o = (float)(randomSource.nextDouble() + (double)(g * (float)randomSource.nextGaussian() * 0.001F));
		float p = -((float)(i & 511) + f) / 512.0F;
		int q = LightTexture.pack((LightTexture.block(n) * 3 + 15) / 4, (LightTexture.sky(n) * 3 + 15) / 4);
		return new WeatherEffectRenderer.ColumnInstance(j, m, k, l, h, p + o, q);
	}

	private void renderInstances(Tesselator tesselator, List<WeatherEffectRenderer.ColumnInstance> list, Vec3 vec3, float f, int i, float g) {
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

		for (WeatherEffectRenderer.ColumnInstance columnInstance : list) {
			float h = (float)((double)columnInstance.x + 0.5 - vec3.x);
			float j = (float)((double)columnInstance.z + 0.5 - vec3.z);
			float k = (float)Mth.lengthSquared((double)h, (double)j);
			float l = Mth.lerp(k / (float)(i * i), f, 0.5F) * g;
			int m = ARGB.white(l);
			int n = (columnInstance.z - Mth.floor(vec3.z) + 16) * 32 + columnInstance.x - Mth.floor(vec3.x) + 16;
			float o = this.columnSizeX[n] / 2.0F;
			float p = this.columnSizeZ[n] / 2.0F;
			float q = h - o;
			float r = h + o;
			float s = (float)((double)columnInstance.topY - vec3.y);
			float t = (float)((double)columnInstance.bottomY - vec3.y);
			float u = j - p;
			float v = j + p;
			float w = columnInstance.uOffset + 0.0F;
			float x = columnInstance.uOffset + 1.0F;
			float y = (float)columnInstance.bottomY * 0.25F + columnInstance.vOffset;
			float z = (float)columnInstance.topY * 0.25F + columnInstance.vOffset;
			bufferBuilder.addVertex(q, s, u).setUv(w, y).setColor(m).setLight(columnInstance.lightCoords);
			bufferBuilder.addVertex(r, s, v).setUv(x, y).setColor(m).setLight(columnInstance.lightCoords);
			bufferBuilder.addVertex(r, t, v).setUv(x, z).setColor(m).setLight(columnInstance.lightCoords);
			bufferBuilder.addVertex(q, t, u).setUv(w, z).setColor(m).setLight(columnInstance.lightCoords);
		}

		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
	}

	public void tickRainParticles(ClientLevel clientLevel, Camera camera, int i, ParticleStatus particleStatus) {
		float f = clientLevel.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
		if (!(f <= 0.0F)) {
			RandomSource randomSource = RandomSource.create((long)i * 312987231L);
			BlockPos blockPos = BlockPos.containing(camera.getPosition());
			BlockPos blockPos2 = null;
			int j = (int)(100.0F * f * f) / (particleStatus == ParticleStatus.DECREASED ? 2 : 1);

			for (int k = 0; k < j; k++) {
				int l = randomSource.nextInt(21) - 10;
				int m = randomSource.nextInt(21) - 10;
				BlockPos blockPos3 = clientLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(l, 0, m));
				if (blockPos3.getY() > clientLevel.getMinY()
					&& blockPos3.getY() <= blockPos.getY() + 10
					&& blockPos3.getY() >= blockPos.getY() - 10
					&& this.getPrecipitationAt(clientLevel, blockPos3) == Biome.Precipitation.RAIN) {
					blockPos2 = blockPos3.below();
					if (particleStatus == ParticleStatus.MINIMAL) {
						break;
					}

					double d = randomSource.nextDouble();
					double e = randomSource.nextDouble();
					BlockState blockState = clientLevel.getBlockState(blockPos2);
					FluidState fluidState = clientLevel.getFluidState(blockPos2);
					VoxelShape voxelShape = blockState.getCollisionShape(clientLevel, blockPos2);
					double g = voxelShape.max(Direction.Axis.Y, d, e);
					double h = (double)fluidState.getHeight(clientLevel, blockPos2);
					double n = Math.max(g, h);
					ParticleOptions particleOptions = !fluidState.is(FluidTags.LAVA) && !blockState.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockState)
						? ParticleTypes.RAIN
						: ParticleTypes.SMOKE;
					clientLevel.addParticle(particleOptions, (double)blockPos2.getX() + d, (double)blockPos2.getY() + n, (double)blockPos2.getZ() + e, 0.0, 0.0, 0.0);
				}
			}

			if (blockPos2 != null && randomSource.nextInt(3) < this.rainSoundTime++) {
				this.rainSoundTime = 0;
				if (blockPos2.getY() > blockPos.getY() + 1
					&& clientLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor((float)blockPos.getY())) {
					clientLevel.playLocalSound(blockPos2, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
				} else {
					clientLevel.playLocalSound(blockPos2, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
				}
			}
		}
	}

	private Biome.Precipitation getPrecipitationAt(Level level, BlockPos blockPos) {
		if (!level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
			return Biome.Precipitation.NONE;
		} else {
			Biome biome = level.getBiome(blockPos).value();
			return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
		}
	}

	@Environment(EnvType.CLIENT)
	static record ColumnInstance(int x, int z, int bottomY, int topY, float uOffset, float vOffset, int lightCoords) {
	}
}
