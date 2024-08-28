package net.minecraft.client.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class LevelEventHandler {
	private final Minecraft minecraft;
	private final Level level;
	private final LevelRenderer levelRenderer;
	private final Map<BlockPos, SoundInstance> playingJukeboxSongs = new HashMap();

	public LevelEventHandler(Minecraft minecraft, Level level, LevelRenderer levelRenderer) {
		this.minecraft = minecraft;
		this.level = level;
		this.levelRenderer = levelRenderer;
	}

	public void globalLevelEvent(int i, BlockPos blockPos, int j) {
		switch (i) {
			case 1023:
			case 1028:
			case 1038:
				Camera camera = this.minecraft.gameRenderer.getMainCamera();
				if (camera.isInitialized()) {
					Vec3 vec3 = Vec3.atCenterOf(blockPos).subtract(camera.getPosition()).normalize();
					Vec3 vec32 = camera.getPosition().add(vec3.scale(2.0));
					if (i == 1023) {
						this.level.playLocalSound(vec32.x, vec32.y, vec32.z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
					} else if (i == 1038) {
						this.level.playLocalSound(vec32.x, vec32.y, vec32.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
					} else {
						this.level.playLocalSound(vec32.x, vec32.y, vec32.z, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
					}
				}
		}
	}

	public void levelEvent(int i, BlockPos blockPos, int j) {
		RandomSource randomSource = this.level.random;
		switch (i) {
			case 1000:
				this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1001:
				this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
				break;
			case 1002:
				this.level.playLocalSound(blockPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
				break;
			case 1004:
				this.level.playLocalSound(blockPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
				break;
			case 1009:
				if (j == 0) {
					this.level
						.playLocalSound(
							blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false
						);
				} else if (j == 1) {
					this.level
						.playLocalSound(
							blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.4F, false
						);
				}
				break;
			case 1010:
				this.level.registryAccess().lookupOrThrow(Registries.JUKEBOX_SONG).get(j).ifPresent(reference -> this.playJukeboxSong(reference, blockPos));
				break;
			case 1011:
				this.stopJukeboxSongAndNotifyNearby(blockPos);
				break;
			case 1015:
				this.level
					.playLocalSound(blockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1016:
				this.level
					.playLocalSound(blockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1017:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1018:
				this.level
					.playLocalSound(blockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1019:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1020:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1021:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1022:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1024:
				this.level
					.playLocalSound(blockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1025:
				this.level
					.playLocalSound(blockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1026:
				this.level
					.playLocalSound(blockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false);
				break;
			case 1027:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1029:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1030:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1031:
				this.level.playLocalSound(blockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1032:
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, randomSource.nextFloat() * 0.4F + 0.8F, 0.25F));
				break;
			case 1033:
				this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1034:
				this.level.playLocalSound(blockPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1035:
				this.level.playLocalSound(blockPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1039:
				this.level.playLocalSound(blockPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1040:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1041:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1042:
				this.level.playLocalSound(blockPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1043:
				this.level.playLocalSound(blockPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1044:
				this.level.playLocalSound(blockPos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1045:
				this.level.playLocalSound(blockPos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 1046:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false
					);
				break;
			case 1047:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false
					);
				break;
			case 1048:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, false
					);
				break;
			case 1049:
				this.level.playLocalSound(blockPos, SoundEvents.CRAFTER_CRAFT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1050:
				this.level.playLocalSound(blockPos, SoundEvents.CRAFTER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 1051:
				this.level
					.playLocalSound(blockPos, SoundEvents.WIND_CHARGE_THROW, SoundSource.BLOCKS, 0.5F, 0.4F / (this.level.getRandom().nextFloat() * 0.4F + 0.8F), false);
				break;
			case 1500:
				ComposterBlock.handleFill(this.level, blockPos, j > 0);
				break;
			case 1501:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false
					);

				for (int ox = 0; ox < 8; ox++) {
					this.level
						.addParticle(
							ParticleTypes.LARGE_SMOKE,
							(double)blockPos.getX() + randomSource.nextDouble(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + randomSource.nextDouble(),
							0.0,
							0.0,
							0.0
						);
				}
				break;
			case 1502:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (randomSource.nextFloat() - randomSource.nextFloat()) * 0.8F, false
					);

				for (int ox = 0; ox < 5; ox++) {
					double g = (double)blockPos.getX() + randomSource.nextDouble() * 0.6 + 0.2;
					double p = (double)blockPos.getY() + randomSource.nextDouble() * 0.6 + 0.2;
					double q = (double)blockPos.getZ() + randomSource.nextDouble() * 0.6 + 0.2;
					this.level.addParticle(ParticleTypes.SMOKE, g, p, q, 0.0, 0.0, 0.0);
				}
				break;
			case 1503:
				this.level.playLocalSound(blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

				for (int ox = 0; ox < 16; ox++) {
					double g = (double)blockPos.getX() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
					double p = (double)blockPos.getY() + 0.8125;
					double q = (double)blockPos.getZ() + (5.0 + randomSource.nextDouble() * 6.0) / 16.0;
					this.level.addParticle(ParticleTypes.SMOKE, g, p, q, 0.0, 0.0, 0.0);
				}
				break;
			case 1504:
				PointedDripstoneBlock.spawnDripParticle(this.level, blockPos, this.level.getBlockState(blockPos));
				break;
			case 1505:
				BoneMealItem.addGrowthParticles(this.level, blockPos, j);
				this.level.playLocalSound(blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 2000:
				this.shootParticles(j, blockPos, randomSource, ParticleTypes.SMOKE);
				break;
			case 2001:
				BlockState blockState = Block.stateById(j);
				if (!blockState.isAir()) {
					SoundType soundType = blockState.getSoundType();
					this.level
						.playLocalSound(blockPos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
				}

				this.level.addDestroyBlockEffect(blockPos, blockState);
				break;
			case 2002:
			case 2007:
				Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);

				for (int l = 0; l < 8; l++) {
					this.levelRenderer
						.addParticle(
							new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)),
							vec3.x,
							vec3.y,
							vec3.z,
							randomSource.nextGaussian() * 0.15,
							randomSource.nextDouble() * 0.2,
							randomSource.nextGaussian() * 0.15
						);
				}

				float h = (float)(j >> 16 & 0xFF) / 255.0F;
				float m = (float)(j >> 8 & 0xFF) / 255.0F;
				float n = (float)(j >> 0 & 0xFF) / 255.0F;
				ParticleOptions particleOptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

				for (int ox = 0; ox < 100; ox++) {
					double g = randomSource.nextDouble() * 4.0;
					double p = randomSource.nextDouble() * Math.PI * 2.0;
					double q = Math.cos(p) * g;
					double r = 0.01 + randomSource.nextDouble() * 0.5;
					double s = Math.sin(p) * g;
					Particle particle = this.levelRenderer
						.addParticleInternal(particleOptions, particleOptions.getType().getOverrideLimiter(), vec3.x + q * 0.1, vec3.y + 0.3, vec3.z + s * 0.1, q, r, s);
					if (particle != null) {
						float t = 0.75F + randomSource.nextFloat() * 0.25F;
						particle.setColor(h * t, m * t, n * t);
						particle.setPower((float)g);
					}
				}

				this.level.playLocalSound(blockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				break;
			case 2003:
				double d = (double)blockPos.getX() + 0.5;
				double e = (double)blockPos.getY();
				double f = (double)blockPos.getZ() + 0.5;

				for (int k = 0; k < 8; k++) {
					this.levelRenderer
						.addParticle(
							new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)),
							d,
							e,
							f,
							randomSource.nextGaussian() * 0.15,
							randomSource.nextDouble() * 0.2,
							randomSource.nextGaussian() * 0.15
						);
				}

				for (double g = 0.0; g < Math.PI * 2; g += Math.PI / 20) {
					this.levelRenderer.addParticle(ParticleTypes.PORTAL, d + Math.cos(g) * 5.0, e - 0.4, f + Math.sin(g) * 5.0, Math.cos(g) * -5.0, 0.0, Math.sin(g) * -5.0);
					this.levelRenderer.addParticle(ParticleTypes.PORTAL, d + Math.cos(g) * 5.0, e - 0.4, f + Math.sin(g) * 5.0, Math.cos(g) * -7.0, 0.0, Math.sin(g) * -7.0);
				}
				break;
			case 2004:
				for (int ux = 0; ux < 20; ux++) {
					double v = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
					double w = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
					double x = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
					this.level.addParticle(ParticleTypes.SMOKE, v, w, x, 0.0, 0.0, 0.0);
					this.level.addParticle(ParticleTypes.FLAME, v, w, x, 0.0, 0.0, 0.0);
				}
				break;
			case 2006:
				for (int o = 0; o < 200; o++) {
					float ad = randomSource.nextFloat() * 4.0F;
					float ai = randomSource.nextFloat() * (float) (Math.PI * 2);
					double p = (double)(Mth.cos(ai) * ad);
					double q = 0.01 + randomSource.nextDouble() * 0.5;
					double r = (double)(Mth.sin(ai) * ad);
					Particle particle2 = this.levelRenderer
						.addParticleInternal(
							ParticleTypes.DRAGON_BREATH, false, (double)blockPos.getX() + p * 0.1, (double)blockPos.getY() + 0.3, (double)blockPos.getZ() + r * 0.1, p, q, r
						);
					if (particle2 != null) {
						particle2.setPower(ad);
					}
				}

				if (j == 1) {
					this.level.playLocalSound(blockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, randomSource.nextFloat() * 0.1F + 0.9F, false);
				}
				break;
			case 2008:
				this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0);
				break;
			case 2009:
				for (int ox = 0; ox < 8; ox++) {
					this.level
						.addParticle(
							ParticleTypes.CLOUD,
							(double)blockPos.getX() + randomSource.nextDouble(),
							(double)blockPos.getY() + 1.2,
							(double)blockPos.getZ() + randomSource.nextDouble(),
							0.0,
							0.0,
							0.0
						);
				}
				break;
			case 2010:
				this.shootParticles(j, blockPos, randomSource, ParticleTypes.WHITE_SMOKE);
				break;
			case 2011:
				ParticleUtils.spawnParticleInBlock(this.level, blockPos, j, ParticleTypes.HAPPY_VILLAGER);
				break;
			case 2012:
				ParticleUtils.spawnParticleInBlock(this.level, blockPos, j, ParticleTypes.HAPPY_VILLAGER);
				break;
			case 2013:
				ParticleUtils.spawnSmashAttackParticles(this.level, blockPos, j);
				break;
			case 3000:
				this.level
					.addParticle(
						ParticleTypes.EXPLOSION_EMITTER, true, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 0.0, 0.0, 0.0
					);
				this.level
					.playLocalSound(
						blockPos,
						SoundEvents.END_GATEWAY_SPAWN,
						SoundSource.BLOCKS,
						10.0F,
						(1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
						false
					);
				break;
			case 3001:
				this.level.playLocalSound(blockPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
				break;
			case 3002:
				if (j >= 0 && j < Direction.Axis.VALUES.length) {
					ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[j], this.level, blockPos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
				} else {
					ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
				}
				break;
			case 3003:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
				this.level.playLocalSound(blockPos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
				break;
			case 3004:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
				break;
			case 3005:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
				break;
			case 3006:
				int u = j >> 6;
				if (u > 0) {
					if (randomSource.nextFloat() < 0.3F + (float)u * 0.1F) {
						float n = 0.15F + 0.02F * (float)u * (float)u * randomSource.nextFloat();
						float y = 0.4F + 0.3F * (float)u * randomSource.nextFloat();
						this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, n, y, false);
					}

					byte b = (byte)(j & 63);
					IntProvider intProvider = UniformInt.of(0, u);
					float z = 0.005F;
					Supplier<Vec3> supplier = () -> new Vec3(
							Mth.nextDouble(randomSource, -0.005F, 0.005F), Mth.nextDouble(randomSource, -0.005F, 0.005F), Mth.nextDouble(randomSource, -0.005F, 0.005F)
						);
					if (b == 0) {
						for (Direction direction : Direction.values()) {
							float aa = direction == Direction.DOWN ? (float) Math.PI : 0.0F;
							double r = direction.getAxis() == Direction.Axis.Y ? 0.65 : 0.57;
							ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(aa), intProvider, direction, supplier, r);
						}
					} else {
						for (Direction direction2 : MultifaceBlock.unpack(b)) {
							float ab = direction2 == Direction.UP ? (float) Math.PI : 0.0F;
							double q = 0.35;
							ParticleUtils.spawnParticlesOnBlockFace(this.level, blockPos, new SculkChargeParticleOptions(ab), intProvider, direction2, supplier, 0.35);
						}
					}
				} else {
					this.level.playLocalSound(blockPos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
					boolean bl = this.level.getBlockState(blockPos).isCollisionShapeFullBlock(this.level, blockPos);
					int ac = bl ? 40 : 20;
					float z = bl ? 0.45F : 0.25F;
					float ad = 0.07F;

					for (int ae = 0; ae < ac; ae++) {
						float af = 2.0F * randomSource.nextFloat() - 1.0F;
						float ab = 2.0F * randomSource.nextFloat() - 1.0F;
						float ag = 2.0F * randomSource.nextFloat() - 1.0F;
						this.level
							.addParticle(
								ParticleTypes.SCULK_CHARGE_POP,
								(double)blockPos.getX() + 0.5 + (double)(af * z),
								(double)blockPos.getY() + 0.5 + (double)(ab * z),
								(double)blockPos.getZ() + 0.5 + (double)(ag * z),
								(double)(af * 0.07F),
								(double)(ab * 0.07F),
								(double)(ag * 0.07F)
							);
					}
				}
				break;
			case 3007:
				for (int ah = 0; ah < 10; ah++) {
					this.level
						.addParticle(
							new ShriekParticleOption(ah * 5),
							false,
							(double)blockPos.getX() + 0.5,
							(double)blockPos.getY() + SculkShriekerBlock.TOP_Y,
							(double)blockPos.getZ() + 0.5,
							0.0,
							0.0,
							0.0
						);
				}

				BlockState blockState3 = this.level.getBlockState(blockPos);
				boolean bl2 = blockState3.hasProperty(BlockStateProperties.WATERLOGGED) && (Boolean)blockState3.getValue(BlockStateProperties.WATERLOGGED);
				if (!bl2) {
					this.level
						.playLocalSound(
							(double)blockPos.getX() + 0.5,
							(double)blockPos.getY() + SculkShriekerBlock.TOP_Y,
							(double)blockPos.getZ() + 0.5,
							SoundEvents.SCULK_SHRIEKER_SHRIEK,
							SoundSource.BLOCKS,
							2.0F,
							0.6F + this.level.random.nextFloat() * 0.4F,
							false
						);
				}
				break;
			case 3008:
				BlockState blockState2 = Block.stateById(j);
				if (blockState2.getBlock() instanceof BrushableBlock brushableBlock) {
					this.level.playLocalSound(blockPos, brushableBlock.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
				}

				this.level.addDestroyBlockEffect(blockPos, blockState2);
				break;
			case 3009:
				ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockPos, ParticleTypes.EGG_CRACK, UniformInt.of(3, 6));
				break;
			case 3011:
				TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource, TrialSpawner.FlameParticle.decode(j).particleType);
				break;
			case 3012:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource, TrialSpawner.FlameParticle.decode(j).particleType);
				break;
			case 3013:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, j, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER);
				break;
			case 3014:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addEjectItemParticles(this.level, blockPos, randomSource);
				break;
			case 3015:
				if (this.level.getBlockEntity(blockPos) instanceof VaultBlockEntity vaultBlockEntity) {
					VaultBlockEntity.Client.emitActivationParticles(
						this.level,
						vaultBlockEntity.getBlockPos(),
						vaultBlockEntity.getBlockState(),
						vaultBlockEntity.getSharedData(),
						j == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME
					);
					this.level
						.playLocalSound(blockPos, SoundEvents.VAULT_ACTIVATE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true);
				}
				break;
			case 3016:
				VaultBlockEntity.Client.emitDeactivationParticles(this.level, blockPos, j == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SOUL_FIRE_FLAME);
				this.level
					.playLocalSound(
						blockPos, SoundEvents.VAULT_DEACTIVATE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				break;
			case 3017:
				TrialSpawner.addEjectItemParticles(this.level, blockPos, randomSource);
				break;
			case 3018:
				for (int ux = 0; ux < 10; ux++) {
					double v = randomSource.nextGaussian() * 0.02;
					double w = randomSource.nextGaussian() * 0.02;
					double x = randomSource.nextGaussian() * 0.02;
					this.level
						.addParticle(
							ParticleTypes.POOF,
							(double)blockPos.getX() + randomSource.nextDouble(),
							(double)blockPos.getY() + randomSource.nextDouble(),
							(double)blockPos.getZ() + randomSource.nextDouble(),
							v,
							w,
							x
						);
				}

				this.level
					.playLocalSound(blockPos, SoundEvents.COBWEB_PLACE, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true);
				break;
			case 3019:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, j, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS);
				break;
			case 3020:
				this.level
					.playLocalSound(
						blockPos,
						SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE,
						SoundSource.BLOCKS,
						j == 0 ? 0.3F : 1.0F,
						(randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F,
						true
					);
				TrialSpawner.addDetectPlayerParticles(this.level, blockPos, randomSource, 0, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS);
				TrialSpawner.addBecomeOminousParticles(this.level, blockPos, randomSource);
				break;
			case 3021:
				this.level
					.playLocalSound(
						blockPos, SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM, SoundSource.BLOCKS, 1.0F, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2F + 1.0F, true
					);
				TrialSpawner.addSpawnParticles(this.level, blockPos, randomSource, TrialSpawner.FlameParticle.decode(j).particleType);
		}
	}

	private void shootParticles(int i, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
		Direction direction = Direction.from3DDataValue(i);
		int j = direction.getStepX();
		int k = direction.getStepY();
		int l = direction.getStepZ();

		for (int m = 0; m < 10; m++) {
			double d = randomSource.nextDouble() * 0.2 + 0.01;
			double e = (double)blockPos.getX() + (double)j * 0.6 + 0.5 + (double)j * 0.01 + (randomSource.nextDouble() - 0.5) * (double)l * 0.5;
			double f = (double)blockPos.getY() + (double)k * 0.6 + 0.5 + (double)k * 0.01 + (randomSource.nextDouble() - 0.5) * (double)k * 0.5;
			double g = (double)blockPos.getZ() + (double)l * 0.6 + 0.5 + (double)l * 0.01 + (randomSource.nextDouble() - 0.5) * (double)j * 0.5;
			double h = (double)j * d + randomSource.nextGaussian() * 0.01;
			double n = (double)k * d + randomSource.nextGaussian() * 0.01;
			double o = (double)l * d + randomSource.nextGaussian() * 0.01;
			this.levelRenderer.addParticle(simpleParticleType, e, f, g, h, n, o);
		}
	}

	private void playJukeboxSong(Holder<JukeboxSong> holder, BlockPos blockPos) {
		this.stopJukeboxSong(blockPos);
		JukeboxSong jukeboxSong = holder.value();
		SoundEvent soundEvent = jukeboxSong.soundEvent().value();
		SoundInstance soundInstance = SimpleSoundInstance.forJukeboxSong(soundEvent, Vec3.atCenterOf(blockPos));
		this.playingJukeboxSongs.put(blockPos, soundInstance);
		this.minecraft.getSoundManager().play(soundInstance);
		this.minecraft.gui.setNowPlaying(jukeboxSong.description());
		this.notifyNearbyEntities(this.level, blockPos, true);
	}

	private void stopJukeboxSong(BlockPos blockPos) {
		SoundInstance soundInstance = (SoundInstance)this.playingJukeboxSongs.remove(blockPos);
		if (soundInstance != null) {
			this.minecraft.getSoundManager().stop(soundInstance);
		}
	}

	private void stopJukeboxSongAndNotifyNearby(BlockPos blockPos) {
		this.stopJukeboxSong(blockPos);
		this.notifyNearbyEntities(this.level, blockPos, false);
	}

	private void notifyNearbyEntities(Level level, BlockPos blockPos, boolean bl) {
		for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(3.0))) {
			livingEntity.setRecordPlayingNearby(blockPos, bl);
		}
	}
}
