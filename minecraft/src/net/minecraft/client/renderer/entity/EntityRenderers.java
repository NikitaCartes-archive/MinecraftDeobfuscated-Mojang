package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class EntityRenderers {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<EntityType<?>, EntityRendererProvider<?>> PROVIDERS = new Object2ObjectOpenHashMap<>();
	private static final Map<PlayerSkin.Model, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS = Map.of(
		PlayerSkin.Model.WIDE,
		(EntityRendererProvider<>)context -> new PlayerRenderer(context, false),
		PlayerSkin.Model.SLIM,
		(EntityRendererProvider<>)context -> new PlayerRenderer(context, true)
	);

	private static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
		PROVIDERS.put(entityType, entityRendererProvider);
	}

	public static Map<EntityType<?>, EntityRenderer<?, ?>> createEntityRenderers(EntityRendererProvider.Context context) {
		Builder<EntityType<?>, EntityRenderer<?, ?>> builder = ImmutableMap.builder();
		PROVIDERS.forEach((entityType, entityRendererProvider) -> {
			try {
				builder.put(entityType, entityRendererProvider.create(context));
			} catch (Exception var5) {
				throw new IllegalArgumentException("Failed to create model for " + BuiltInRegistries.ENTITY_TYPE.getKey(entityType), var5);
			}
		});
		return builder.build();
	}

	public static Map<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> createPlayerRenderers(EntityRendererProvider.Context context) {
		Builder<PlayerSkin.Model, EntityRenderer<? extends Player, ?>> builder = ImmutableMap.builder();
		PLAYER_PROVIDERS.forEach((model, entityRendererProvider) -> {
			try {
				builder.put(model, entityRendererProvider.create(context));
			} catch (Exception var5) {
				throw new IllegalArgumentException("Failed to create player model for " + model, var5);
			}
		});
		return builder.build();
	}

	public static boolean validateRegistrations() {
		boolean bl = true;

		for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
			if (entityType != EntityType.PLAYER && !PROVIDERS.containsKey(entityType)) {
				LOGGER.warn("No renderer registered for {}", BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
				bl = false;
			}
		}

		return !bl;
	}

	static {
		register(EntityType.ALLAY, AllayRenderer::new);
		register(EntityType.AREA_EFFECT_CLOUD, NoopRenderer::new);
		register(EntityType.ARMADILLO, ArmadilloRenderer::new);
		register(EntityType.ARMOR_STAND, ArmorStandRenderer::new);
		register(EntityType.ARROW, TippableArrowRenderer::new);
		register(EntityType.AXOLOTL, AxolotlRenderer::new);
		register(EntityType.BAT, BatRenderer::new);
		register(EntityType.BEE, BeeRenderer::new);
		register(EntityType.BLAZE, BlazeRenderer::new);
		register(EntityType.BLOCK_DISPLAY, DisplayRenderer.BlockDisplayRenderer::new);
		register(EntityType.OAK_BOAT, context -> new BoatRenderer(context, ModelLayers.OAK_BOAT));
		register(EntityType.SPRUCE_BOAT, context -> new BoatRenderer(context, ModelLayers.SPRUCE_BOAT));
		register(EntityType.BIRCH_BOAT, context -> new BoatRenderer(context, ModelLayers.BIRCH_BOAT));
		register(EntityType.JUNGLE_BOAT, context -> new BoatRenderer(context, ModelLayers.JUNGLE_BOAT));
		register(EntityType.ACACIA_BOAT, context -> new BoatRenderer(context, ModelLayers.ACACIA_BOAT));
		register(EntityType.CHERRY_BOAT, context -> new BoatRenderer(context, ModelLayers.CHERRY_BOAT));
		register(EntityType.DARK_OAK_BOAT, context -> new BoatRenderer(context, ModelLayers.DARK_OAK_BOAT));
		register(EntityType.PALE_OAK_BOAT, context -> new BoatRenderer(context, ModelLayers.PALE_OAK_BOAT));
		register(EntityType.MANGROVE_BOAT, context -> new BoatRenderer(context, ModelLayers.MANGROVE_BOAT));
		register(EntityType.BAMBOO_RAFT, context -> new RaftRenderer(context, ModelLayers.BAMBOO_RAFT));
		register(EntityType.BOGGED, BoggedRenderer::new);
		register(EntityType.BREEZE, BreezeRenderer::new);
		register(EntityType.BREEZE_WIND_CHARGE, WindChargeRenderer::new);
		register(EntityType.CAT, CatRenderer::new);
		register(EntityType.CAMEL, CamelRenderer::new);
		register(EntityType.CAVE_SPIDER, CaveSpiderRenderer::new);
		register(EntityType.OAK_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.OAK_CHEST_BOAT));
		register(EntityType.SPRUCE_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.SPRUCE_CHEST_BOAT));
		register(EntityType.BIRCH_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.BIRCH_CHEST_BOAT));
		register(EntityType.JUNGLE_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.JUNGLE_CHEST_BOAT));
		register(EntityType.ACACIA_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.ACACIA_CHEST_BOAT));
		register(EntityType.CHERRY_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.CHERRY_CHEST_BOAT));
		register(EntityType.DARK_OAK_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.DARK_OAK_CHEST_BOAT));
		register(EntityType.PALE_OAK_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.PALE_OAK_CHEST_BOAT));
		register(EntityType.MANGROVE_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.MANGROVE_CHEST_BOAT));
		register(EntityType.BAMBOO_CHEST_RAFT, context -> new RaftRenderer(context, ModelLayers.BAMBOO_CHEST_RAFT));
		register(EntityType.CHEST_MINECART, context -> new MinecartRenderer(context, ModelLayers.CHEST_MINECART));
		register(EntityType.CHICKEN, ChickenRenderer::new);
		register(EntityType.COD, CodRenderer::new);
		register(EntityType.COMMAND_BLOCK_MINECART, context -> new MinecartRenderer(context, ModelLayers.COMMAND_BLOCK_MINECART));
		register(EntityType.COW, CowRenderer::new);
		register(EntityType.CREAKING, CreakingRenderer::new);
		register(EntityType.CREAKING_TRANSIENT, CreakingRenderer::new);
		register(EntityType.CREEPER, CreeperRenderer::new);
		register(EntityType.DOLPHIN, DolphinRenderer::new);
		register(EntityType.DONKEY, context -> new DonkeyRenderer<>(context, 0.87F, ModelLayers.DONKEY, ModelLayers.DONKEY_BABY, false));
		register(EntityType.DRAGON_FIREBALL, DragonFireballRenderer::new);
		register(EntityType.DROWNED, DrownedRenderer::new);
		register(EntityType.EGG, ThrownItemRenderer::new);
		register(EntityType.ELDER_GUARDIAN, ElderGuardianRenderer::new);
		register(EntityType.ENDERMAN, EndermanRenderer::new);
		register(EntityType.ENDERMITE, EndermiteRenderer::new);
		register(EntityType.ENDER_DRAGON, EnderDragonRenderer::new);
		register(EntityType.ENDER_PEARL, ThrownItemRenderer::new);
		register(EntityType.END_CRYSTAL, EndCrystalRenderer::new);
		register(EntityType.EVOKER, EvokerRenderer::new);
		register(EntityType.EVOKER_FANGS, EvokerFangsRenderer::new);
		register(EntityType.EXPERIENCE_BOTTLE, ThrownItemRenderer::new);
		register(EntityType.EXPERIENCE_ORB, ExperienceOrbRenderer::new);
		register(EntityType.EYE_OF_ENDER, context -> new ThrownItemRenderer<>(context, 1.0F, true));
		register(EntityType.FALLING_BLOCK, FallingBlockRenderer::new);
		register(EntityType.FIREBALL, context -> new ThrownItemRenderer<>(context, 3.0F, true));
		register(EntityType.FIREWORK_ROCKET, FireworkEntityRenderer::new);
		register(EntityType.FISHING_BOBBER, FishingHookRenderer::new);
		register(EntityType.FOX, FoxRenderer::new);
		register(EntityType.FROG, FrogRenderer::new);
		register(EntityType.FURNACE_MINECART, context -> new MinecartRenderer(context, ModelLayers.FURNACE_MINECART));
		register(EntityType.GHAST, GhastRenderer::new);
		register(EntityType.GIANT, context -> new GiantMobRenderer(context, 6.0F));
		register(EntityType.GLOW_ITEM_FRAME, ItemFrameRenderer::new);
		register(
			EntityType.GLOW_SQUID,
			context -> new GlowSquidRenderer(
					context, new SquidModel(context.bakeLayer(ModelLayers.GLOW_SQUID)), new SquidModel(context.bakeLayer(ModelLayers.GLOW_SQUID_BABY))
				)
		);
		register(EntityType.GOAT, GoatRenderer::new);
		register(EntityType.GUARDIAN, GuardianRenderer::new);
		register(EntityType.HOGLIN, HoglinRenderer::new);
		register(EntityType.HOPPER_MINECART, context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
		register(EntityType.HORSE, HorseRenderer::new);
		register(EntityType.HUSK, HuskRenderer::new);
		register(EntityType.ILLUSIONER, IllusionerRenderer::new);
		register(EntityType.INTERACTION, NoopRenderer::new);
		register(EntityType.IRON_GOLEM, IronGolemRenderer::new);
		register(EntityType.ITEM, ItemEntityRenderer::new);
		register(EntityType.ITEM_DISPLAY, DisplayRenderer.ItemDisplayRenderer::new);
		register(EntityType.ITEM_FRAME, ItemFrameRenderer::new);
		register(EntityType.OMINOUS_ITEM_SPAWNER, OminousItemSpawnerRenderer::new);
		register(EntityType.LEASH_KNOT, LeashKnotRenderer::new);
		register(EntityType.LIGHTNING_BOLT, LightningBoltRenderer::new);
		register(EntityType.LLAMA, context -> new LlamaRenderer(context, ModelLayers.LLAMA, ModelLayers.LLAMA_BABY));
		register(EntityType.LLAMA_SPIT, LlamaSpitRenderer::new);
		register(EntityType.MAGMA_CUBE, MagmaCubeRenderer::new);
		register(EntityType.MARKER, NoopRenderer::new);
		register(EntityType.MINECART, context -> new MinecartRenderer(context, ModelLayers.MINECART));
		register(EntityType.MOOSHROOM, MushroomCowRenderer::new);
		register(EntityType.MULE, context -> new DonkeyRenderer<>(context, 0.92F, ModelLayers.MULE, ModelLayers.MULE_BABY, true));
		register(EntityType.OCELOT, OcelotRenderer::new);
		register(EntityType.PAINTING, PaintingRenderer::new);
		register(EntityType.PANDA, PandaRenderer::new);
		register(EntityType.PARROT, ParrotRenderer::new);
		register(EntityType.PHANTOM, PhantomRenderer::new);
		register(EntityType.PIG, PigRenderer::new);
		register(
			EntityType.PIGLIN,
			context -> new PiglinRenderer(
					context,
					ModelLayers.PIGLIN,
					ModelLayers.PIGLIN_BABY,
					ModelLayers.PIGLIN_INNER_ARMOR,
					ModelLayers.PIGLIN_OUTER_ARMOR,
					ModelLayers.PIGLIN_BABY_INNER_ARMOR,
					ModelLayers.PIGLIN_BABY_OUTER_ARMOR
				)
		);
		register(
			EntityType.PIGLIN_BRUTE,
			context -> new PiglinRenderer(
					context,
					ModelLayers.PIGLIN_BRUTE,
					ModelLayers.PIGLIN_BRUTE,
					ModelLayers.PIGLIN_BRUTE_INNER_ARMOR,
					ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR,
					ModelLayers.PIGLIN_BRUTE_INNER_ARMOR,
					ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR
				)
		);
		register(EntityType.PILLAGER, PillagerRenderer::new);
		register(EntityType.POLAR_BEAR, PolarBearRenderer::new);
		register(EntityType.POTION, ThrownItemRenderer::new);
		register(EntityType.PUFFERFISH, PufferfishRenderer::new);
		register(EntityType.RABBIT, RabbitRenderer::new);
		register(EntityType.RAVAGER, RavagerRenderer::new);
		register(EntityType.SALMON, SalmonRenderer::new);
		register(EntityType.SHEEP, SheepRenderer::new);
		register(EntityType.SHULKER, ShulkerRenderer::new);
		register(EntityType.SHULKER_BULLET, ShulkerBulletRenderer::new);
		register(EntityType.SILVERFISH, SilverfishRenderer::new);
		register(EntityType.SKELETON, SkeletonRenderer::new);
		register(EntityType.SKELETON_HORSE, context -> new UndeadHorseRenderer(context, ModelLayers.SKELETON_HORSE, ModelLayers.SKELETON_HORSE_BABY, true));
		register(EntityType.SLIME, SlimeRenderer::new);
		register(EntityType.SMALL_FIREBALL, context -> new ThrownItemRenderer<>(context, 0.75F, true));
		register(EntityType.SNIFFER, SnifferRenderer::new);
		register(EntityType.SNOWBALL, ThrownItemRenderer::new);
		register(EntityType.SNOW_GOLEM, SnowGolemRenderer::new);
		register(EntityType.SPAWNER_MINECART, context -> new MinecartRenderer(context, ModelLayers.SPAWNER_MINECART));
		register(EntityType.SPECTRAL_ARROW, SpectralArrowRenderer::new);
		register(EntityType.SPIDER, SpiderRenderer::new);
		register(
			EntityType.SQUID,
			context -> new SquidRenderer<>(context, new SquidModel(context.bakeLayer(ModelLayers.SQUID)), new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY)))
		);
		register(EntityType.STRAY, StrayRenderer::new);
		register(EntityType.STRIDER, StriderRenderer::new);
		register(EntityType.TADPOLE, TadpoleRenderer::new);
		register(EntityType.TEXT_DISPLAY, DisplayRenderer.TextDisplayRenderer::new);
		register(EntityType.TNT, TntRenderer::new);
		register(EntityType.TNT_MINECART, TntMinecartRenderer::new);
		register(EntityType.TRADER_LLAMA, context -> new LlamaRenderer(context, ModelLayers.TRADER_LLAMA, ModelLayers.TRADER_LLAMA_BABY));
		register(EntityType.TRIDENT, ThrownTridentRenderer::new);
		register(EntityType.TROPICAL_FISH, TropicalFishRenderer::new);
		register(EntityType.TURTLE, TurtleRenderer::new);
		register(EntityType.VEX, VexRenderer::new);
		register(EntityType.VILLAGER, VillagerRenderer::new);
		register(EntityType.VINDICATOR, VindicatorRenderer::new);
		register(EntityType.WARDEN, WardenRenderer::new);
		register(EntityType.WANDERING_TRADER, WanderingTraderRenderer::new);
		register(EntityType.WIND_CHARGE, WindChargeRenderer::new);
		register(EntityType.WITCH, WitchRenderer::new);
		register(EntityType.WITHER, WitherBossRenderer::new);
		register(EntityType.WITHER_SKELETON, WitherSkeletonRenderer::new);
		register(EntityType.WITHER_SKULL, WitherSkullRenderer::new);
		register(EntityType.WOLF, WolfRenderer::new);
		register(EntityType.ZOGLIN, ZoglinRenderer::new);
		register(EntityType.ZOMBIE, ZombieRenderer::new);
		register(EntityType.ZOMBIE_HORSE, context -> new UndeadHorseRenderer(context, ModelLayers.ZOMBIE_HORSE, ModelLayers.ZOMBIE_HORSE_BABY, false));
		register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
		register(
			EntityType.ZOMBIFIED_PIGLIN,
			context -> new ZombifiedPiglinRenderer(
					context,
					ModelLayers.ZOMBIFIED_PIGLIN,
					ModelLayers.ZOMBIFIED_PIGLIN_BABY,
					ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR,
					ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR,
					ModelLayers.ZOMBIFIED_PIGLIN_BABY_INNER_ARMOR,
					ModelLayers.ZOMBIFIED_PIGLIN_BABY_OUTER_ARMOR
				)
		);
	}
}
