package net.minecraft.world.entity;

import com.mojang.datafixers.DataFixUtils;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityType<T extends Entity> {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final EntityType<AreaEffectCloud> AREA_EFFECT_CLOUD = register(
		"area_effect_cloud", EntityType.Builder.<AreaEffectCloud>of(AreaEffectCloud::new, MobCategory.MISC).fireImmune().sized(6.0F, 0.5F)
	);
	public static final EntityType<ArmorStand> ARMOR_STAND = register(
		"armor_stand", EntityType.Builder.<ArmorStand>of(ArmorStand::new, MobCategory.MISC).sized(0.5F, 1.975F)
	);
	public static final EntityType<Arrow> ARROW = register("arrow", EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC).sized(0.5F, 0.5F));
	public static final EntityType<Bat> BAT = register("bat", EntityType.Builder.of(Bat::new, MobCategory.AMBIENT).sized(0.5F, 0.9F));
	public static final EntityType<Bee> BEE = register("bee", EntityType.Builder.of(Bee::new, MobCategory.CREATURE).sized(0.7F, 0.6F));
	public static final EntityType<Blaze> BLAZE = register("blaze", EntityType.Builder.of(Blaze::new, MobCategory.MONSTER).fireImmune().sized(0.6F, 1.8F));
	public static final EntityType<Boat> BOAT = register("boat", EntityType.Builder.<Boat>of(Boat::new, MobCategory.MISC).sized(1.375F, 0.5625F));
	public static final EntityType<Cat> CAT = register("cat", EntityType.Builder.of(Cat::new, MobCategory.CREATURE).sized(0.6F, 0.7F));
	public static final EntityType<CaveSpider> CAVE_SPIDER = register("cave_spider", EntityType.Builder.of(CaveSpider::new, MobCategory.MONSTER).sized(0.7F, 0.5F));
	public static final EntityType<Chicken> CHICKEN = register("chicken", EntityType.Builder.of(Chicken::new, MobCategory.CREATURE).sized(0.4F, 0.7F));
	public static final EntityType<Cod> COD = register(
		"cod", EntityType.Builder.of(Cod::new, MobCategory.WATER_CREATURE).sized(0.5F, 0.3F).instantDespawnDistance(64)
	);
	public static final EntityType<Cow> COW = register("cow", EntityType.Builder.of(Cow::new, MobCategory.CREATURE).sized(0.9F, 1.4F));
	public static final EntityType<Creeper> CREEPER = register("creeper", EntityType.Builder.of(Creeper::new, MobCategory.MONSTER).sized(0.6F, 1.7F));
	public static final EntityType<Donkey> DONKEY = register("donkey", EntityType.Builder.of(Donkey::new, MobCategory.CREATURE).sized(1.3964844F, 1.5F));
	public static final EntityType<Dolphin> DOLPHIN = register("dolphin", EntityType.Builder.of(Dolphin::new, MobCategory.WATER_CREATURE).sized(0.9F, 0.6F));
	public static final EntityType<DragonFireball> DRAGON_FIREBALL = register(
		"dragon_fireball", EntityType.Builder.<DragonFireball>of(DragonFireball::new, MobCategory.MISC).sized(1.0F, 1.0F)
	);
	public static final EntityType<Drowned> DROWNED = register("drowned", EntityType.Builder.of(Drowned::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<ElderGuardian> ELDER_GUARDIAN = register(
		"elder_guardian", EntityType.Builder.of(ElderGuardian::new, MobCategory.MONSTER).sized(1.9975F, 1.9975F)
	);
	public static final EntityType<EndCrystal> END_CRYSTAL = register(
		"end_crystal", EntityType.Builder.<EndCrystal>of(EndCrystal::new, MobCategory.MISC).sized(2.0F, 2.0F)
	);
	public static final EntityType<EnderDragon> ENDER_DRAGON = register(
		"ender_dragon", EntityType.Builder.of(EnderDragon::new, MobCategory.MONSTER).fireImmune().sized(16.0F, 8.0F)
	);
	public static final EntityType<EnderMan> ENDERMAN = register("enderman", EntityType.Builder.of(EnderMan::new, MobCategory.MONSTER).sized(0.6F, 2.9F));
	public static final EntityType<Endermite> ENDERMITE = register("endermite", EntityType.Builder.of(Endermite::new, MobCategory.MONSTER).sized(0.4F, 0.3F));
	public static final EntityType<EvokerFangs> EVOKER_FANGS = register(
		"evoker_fangs", EntityType.Builder.<EvokerFangs>of(EvokerFangs::new, MobCategory.MISC).sized(0.5F, 0.8F)
	);
	public static final EntityType<Evoker> EVOKER = register("evoker", EntityType.Builder.of(Evoker::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<ExperienceOrb> EXPERIENCE_ORB = register(
		"experience_orb", EntityType.Builder.<ExperienceOrb>of(ExperienceOrb::new, MobCategory.MISC).sized(0.5F, 0.5F)
	);
	public static final EntityType<EyeOfEnder> EYE_OF_ENDER = register(
		"eye_of_ender", EntityType.Builder.<EyeOfEnder>of(EyeOfEnder::new, MobCategory.MISC).sized(0.25F, 0.25F)
	);
	public static final EntityType<FallingBlockEntity> FALLING_BLOCK = register(
		"falling_block", EntityType.Builder.<FallingBlockEntity>of(FallingBlockEntity::new, MobCategory.MISC).sized(0.98F, 0.98F)
	);
	public static final EntityType<FireworkRocketEntity> FIREWORK_ROCKET = register(
		"firework_rocket", EntityType.Builder.<FireworkRocketEntity>of(FireworkRocketEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
	);
	public static final EntityType<Fox> FOX = register("fox", EntityType.Builder.of(Fox::new, MobCategory.CREATURE).sized(0.6F, 0.7F));
	public static final EntityType<Ghast> GHAST = register("ghast", EntityType.Builder.of(Ghast::new, MobCategory.MONSTER).fireImmune().sized(4.0F, 4.0F));
	public static final EntityType<Giant> GIANT = register("giant", EntityType.Builder.of(Giant::new, MobCategory.MONSTER).sized(3.6F, 12.0F));
	public static final EntityType<Guardian> GUARDIAN = register("guardian", EntityType.Builder.of(Guardian::new, MobCategory.MONSTER).sized(0.85F, 0.85F));
	public static final EntityType<Horse> HORSE = register("horse", EntityType.Builder.of(Horse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F));
	public static final EntityType<Husk> HUSK = register("husk", EntityType.Builder.of(Husk::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<Illusioner> ILLUSIONER = register("illusioner", EntityType.Builder.of(Illusioner::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<ItemEntity> ITEM = register("item", EntityType.Builder.<ItemEntity>of(ItemEntity::new, MobCategory.MISC).sized(0.25F, 0.25F));
	public static final EntityType<ItemFrame> ITEM_FRAME = register(
		"item_frame", EntityType.Builder.<ItemFrame>of(ItemFrame::new, MobCategory.MISC).sized(0.5F, 0.5F)
	);
	public static final EntityType<LargeFireball> FIREBALL = register(
		"fireball", EntityType.Builder.<LargeFireball>of(LargeFireball::new, MobCategory.MISC).sized(1.0F, 1.0F)
	);
	public static final EntityType<LeashFenceKnotEntity> LEASH_KNOT = register(
		"leash_knot", EntityType.Builder.<LeashFenceKnotEntity>of(LeashFenceKnotEntity::new, MobCategory.MISC).noSave().sized(0.5F, 0.5F)
	);
	public static final EntityType<Llama> LLAMA = register("llama", EntityType.Builder.of(Llama::new, MobCategory.CREATURE).sized(0.9F, 1.87F));
	public static final EntityType<LlamaSpit> LLAMA_SPIT = register(
		"llama_spit", EntityType.Builder.<LlamaSpit>of(LlamaSpit::new, MobCategory.MISC).sized(0.25F, 0.25F)
	);
	public static final EntityType<MagmaCube> MAGMA_CUBE = register(
		"magma_cube", EntityType.Builder.of(MagmaCube::new, MobCategory.MONSTER).fireImmune().sized(2.04F, 2.04F)
	);
	public static final EntityType<Minecart> MINECART = register("minecart", EntityType.Builder.<Minecart>of(Minecart::new, MobCategory.MISC).sized(0.98F, 0.7F));
	public static final EntityType<MinecartChest> CHEST_MINECART = register(
		"chest_minecart", EntityType.Builder.<MinecartChest>of(MinecartChest::new, MobCategory.MISC).sized(0.98F, 0.7F)
	);
	public static final EntityType<MinecartCommandBlock> COMMAND_BLOCK_MINECART = register(
		"command_block_minecart", EntityType.Builder.<MinecartCommandBlock>of(MinecartCommandBlock::new, MobCategory.MISC).sized(0.98F, 0.7F)
	);
	public static final EntityType<MinecartFurnace> FURNACE_MINECART = register(
		"furnace_minecart", EntityType.Builder.<MinecartFurnace>of(MinecartFurnace::new, MobCategory.MISC).sized(0.98F, 0.7F)
	);
	public static final EntityType<MinecartHopper> HOPPER_MINECART = register(
		"hopper_minecart", EntityType.Builder.<MinecartHopper>of(MinecartHopper::new, MobCategory.MISC).sized(0.98F, 0.7F)
	);
	public static final EntityType<MinecartSpawner> SPAWNER_MINECART = register(
		"spawner_minecart", EntityType.Builder.<MinecartSpawner>of(MinecartSpawner::new, MobCategory.MISC).sized(0.98F, 0.7F)
	);
	public static final EntityType<MinecartTNT> TNT_MINECART = register(
		"tnt_minecart", EntityType.Builder.<MinecartTNT>of(MinecartTNT::new, MobCategory.MISC).sized(0.98F, 0.7F)
	);
	public static final EntityType<Mule> MULE = register("mule", EntityType.Builder.of(Mule::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F));
	public static final EntityType<MushroomCow> MOOSHROOM = register("mooshroom", EntityType.Builder.of(MushroomCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F));
	public static final EntityType<Ocelot> OCELOT = register("ocelot", EntityType.Builder.of(Ocelot::new, MobCategory.CREATURE).sized(0.6F, 0.7F));
	public static final EntityType<Painting> PAINTING = register("painting", EntityType.Builder.<Painting>of(Painting::new, MobCategory.MISC).sized(0.5F, 0.5F));
	public static final EntityType<Panda> PANDA = register("panda", EntityType.Builder.of(Panda::new, MobCategory.CREATURE).sized(1.3F, 1.25F));
	public static final EntityType<Parrot> PARROT = register("parrot", EntityType.Builder.of(Parrot::new, MobCategory.CREATURE).sized(0.5F, 0.9F));
	public static final EntityType<Pig> PIG = register("pig", EntityType.Builder.of(Pig::new, MobCategory.CREATURE).sized(0.9F, 0.9F));
	public static final EntityType<Pufferfish> PUFFERFISH = register(
		"pufferfish", EntityType.Builder.of(Pufferfish::new, MobCategory.WATER_CREATURE).sized(0.7F, 0.7F).instantDespawnDistance(64)
	);
	public static final EntityType<ZombifiedPiglin> ZOMBIFIED_PIGLIN = register(
		"zombified_piglin", EntityType.Builder.of(ZombifiedPiglin::new, MobCategory.MONSTER).fireImmune().sized(0.6F, 1.95F)
	);
	public static final EntityType<PolarBear> POLAR_BEAR = register("polar_bear", EntityType.Builder.of(PolarBear::new, MobCategory.CREATURE).sized(1.4F, 1.4F));
	public static final EntityType<PrimedTnt> TNT = register(
		"tnt", EntityType.Builder.<PrimedTnt>of(PrimedTnt::new, MobCategory.MISC).fireImmune().sized(0.98F, 0.98F)
	);
	public static final EntityType<Rabbit> RABBIT = register("rabbit", EntityType.Builder.of(Rabbit::new, MobCategory.CREATURE).sized(0.4F, 0.5F));
	public static final EntityType<Salmon> SALMON = register(
		"salmon", EntityType.Builder.of(Salmon::new, MobCategory.WATER_CREATURE).sized(0.7F, 0.4F).instantDespawnDistance(64)
	);
	public static final EntityType<Sheep> SHEEP = register("sheep", EntityType.Builder.of(Sheep::new, MobCategory.CREATURE).sized(0.9F, 1.3F));
	public static final EntityType<Shulker> SHULKER = register(
		"shulker", EntityType.Builder.of(Shulker::new, MobCategory.MONSTER).fireImmune().canSpawnFarFromPlayer().sized(1.0F, 1.0F)
	);
	public static final EntityType<ShulkerBullet> SHULKER_BULLET = register(
		"shulker_bullet", EntityType.Builder.<ShulkerBullet>of(ShulkerBullet::new, MobCategory.MISC).sized(0.3125F, 0.3125F)
	);
	public static final EntityType<Silverfish> SILVERFISH = register("silverfish", EntityType.Builder.of(Silverfish::new, MobCategory.MONSTER).sized(0.4F, 0.3F));
	public static final EntityType<Skeleton> SKELETON = register("skeleton", EntityType.Builder.of(Skeleton::new, MobCategory.MONSTER).sized(0.6F, 1.99F));
	public static final EntityType<SkeletonHorse> SKELETON_HORSE = register(
		"skeleton_horse", EntityType.Builder.of(SkeletonHorse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F)
	);
	public static final EntityType<Slime> SLIME = register("slime", EntityType.Builder.of(Slime::new, MobCategory.MONSTER).sized(2.04F, 2.04F));
	public static final EntityType<SmallFireball> SMALL_FIREBALL = register(
		"small_fireball", EntityType.Builder.<SmallFireball>of(SmallFireball::new, MobCategory.MISC).sized(0.3125F, 0.3125F)
	);
	public static final EntityType<SnowGolem> SNOW_GOLEM = register("snow_golem", EntityType.Builder.of(SnowGolem::new, MobCategory.MISC).sized(0.7F, 1.9F));
	public static final EntityType<Snowball> SNOWBALL = register("snowball", EntityType.Builder.<Snowball>of(Snowball::new, MobCategory.MISC).sized(0.25F, 0.25F));
	public static final EntityType<SpectralArrow> SPECTRAL_ARROW = register(
		"spectral_arrow", EntityType.Builder.<SpectralArrow>of(SpectralArrow::new, MobCategory.MISC).sized(0.5F, 0.5F)
	);
	public static final EntityType<Spider> SPIDER = register("spider", EntityType.Builder.of(Spider::new, MobCategory.MONSTER).sized(1.4F, 0.9F));
	public static final EntityType<Squid> SQUID = register("squid", EntityType.Builder.of(Squid::new, MobCategory.WATER_CREATURE).sized(0.8F, 0.8F));
	public static final EntityType<Stray> STRAY = register("stray", EntityType.Builder.of(Stray::new, MobCategory.MONSTER).sized(0.6F, 1.99F));
	public static final EntityType<TraderLlama> TRADER_LLAMA = register(
		"trader_llama", EntityType.Builder.of(TraderLlama::new, MobCategory.CREATURE).sized(0.9F, 1.87F)
	);
	public static final EntityType<TropicalFish> TROPICAL_FISH = register(
		"tropical_fish", EntityType.Builder.of(TropicalFish::new, MobCategory.WATER_CREATURE).sized(0.5F, 0.4F).instantDespawnDistance(64)
	);
	public static final EntityType<Turtle> TURTLE = register("turtle", EntityType.Builder.of(Turtle::new, MobCategory.CREATURE).sized(1.2F, 0.4F));
	public static final EntityType<ThrownEgg> EGG = register("egg", EntityType.Builder.<ThrownEgg>of(ThrownEgg::new, MobCategory.MISC).sized(0.25F, 0.25F));
	public static final EntityType<ThrownEnderpearl> ENDER_PEARL = register(
		"ender_pearl", EntityType.Builder.<ThrownEnderpearl>of(ThrownEnderpearl::new, MobCategory.MISC).sized(0.25F, 0.25F)
	);
	public static final EntityType<ThrownExperienceBottle> EXPERIENCE_BOTTLE = register(
		"experience_bottle", EntityType.Builder.<ThrownExperienceBottle>of(ThrownExperienceBottle::new, MobCategory.MISC).sized(0.25F, 0.25F)
	);
	public static final EntityType<ThrownPotion> POTION = register(
		"potion", EntityType.Builder.<ThrownPotion>of(ThrownPotion::new, MobCategory.MISC).sized(0.25F, 0.25F)
	);
	public static final EntityType<ThrownTrident> TRIDENT = register(
		"trident", EntityType.Builder.<ThrownTrident>of(ThrownTrident::new, MobCategory.MISC).sized(0.5F, 0.5F)
	);
	public static final EntityType<Vex> VEX = register("vex", EntityType.Builder.of(Vex::new, MobCategory.MONSTER).fireImmune().sized(0.4F, 0.8F));
	public static final EntityType<Villager> VILLAGER = register("villager", EntityType.Builder.<Villager>of(Villager::new, MobCategory.MISC).sized(0.6F, 1.95F));
	public static final EntityType<IronGolem> IRON_GOLEM = register("iron_golem", EntityType.Builder.of(IronGolem::new, MobCategory.MISC).sized(1.4F, 2.7F));
	public static final EntityType<Vindicator> VINDICATOR = register("vindicator", EntityType.Builder.of(Vindicator::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<Pillager> PILLAGER = register(
		"pillager", EntityType.Builder.of(Pillager::new, MobCategory.MONSTER).canSpawnFarFromPlayer().sized(0.6F, 1.95F)
	);
	public static final EntityType<WanderingTrader> WANDERING_TRADER = register(
		"wandering_trader", EntityType.Builder.of(WanderingTrader::new, MobCategory.CREATURE).sized(0.6F, 1.95F)
	);
	public static final EntityType<Witch> WITCH = register("witch", EntityType.Builder.of(Witch::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<WitherBoss> WITHER = register(
		"wither", EntityType.Builder.of(WitherBoss::new, MobCategory.MONSTER).fireImmune().sized(0.9F, 3.5F)
	);
	public static final EntityType<WitherSkeleton> WITHER_SKELETON = register(
		"wither_skeleton", EntityType.Builder.of(WitherSkeleton::new, MobCategory.MONSTER).fireImmune().sized(0.7F, 2.4F)
	);
	public static final EntityType<WitherSkull> WITHER_SKULL = register(
		"wither_skull", EntityType.Builder.<WitherSkull>of(WitherSkull::new, MobCategory.MISC).sized(0.3125F, 0.3125F)
	);
	public static final EntityType<Wolf> WOLF = register("wolf", EntityType.Builder.of(Wolf::new, MobCategory.CREATURE).sized(0.6F, 0.85F));
	public static final EntityType<Zombie> ZOMBIE = register("zombie", EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<ZombieHorse> ZOMBIE_HORSE = register(
		"zombie_horse", EntityType.Builder.of(ZombieHorse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F)
	);
	public static final EntityType<ZombieVillager> ZOMBIE_VILLAGER = register(
		"zombie_villager", EntityType.Builder.of(ZombieVillager::new, MobCategory.MONSTER).sized(0.6F, 1.95F)
	);
	public static final EntityType<Phantom> PHANTOM = register("phantom", EntityType.Builder.of(Phantom::new, MobCategory.MONSTER).sized(0.9F, 0.5F));
	public static final EntityType<Ravager> RAVAGER = register("ravager", EntityType.Builder.of(Ravager::new, MobCategory.MONSTER).sized(1.95F, 2.2F));
	public static final EntityType<Hoglin> HOGLIN = register("hoglin", EntityType.Builder.of(Hoglin::new, MobCategory.MONSTER).sized(1.3964844F, 1.4F));
	public static final EntityType<Piglin> PIGLIN = register("piglin", EntityType.Builder.of(Piglin::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
	public static final EntityType<LightningBolt> LIGHTNING_BOLT = register(
		"lightning_bolt", EntityType.Builder.<LightningBolt>createNothing(MobCategory.MISC).noSave().sized(0.0F, 0.0F)
	);
	public static final EntityType<Player> PLAYER = register(
		"player", EntityType.Builder.<Player>createNothing(MobCategory.MISC).noSave().noSummon().sized(0.6F, 1.8F)
	);
	public static final EntityType<FishingHook> FISHING_BOBBER = register(
		"fishing_bobber", EntityType.Builder.<FishingHook>createNothing(MobCategory.MISC).noSave().noSummon().sized(0.25F, 0.25F)
	);
	private final EntityType.EntityFactory<T> factory;
	private final MobCategory category;
	private final boolean serialize;
	private final boolean summon;
	private final boolean fireImmune;
	private final boolean canSpawnFarFromPlayer;
	private final int instantDespawnDistance;
	private final int noDespawnDistance;
	@Nullable
	private String descriptionId;
	@Nullable
	private Component description;
	@Nullable
	private ResourceLocation lootTable;
	private final EntityDimensions dimensions;

	private static <T extends Entity> EntityType<T> register(String string, EntityType.Builder<T> builder) {
		return Registry.register(Registry.ENTITY_TYPE, string, builder.build(string));
	}

	public static ResourceLocation getKey(EntityType<?> entityType) {
		return Registry.ENTITY_TYPE.getKey(entityType);
	}

	public static Optional<EntityType<?>> byString(String string) {
		return Registry.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(string));
	}

	public EntityType(
		EntityType.EntityFactory<T> entityFactory,
		MobCategory mobCategory,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		int i,
		int j,
		EntityDimensions entityDimensions
	) {
		this.factory = entityFactory;
		this.category = mobCategory;
		this.canSpawnFarFromPlayer = bl4;
		this.instantDespawnDistance = i;
		this.noDespawnDistance = j;
		this.serialize = bl;
		this.summon = bl2;
		this.fireImmune = bl3;
		this.dimensions = entityDimensions;
	}

	@Nullable
	public Entity spawn(Level level, @Nullable ItemStack itemStack, @Nullable Player player, BlockPos blockPos, MobSpawnType mobSpawnType, boolean bl, boolean bl2) {
		return this.spawn(
			level,
			itemStack == null ? null : itemStack.getTag(),
			itemStack != null && itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null,
			player,
			blockPos,
			mobSpawnType,
			bl,
			bl2
		);
	}

	@Nullable
	public T spawn(
		Level level,
		@Nullable CompoundTag compoundTag,
		@Nullable Component component,
		@Nullable Player player,
		BlockPos blockPos,
		MobSpawnType mobSpawnType,
		boolean bl,
		boolean bl2
	) {
		T entity = this.create(level, compoundTag, component, player, blockPos, mobSpawnType, bl, bl2);
		level.addFreshEntity(entity);
		return entity;
	}

	@Nullable
	public T create(
		Level level,
		@Nullable CompoundTag compoundTag,
		@Nullable Component component,
		@Nullable Player player,
		BlockPos blockPos,
		MobSpawnType mobSpawnType,
		boolean bl,
		boolean bl2
	) {
		T entity = this.create(level);
		if (entity == null) {
			return null;
		} else {
			double d;
			if (bl) {
				entity.setPos((double)blockPos.getX() + 0.5, (double)(blockPos.getY() + 1), (double)blockPos.getZ() + 0.5);
				d = getYOffset(level, blockPos, bl2, entity.getBoundingBox());
			} else {
				d = 0.0;
			}

			entity.moveTo(
				(double)blockPos.getX() + 0.5, (double)blockPos.getY() + d, (double)blockPos.getZ() + 0.5, Mth.wrapDegrees(level.random.nextFloat() * 360.0F), 0.0F
			);
			if (entity instanceof Mob) {
				Mob mob = (Mob)entity;
				mob.yHeadRot = mob.yRot;
				mob.yBodyRot = mob.yRot;
				mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), mobSpawnType, null, compoundTag);
				mob.playAmbientSound();
			}

			if (component != null && entity instanceof LivingEntity) {
				entity.setCustomName(component);
			}

			updateCustomEntityTag(level, player, entity, compoundTag);
			return entity;
		}
	}

	protected static double getYOffset(LevelReader levelReader, BlockPos blockPos, boolean bl, AABB aABB) {
		AABB aABB2 = new AABB(blockPos);
		if (bl) {
			aABB2 = aABB2.expandTowards(0.0, -1.0, 0.0);
		}

		Stream<VoxelShape> stream = levelReader.getCollisions(null, aABB2, Collections.emptySet());
		return 1.0 + Shapes.collide(Direction.Axis.Y, aABB, stream, bl ? -2.0 : -1.0);
	}

	public static void updateCustomEntityTag(Level level, @Nullable Player player, @Nullable Entity entity, @Nullable CompoundTag compoundTag) {
		if (compoundTag != null && compoundTag.contains("EntityTag", 10)) {
			MinecraftServer minecraftServer = level.getServer();
			if (minecraftServer != null && entity != null) {
				if (level.isClientSide || !entity.onlyOpCanSetNbt() || player != null && minecraftServer.getPlayerList().isOp(player.getGameProfile())) {
					CompoundTag compoundTag2 = entity.saveWithoutId(new CompoundTag());
					UUID uUID = entity.getUUID();
					compoundTag2.merge(compoundTag.getCompound("EntityTag"));
					entity.setUUID(uUID);
					entity.load(compoundTag2);
				}
			}
		}
	}

	public boolean canSerialize() {
		return this.serialize;
	}

	public boolean canSummon() {
		return this.summon;
	}

	public boolean fireImmune() {
		return this.fireImmune;
	}

	public boolean canSpawnFarFromPlayer() {
		return this.canSpawnFarFromPlayer;
	}

	public int getInstantDespawnDistance() {
		return this.instantDespawnDistance;
	}

	public int getNoDespawnDistance() {
		return this.noDespawnDistance;
	}

	public MobCategory getCategory() {
		return this.category;
	}

	public String getDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("entity", Registry.ENTITY_TYPE.getKey(this));
		}

		return this.descriptionId;
	}

	public Component getDescription() {
		if (this.description == null) {
			this.description = new TranslatableComponent(this.getDescriptionId());
		}

		return this.description;
	}

	public ResourceLocation getDefaultLootTable() {
		if (this.lootTable == null) {
			ResourceLocation resourceLocation = Registry.ENTITY_TYPE.getKey(this);
			this.lootTable = new ResourceLocation(resourceLocation.getNamespace(), "entities/" + resourceLocation.getPath());
		}

		return this.lootTable;
	}

	public float getWidth() {
		return this.dimensions.width;
	}

	public float getHeight() {
		return this.dimensions.height;
	}

	@Nullable
	public T create(Level level) {
		return this.factory.create(this, level);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static Entity create(int i, Level level) {
		return create(level, Registry.ENTITY_TYPE.byId(i));
	}

	public static Optional<Entity> create(CompoundTag compoundTag, Level level) {
		return Util.ifElse(
			by(compoundTag).map(entityType -> entityType.create(level)),
			entity -> entity.load(compoundTag),
			() -> LOGGER.warn("Skipping Entity with id {}", compoundTag.getString("id"))
		);
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	private static Entity create(Level level, @Nullable EntityType<?> entityType) {
		return entityType == null ? null : entityType.create(level);
	}

	public AABB getAABB(double d, double e, double f) {
		float g = this.getWidth() / 2.0F;
		return new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)this.getHeight(), f + (double)g);
	}

	public EntityDimensions getDimensions() {
		return this.dimensions;
	}

	public static Optional<EntityType<?>> by(CompoundTag compoundTag) {
		return Registry.ENTITY_TYPE.getOptional(new ResourceLocation(compoundTag.getString("id")));
	}

	@Nullable
	public static Entity loadEntityRecursive(CompoundTag compoundTag, Level level, Function<Entity, Entity> function) {
		return (Entity)loadStaticEntity(compoundTag, level).map(function).map(entity -> {
			if (compoundTag.contains("Passengers", 9)) {
				ListTag listTag = compoundTag.getList("Passengers", 10);

				for (int i = 0; i < listTag.size(); i++) {
					Entity entity2 = loadEntityRecursive(listTag.getCompound(i), level, function);
					if (entity2 != null) {
						entity2.startRiding(entity, true);
					}
				}
			}

			return entity;
		}).orElse(null);
	}

	private static Optional<Entity> loadStaticEntity(CompoundTag compoundTag, Level level) {
		try {
			return create(compoundTag, level);
		} catch (RuntimeException var3) {
			LOGGER.warn("Exception loading entity: ", (Throwable)var3);
			return Optional.empty();
		}
	}

	public int chunkRange() {
		if (this == PLAYER) {
			return 32;
		} else if (this == END_CRYSTAL) {
			return 16;
		} else if (this == ENDER_DRAGON
			|| this == TNT
			|| this == FALLING_BLOCK
			|| this == ITEM_FRAME
			|| this == LEASH_KNOT
			|| this == PAINTING
			|| this == ARMOR_STAND
			|| this == EXPERIENCE_ORB
			|| this == AREA_EFFECT_CLOUD
			|| this == EVOKER_FANGS) {
			return 10;
		} else {
			return this != FISHING_BOBBER
					&& this != ARROW
					&& this != SPECTRAL_ARROW
					&& this != TRIDENT
					&& this != SMALL_FIREBALL
					&& this != DRAGON_FIREBALL
					&& this != FIREBALL
					&& this != WITHER_SKULL
					&& this != SNOWBALL
					&& this != LLAMA_SPIT
					&& this != ENDER_PEARL
					&& this != EYE_OF_ENDER
					&& this != EGG
					&& this != POTION
					&& this != EXPERIENCE_BOTTLE
					&& this != FIREWORK_ROCKET
					&& this != ITEM
				? 5
				: 4;
		}
	}

	public int updateInterval() {
		if (this == PLAYER || this == EVOKER_FANGS) {
			return 2;
		} else if (this == EYE_OF_ENDER) {
			return 4;
		} else if (this == FISHING_BOBBER) {
			return 5;
		} else if (this == SMALL_FIREBALL
			|| this == DRAGON_FIREBALL
			|| this == FIREBALL
			|| this == WITHER_SKULL
			|| this == SNOWBALL
			|| this == LLAMA_SPIT
			|| this == ENDER_PEARL
			|| this == EGG
			|| this == POTION
			|| this == EXPERIENCE_BOTTLE
			|| this == FIREWORK_ROCKET
			|| this == TNT) {
			return 10;
		} else if (this == ARROW || this == SPECTRAL_ARROW || this == TRIDENT || this == ITEM || this == FALLING_BLOCK || this == EXPERIENCE_ORB) {
			return 20;
		} else {
			return this != ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != AREA_EFFECT_CLOUD && this != END_CRYSTAL ? 3 : Integer.MAX_VALUE;
		}
	}

	public boolean trackDeltas() {
		return this != PLAYER
			&& this != LLAMA_SPIT
			&& this != WITHER
			&& this != BAT
			&& this != ITEM_FRAME
			&& this != LEASH_KNOT
			&& this != PAINTING
			&& this != END_CRYSTAL
			&& this != EVOKER_FANGS;
	}

	public boolean is(Tag<EntityType<?>> tag) {
		return tag.contains(this);
	}

	public static class Builder<T extends Entity> {
		private final EntityType.EntityFactory<T> factory;
		private final MobCategory category;
		private boolean serialize = true;
		private boolean summon = true;
		private boolean fireImmune;
		private boolean canSpawnFarFromPlayer;
		private int instantDespawnDistance = 128;
		private int noDespawnDistance = 32;
		private EntityDimensions dimensions = EntityDimensions.scalable(0.6F, 1.8F);

		private Builder(EntityType.EntityFactory<T> entityFactory, MobCategory mobCategory) {
			this.factory = entityFactory;
			this.category = mobCategory;
			this.canSpawnFarFromPlayer = mobCategory == MobCategory.CREATURE || mobCategory == MobCategory.MISC;
		}

		public static <T extends Entity> EntityType.Builder<T> of(EntityType.EntityFactory<T> entityFactory, MobCategory mobCategory) {
			return new EntityType.Builder<>(entityFactory, mobCategory);
		}

		public static <T extends Entity> EntityType.Builder<T> createNothing(MobCategory mobCategory) {
			return new EntityType.Builder<>((entityType, level) -> null, mobCategory);
		}

		public EntityType.Builder<T> sized(float f, float g) {
			this.dimensions = EntityDimensions.scalable(f, g);
			return this;
		}

		public EntityType.Builder<T> noSummon() {
			this.summon = false;
			return this;
		}

		public EntityType.Builder<T> noSave() {
			this.serialize = false;
			return this;
		}

		public EntityType.Builder<T> fireImmune() {
			this.fireImmune = true;
			return this;
		}

		public EntityType.Builder<T> canSpawnFarFromPlayer() {
			this.canSpawnFarFromPlayer = true;
			return this;
		}

		public EntityType.Builder<T> instantDespawnDistance(int i) {
			this.instantDespawnDistance = i;
			return this;
		}

		public EntityType<T> build(String string) {
			if (this.serialize) {
				try {
					DataFixers.getDataFixer()
						.getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion()))
						.getChoiceType(References.ENTITY_TREE, string);
				} catch (IllegalArgumentException var3) {
					if (SharedConstants.IS_RUNNING_IN_IDE) {
						throw var3;
					}

					EntityType.LOGGER.warn("No data fixer registered for entity {}", string);
				}
			}

			return new EntityType<>(
				this.factory,
				this.category,
				this.serialize,
				this.summon,
				this.fireImmune,
				this.canSpawnFarFromPlayer,
				this.instantDespawnDistance,
				this.noDespawnDistance,
				this.dimensions
			);
		}
	}

	public interface EntityFactory<T extends Entity> {
		T create(EntityType<T> entityType, Level level);
	}
}
