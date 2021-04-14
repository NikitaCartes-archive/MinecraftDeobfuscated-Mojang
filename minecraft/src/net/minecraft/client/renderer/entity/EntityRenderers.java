package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class EntityRenderers {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String DEFAULT_PLAYER_MODEL = "default";
	private static final Map<EntityType<?>, EntityRendererProvider<?>> PROVIDERS = Maps.<EntityType<?>, EntityRendererProvider<?>>newHashMap();
	private static final Map<String, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS = ImmutableMap.of(
		"default", context -> new PlayerRenderer(context, false), "slim", context -> new PlayerRenderer(context, true)
	);

	private static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
		PROVIDERS.put(entityType, entityRendererProvider);
	}

	public static Map<EntityType<?>, EntityRenderer<?>> createEntityRenderers(EntityRendererProvider.Context context) {
		Builder<EntityType<?>, EntityRenderer<?>> builder = ImmutableMap.builder();
		PROVIDERS.forEach((entityType, entityRendererProvider) -> {
			try {
				builder.put(entityType, entityRendererProvider.create(context));
			} catch (Exception var5) {
				throw new IllegalArgumentException("Failed to create model for " + Registry.ENTITY_TYPE.getKey(entityType), var5);
			}
		});
		return builder.build();
	}

	public static Map<String, EntityRenderer<? extends Player>> createPlayerRenderers(EntityRendererProvider.Context context) {
		Builder<String, EntityRenderer<? extends Player>> builder = ImmutableMap.builder();
		PLAYER_PROVIDERS.forEach((string, entityRendererProvider) -> {
			try {
				builder.put(string, entityRendererProvider.create(context));
			} catch (Exception var5) {
				throw new IllegalArgumentException("Failed to create player model for " + string, var5);
			}
		});
		return builder.build();
	}

	public static boolean validateRegistrations() {
		boolean bl = true;

		for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
			if (entityType != EntityType.PLAYER && !PROVIDERS.containsKey(entityType)) {
				LOGGER.warn("No renderer registered for {}", Registry.ENTITY_TYPE.getKey(entityType));
				bl = false;
			}
		}

		return !bl;
	}

	static {
		register(EntityType.AREA_EFFECT_CLOUD, NoopRenderer::new);
		register(EntityType.ARMOR_STAND, ArmorStandRenderer::new);
		register(EntityType.ARROW, TippableArrowRenderer::new);
		register(EntityType.AXOLOTL, AxolotlRenderer::new);
		register(EntityType.BAT, BatRenderer::new);
		register(EntityType.BEE, BeeRenderer::new);
		register(EntityType.BLAZE, BlazeRenderer::new);
		register(EntityType.BOAT, BoatRenderer::new);
		register(EntityType.CAT, CatRenderer::new);
		register(EntityType.CAVE_SPIDER, CaveSpiderRenderer::new);
		register(EntityType.CHEST_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.CHEST_MINECART));
		register(EntityType.CHICKEN, ChickenRenderer::new);
		register(EntityType.COD, CodRenderer::new);
		register(EntityType.COMMAND_BLOCK_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.COMMAND_BLOCK_MINECART));
		register(EntityType.COW, CowRenderer::new);
		register(EntityType.CREEPER, CreeperRenderer::new);
		register(EntityType.DOLPHIN, DolphinRenderer::new);
		register(EntityType.DONKEY, context -> new ChestedHorseRenderer<>(context, 0.87F, ModelLayers.DONKEY));
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
		register(EntityType.FURNACE_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.FURNACE_MINECART));
		register(EntityType.GHAST, GhastRenderer::new);
		register(EntityType.GIANT, context -> new GiantMobRenderer(context, 6.0F));
		register(EntityType.GLOW_ITEM_FRAME, ItemFrameRenderer::new);
		register(EntityType.GLOW_SQUID, context -> new GlowSquidRenderer(context, new SquidModel<>(context.bakeLayer(ModelLayers.GLOW_SQUID))));
		register(EntityType.GOAT, GoatRenderer::new);
		register(EntityType.GUARDIAN, GuardianRenderer::new);
		register(EntityType.HOGLIN, HoglinRenderer::new);
		register(EntityType.HOPPER_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.HOPPER_MINECART));
		register(EntityType.HORSE, HorseRenderer::new);
		register(EntityType.HUSK, HuskRenderer::new);
		register(EntityType.ILLUSIONER, IllusionerRenderer::new);
		register(EntityType.IRON_GOLEM, IronGolemRenderer::new);
		register(EntityType.ITEM, ItemEntityRenderer::new);
		register(EntityType.ITEM_FRAME, ItemFrameRenderer::new);
		register(EntityType.LEASH_KNOT, LeashKnotRenderer::new);
		register(EntityType.LIGHTNING_BOLT, LightningBoltRenderer::new);
		register(EntityType.LLAMA, context -> new LlamaRenderer(context, ModelLayers.LLAMA));
		register(EntityType.LLAMA_SPIT, LlamaSpitRenderer::new);
		register(EntityType.MAGMA_CUBE, MagmaCubeRenderer::new);
		register(EntityType.MARKER, NoopRenderer::new);
		register(EntityType.MINECART, context -> new MinecartRenderer<>(context, ModelLayers.MINECART));
		register(EntityType.MOOSHROOM, MushroomCowRenderer::new);
		register(EntityType.MULE, context -> new ChestedHorseRenderer<>(context, 0.92F, ModelLayers.MULE));
		register(EntityType.OCELOT, OcelotRenderer::new);
		register(EntityType.PAINTING, PaintingRenderer::new);
		register(EntityType.PANDA, PandaRenderer::new);
		register(EntityType.PARROT, ParrotRenderer::new);
		register(EntityType.PHANTOM, PhantomRenderer::new);
		register(EntityType.PIG, PigRenderer::new);
		register(EntityType.PIGLIN, context -> new PiglinRenderer(context, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false));
		register(
			EntityType.PIGLIN_BRUTE,
			context -> new PiglinRenderer(context, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE_INNER_ARMOR, ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false)
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
		register(EntityType.SKELETON_HORSE, context -> new UndeadHorseRenderer(context, ModelLayers.SKELETON_HORSE));
		register(EntityType.SLIME, SlimeRenderer::new);
		register(EntityType.SMALL_FIREBALL, context -> new ThrownItemRenderer<>(context, 0.75F, true));
		register(EntityType.SNOWBALL, ThrownItemRenderer::new);
		register(EntityType.SNOW_GOLEM, SnowGolemRenderer::new);
		register(EntityType.SPAWNER_MINECART, context -> new MinecartRenderer<>(context, ModelLayers.SPAWNER_MINECART));
		register(EntityType.SPECTRAL_ARROW, SpectralArrowRenderer::new);
		register(EntityType.SPIDER, SpiderRenderer::new);
		register(EntityType.SQUID, context -> new SquidRenderer<>(context, new SquidModel<>(context.bakeLayer(ModelLayers.SQUID))));
		register(EntityType.STRAY, StrayRenderer::new);
		register(EntityType.STRIDER, StriderRenderer::new);
		register(EntityType.TNT, TntRenderer::new);
		register(EntityType.TNT_MINECART, TntMinecartRenderer::new);
		register(EntityType.TRADER_LLAMA, context -> new LlamaRenderer(context, ModelLayers.TRADER_LLAMA));
		register(EntityType.TRIDENT, ThrownTridentRenderer::new);
		register(EntityType.TROPICAL_FISH, TropicalFishRenderer::new);
		register(EntityType.TURTLE, TurtleRenderer::new);
		register(EntityType.VEX, VexRenderer::new);
		register(EntityType.VILLAGER, VillagerRenderer::new);
		register(EntityType.VINDICATOR, VindicatorRenderer::new);
		register(EntityType.WANDERING_TRADER, WanderingTraderRenderer::new);
		register(EntityType.WITCH, WitchRenderer::new);
		register(EntityType.WITHER, WitherBossRenderer::new);
		register(EntityType.WITHER_SKELETON, WitherSkeletonRenderer::new);
		register(EntityType.WITHER_SKULL, WitherSkullRenderer::new);
		register(EntityType.WOLF, WolfRenderer::new);
		register(EntityType.ZOGLIN, ZoglinRenderer::new);
		register(EntityType.ZOMBIE, ZombieRenderer::new);
		register(EntityType.ZOMBIE_HORSE, context -> new UndeadHorseRenderer(context, ModelLayers.ZOMBIE_HORSE));
		register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
		register(
			EntityType.ZOMBIFIED_PIGLIN,
			context -> new PiglinRenderer(
					context, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true
				)
		);
	}
}
