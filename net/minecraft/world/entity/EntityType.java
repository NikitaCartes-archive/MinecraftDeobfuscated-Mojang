/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import com.mojang.datafixers.DataFixUtils;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.entity.monster.Hoglin;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.PigZombie;
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
import org.jetbrains.annotations.Nullable;

public class EntityType<T extends Entity> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final EntityType<AreaEffectCloud> AREA_EFFECT_CLOUD = EntityType.register("area_effect_cloud", Builder.of(AreaEffectCloud::new, MobCategory.MISC).fireImmune().sized(6.0f, 0.5f));
    public static final EntityType<ArmorStand> ARMOR_STAND = EntityType.register("armor_stand", Builder.of(ArmorStand::new, MobCategory.MISC).sized(0.5f, 1.975f));
    public static final EntityType<Arrow> ARROW = EntityType.register("arrow", Builder.of(Arrow::new, MobCategory.MISC).sized(0.5f, 0.5f));
    public static final EntityType<Bat> BAT = EntityType.register("bat", Builder.of(Bat::new, MobCategory.AMBIENT).sized(0.5f, 0.9f));
    public static final EntityType<Bee> BEE = EntityType.register("bee", Builder.of(Bee::new, MobCategory.CREATURE).sized(0.7f, 0.6f));
    public static final EntityType<Blaze> BLAZE = EntityType.register("blaze", Builder.of(Blaze::new, MobCategory.MONSTER).fireImmune().sized(0.6f, 1.8f));
    public static final EntityType<Boat> BOAT = EntityType.register("boat", Builder.of(Boat::new, MobCategory.MISC).sized(1.375f, 0.5625f));
    public static final EntityType<Cat> CAT = EntityType.register("cat", Builder.of(Cat::new, MobCategory.CREATURE).sized(0.6f, 0.7f));
    public static final EntityType<CaveSpider> CAVE_SPIDER = EntityType.register("cave_spider", Builder.of(CaveSpider::new, MobCategory.MONSTER).sized(0.7f, 0.5f));
    public static final EntityType<Chicken> CHICKEN = EntityType.register("chicken", Builder.of(Chicken::new, MobCategory.CREATURE).sized(0.4f, 0.7f));
    public static final EntityType<Cod> COD = EntityType.register("cod", Builder.of(Cod::new, MobCategory.WATER_CREATURE).sized(0.5f, 0.3f));
    public static final EntityType<Cow> COW = EntityType.register("cow", Builder.of(Cow::new, MobCategory.CREATURE).sized(0.9f, 1.4f));
    public static final EntityType<Creeper> CREEPER = EntityType.register("creeper", Builder.of(Creeper::new, MobCategory.MONSTER).sized(0.6f, 1.7f));
    public static final EntityType<Donkey> DONKEY = EntityType.register("donkey", Builder.of(Donkey::new, MobCategory.CREATURE).sized(1.3964844f, 1.5f));
    public static final EntityType<Dolphin> DOLPHIN = EntityType.register("dolphin", Builder.of(Dolphin::new, MobCategory.WATER_CREATURE).sized(0.9f, 0.6f));
    public static final EntityType<DragonFireball> DRAGON_FIREBALL = EntityType.register("dragon_fireball", Builder.of(DragonFireball::new, MobCategory.MISC).sized(1.0f, 1.0f));
    public static final EntityType<Drowned> DROWNED = EntityType.register("drowned", Builder.of(Drowned::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<ElderGuardian> ELDER_GUARDIAN = EntityType.register("elder_guardian", Builder.of(ElderGuardian::new, MobCategory.MONSTER).sized(1.9975f, 1.9975f));
    public static final EntityType<EndCrystal> END_CRYSTAL = EntityType.register("end_crystal", Builder.of(EndCrystal::new, MobCategory.MISC).sized(2.0f, 2.0f));
    public static final EntityType<EnderDragon> ENDER_DRAGON = EntityType.register("ender_dragon", Builder.of(EnderDragon::new, MobCategory.MONSTER).fireImmune().sized(16.0f, 8.0f));
    public static final EntityType<EnderMan> ENDERMAN = EntityType.register("enderman", Builder.of(EnderMan::new, MobCategory.MONSTER).sized(0.6f, 2.9f));
    public static final EntityType<Endermite> ENDERMITE = EntityType.register("endermite", Builder.of(Endermite::new, MobCategory.MONSTER).sized(0.4f, 0.3f));
    public static final EntityType<EvokerFangs> EVOKER_FANGS = EntityType.register("evoker_fangs", Builder.of(EvokerFangs::new, MobCategory.MISC).sized(0.5f, 0.8f));
    public static final EntityType<Evoker> EVOKER = EntityType.register("evoker", Builder.of(Evoker::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<ExperienceOrb> EXPERIENCE_ORB = EntityType.register("experience_orb", Builder.of(ExperienceOrb::new, MobCategory.MISC).sized(0.5f, 0.5f));
    public static final EntityType<EyeOfEnder> EYE_OF_ENDER = EntityType.register("eye_of_ender", Builder.of(EyeOfEnder::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<FallingBlockEntity> FALLING_BLOCK = EntityType.register("falling_block", Builder.of(FallingBlockEntity::new, MobCategory.MISC).sized(0.98f, 0.98f));
    public static final EntityType<FireworkRocketEntity> FIREWORK_ROCKET = EntityType.register("firework_rocket", Builder.of(FireworkRocketEntity::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<Fox> FOX = EntityType.register("fox", Builder.of(Fox::new, MobCategory.CREATURE).sized(0.6f, 0.7f));
    public static final EntityType<Ghast> GHAST = EntityType.register("ghast", Builder.of(Ghast::new, MobCategory.MONSTER).fireImmune().sized(4.0f, 4.0f));
    public static final EntityType<Giant> GIANT = EntityType.register("giant", Builder.of(Giant::new, MobCategory.MONSTER).sized(3.6f, 12.0f));
    public static final EntityType<Guardian> GUARDIAN = EntityType.register("guardian", Builder.of(Guardian::new, MobCategory.MONSTER).sized(0.85f, 0.85f));
    public static final EntityType<Horse> HORSE = EntityType.register("horse", Builder.of(Horse::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f));
    public static final EntityType<Husk> HUSK = EntityType.register("husk", Builder.of(Husk::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<Illusioner> ILLUSIONER = EntityType.register("illusioner", Builder.of(Illusioner::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<ItemEntity> ITEM = EntityType.register("item", Builder.of(ItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<ItemFrame> ITEM_FRAME = EntityType.register("item_frame", Builder.of(ItemFrame::new, MobCategory.MISC).sized(0.5f, 0.5f));
    public static final EntityType<LargeFireball> FIREBALL = EntityType.register("fireball", Builder.of(LargeFireball::new, MobCategory.MISC).sized(1.0f, 1.0f));
    public static final EntityType<LeashFenceKnotEntity> LEASH_KNOT = EntityType.register("leash_knot", Builder.of(LeashFenceKnotEntity::new, MobCategory.MISC).noSave().sized(0.5f, 0.5f));
    public static final EntityType<Llama> LLAMA = EntityType.register("llama", Builder.of(Llama::new, MobCategory.CREATURE).sized(0.9f, 1.87f));
    public static final EntityType<LlamaSpit> LLAMA_SPIT = EntityType.register("llama_spit", Builder.of(LlamaSpit::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<MagmaCube> MAGMA_CUBE = EntityType.register("magma_cube", Builder.of(MagmaCube::new, MobCategory.MONSTER).fireImmune().sized(2.04f, 2.04f));
    public static final EntityType<Minecart> MINECART = EntityType.register("minecart", Builder.of(Minecart::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<MinecartChest> CHEST_MINECART = EntityType.register("chest_minecart", Builder.of(MinecartChest::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<MinecartCommandBlock> COMMAND_BLOCK_MINECART = EntityType.register("command_block_minecart", Builder.of(MinecartCommandBlock::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<MinecartFurnace> FURNACE_MINECART = EntityType.register("furnace_minecart", Builder.of(MinecartFurnace::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<MinecartHopper> HOPPER_MINECART = EntityType.register("hopper_minecart", Builder.of(MinecartHopper::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<MinecartSpawner> SPAWNER_MINECART = EntityType.register("spawner_minecart", Builder.of(MinecartSpawner::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<MinecartTNT> TNT_MINECART = EntityType.register("tnt_minecart", Builder.of(MinecartTNT::new, MobCategory.MISC).sized(0.98f, 0.7f));
    public static final EntityType<Mule> MULE = EntityType.register("mule", Builder.of(Mule::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f));
    public static final EntityType<MushroomCow> MOOSHROOM = EntityType.register("mooshroom", Builder.of(MushroomCow::new, MobCategory.CREATURE).sized(0.9f, 1.4f));
    public static final EntityType<Ocelot> OCELOT = EntityType.register("ocelot", Builder.of(Ocelot::new, MobCategory.CREATURE).sized(0.6f, 0.7f));
    public static final EntityType<Painting> PAINTING = EntityType.register("painting", Builder.of(Painting::new, MobCategory.MISC).sized(0.5f, 0.5f));
    public static final EntityType<Panda> PANDA = EntityType.register("panda", Builder.of(Panda::new, MobCategory.CREATURE).sized(1.3f, 1.25f));
    public static final EntityType<Parrot> PARROT = EntityType.register("parrot", Builder.of(Parrot::new, MobCategory.CREATURE).sized(0.5f, 0.9f));
    public static final EntityType<Pig> PIG = EntityType.register("pig", Builder.of(Pig::new, MobCategory.CREATURE).sized(0.9f, 0.9f));
    public static final EntityType<Pufferfish> PUFFERFISH = EntityType.register("pufferfish", Builder.of(Pufferfish::new, MobCategory.WATER_CREATURE).sized(0.7f, 0.7f));
    public static final EntityType<PigZombie> ZOMBIE_PIGMAN = EntityType.register("zombie_pigman", Builder.of(PigZombie::new, MobCategory.MONSTER).fireImmune().sized(0.6f, 1.95f));
    public static final EntityType<PolarBear> POLAR_BEAR = EntityType.register("polar_bear", Builder.of(PolarBear::new, MobCategory.CREATURE).sized(1.4f, 1.4f));
    public static final EntityType<PrimedTnt> TNT = EntityType.register("tnt", Builder.of(PrimedTnt::new, MobCategory.MISC).fireImmune().sized(0.98f, 0.98f));
    public static final EntityType<Rabbit> RABBIT = EntityType.register("rabbit", Builder.of(Rabbit::new, MobCategory.CREATURE).sized(0.4f, 0.5f));
    public static final EntityType<Salmon> SALMON = EntityType.register("salmon", Builder.of(Salmon::new, MobCategory.WATER_CREATURE).sized(0.7f, 0.4f));
    public static final EntityType<Sheep> SHEEP = EntityType.register("sheep", Builder.of(Sheep::new, MobCategory.CREATURE).sized(0.9f, 1.3f));
    public static final EntityType<Shulker> SHULKER = EntityType.register("shulker", Builder.of(Shulker::new, MobCategory.MONSTER).fireImmune().canSpawnFarFromPlayer().sized(1.0f, 1.0f));
    public static final EntityType<ShulkerBullet> SHULKER_BULLET = EntityType.register("shulker_bullet", Builder.of(ShulkerBullet::new, MobCategory.MISC).sized(0.3125f, 0.3125f));
    public static final EntityType<Silverfish> SILVERFISH = EntityType.register("silverfish", Builder.of(Silverfish::new, MobCategory.MONSTER).sized(0.4f, 0.3f));
    public static final EntityType<Skeleton> SKELETON = EntityType.register("skeleton", Builder.of(Skeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f));
    public static final EntityType<SkeletonHorse> SKELETON_HORSE = EntityType.register("skeleton_horse", Builder.of(SkeletonHorse::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f));
    public static final EntityType<Slime> SLIME = EntityType.register("slime", Builder.of(Slime::new, MobCategory.MONSTER).sized(2.04f, 2.04f));
    public static final EntityType<SmallFireball> SMALL_FIREBALL = EntityType.register("small_fireball", Builder.of(SmallFireball::new, MobCategory.MISC).sized(0.3125f, 0.3125f));
    public static final EntityType<SnowGolem> SNOW_GOLEM = EntityType.register("snow_golem", Builder.of(SnowGolem::new, MobCategory.MISC).sized(0.7f, 1.9f));
    public static final EntityType<Snowball> SNOWBALL = EntityType.register("snowball", Builder.of(Snowball::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<SpectralArrow> SPECTRAL_ARROW = EntityType.register("spectral_arrow", Builder.of(SpectralArrow::new, MobCategory.MISC).sized(0.5f, 0.5f));
    public static final EntityType<Spider> SPIDER = EntityType.register("spider", Builder.of(Spider::new, MobCategory.MONSTER).sized(1.4f, 0.9f));
    public static final EntityType<Squid> SQUID = EntityType.register("squid", Builder.of(Squid::new, MobCategory.WATER_CREATURE).sized(0.8f, 0.8f));
    public static final EntityType<Stray> STRAY = EntityType.register("stray", Builder.of(Stray::new, MobCategory.MONSTER).sized(0.6f, 1.99f));
    public static final EntityType<TraderLlama> TRADER_LLAMA = EntityType.register("trader_llama", Builder.of(TraderLlama::new, MobCategory.CREATURE).sized(0.9f, 1.87f));
    public static final EntityType<TropicalFish> TROPICAL_FISH = EntityType.register("tropical_fish", Builder.of(TropicalFish::new, MobCategory.WATER_CREATURE).sized(0.5f, 0.4f));
    public static final EntityType<Turtle> TURTLE = EntityType.register("turtle", Builder.of(Turtle::new, MobCategory.CREATURE).sized(1.2f, 0.4f));
    public static final EntityType<ThrownEgg> EGG = EntityType.register("egg", Builder.of(ThrownEgg::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<ThrownEnderpearl> ENDER_PEARL = EntityType.register("ender_pearl", Builder.of(ThrownEnderpearl::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<ThrownExperienceBottle> EXPERIENCE_BOTTLE = EntityType.register("experience_bottle", Builder.of(ThrownExperienceBottle::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<ThrownPotion> POTION = EntityType.register("potion", Builder.of(ThrownPotion::new, MobCategory.MISC).sized(0.25f, 0.25f));
    public static final EntityType<ThrownTrident> TRIDENT = EntityType.register("trident", Builder.of(ThrownTrident::new, MobCategory.MISC).sized(0.5f, 0.5f));
    public static final EntityType<Vex> VEX = EntityType.register("vex", Builder.of(Vex::new, MobCategory.MONSTER).fireImmune().sized(0.4f, 0.8f));
    public static final EntityType<Villager> VILLAGER = EntityType.register("villager", Builder.of(Villager::new, MobCategory.MISC).sized(0.6f, 1.95f));
    public static final EntityType<IronGolem> IRON_GOLEM = EntityType.register("iron_golem", Builder.of(IronGolem::new, MobCategory.MISC).sized(1.4f, 2.7f));
    public static final EntityType<Vindicator> VINDICATOR = EntityType.register("vindicator", Builder.of(Vindicator::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<Pillager> PILLAGER = EntityType.register("pillager", Builder.of(Pillager::new, MobCategory.MONSTER).canSpawnFarFromPlayer().sized(0.6f, 1.95f));
    public static final EntityType<WanderingTrader> WANDERING_TRADER = EntityType.register("wandering_trader", Builder.of(WanderingTrader::new, MobCategory.CREATURE).sized(0.6f, 1.95f));
    public static final EntityType<Witch> WITCH = EntityType.register("witch", Builder.of(Witch::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<WitherBoss> WITHER = EntityType.register("wither", Builder.of(WitherBoss::new, MobCategory.MONSTER).fireImmune().sized(0.9f, 3.5f));
    public static final EntityType<WitherSkeleton> WITHER_SKELETON = EntityType.register("wither_skeleton", Builder.of(WitherSkeleton::new, MobCategory.MONSTER).fireImmune().sized(0.7f, 2.4f));
    public static final EntityType<WitherSkull> WITHER_SKULL = EntityType.register("wither_skull", Builder.of(WitherSkull::new, MobCategory.MISC).sized(0.3125f, 0.3125f));
    public static final EntityType<Wolf> WOLF = EntityType.register("wolf", Builder.of(Wolf::new, MobCategory.CREATURE).sized(0.6f, 0.85f));
    public static final EntityType<Zombie> ZOMBIE = EntityType.register("zombie", Builder.of(Zombie::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<ZombieHorse> ZOMBIE_HORSE = EntityType.register("zombie_horse", Builder.of(ZombieHorse::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f));
    public static final EntityType<ZombieVillager> ZOMBIE_VILLAGER = EntityType.register("zombie_villager", Builder.of(ZombieVillager::new, MobCategory.MONSTER).sized(0.6f, 1.95f));
    public static final EntityType<Phantom> PHANTOM = EntityType.register("phantom", Builder.of(Phantom::new, MobCategory.MONSTER).sized(0.9f, 0.5f));
    public static final EntityType<Ravager> RAVAGER = EntityType.register("ravager", Builder.of(Ravager::new, MobCategory.MONSTER).sized(1.95f, 2.2f));
    public static final EntityType<Hoglin> HOGLIN = EntityType.register("hoglin", Builder.of(Hoglin::new, MobCategory.MONSTER).sized(0.9f, 0.9f));
    public static final EntityType<LightningBolt> LIGHTNING_BOLT = EntityType.register("lightning_bolt", Builder.createNothing(MobCategory.MISC).noSave().sized(0.0f, 0.0f));
    public static final EntityType<Player> PLAYER = EntityType.register("player", Builder.createNothing(MobCategory.MISC).noSave().noSummon().sized(0.6f, 1.8f));
    public static final EntityType<FishingHook> FISHING_BOBBER = EntityType.register("fishing_bobber", Builder.createNothing(MobCategory.MISC).noSave().noSummon().sized(0.25f, 0.25f));
    private final EntityFactory<T> factory;
    private final MobCategory category;
    private final boolean serialize;
    private final boolean summon;
    private final boolean fireImmune;
    private final boolean canSpawnFarFromPlayer;
    @Nullable
    private String descriptionId;
    @Nullable
    private Component description;
    @Nullable
    private ResourceLocation lootTable;
    private final EntityDimensions dimensions;

    private static <T extends Entity> EntityType<T> register(String string, Builder<T> builder) {
        return Registry.register(Registry.ENTITY_TYPE, string, builder.build(string));
    }

    public static ResourceLocation getKey(EntityType<?> entityType) {
        return Registry.ENTITY_TYPE.getKey(entityType);
    }

    public static Optional<EntityType<?>> byString(String string) {
        return Registry.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(string));
    }

    public EntityType(EntityFactory<T> entityFactory, MobCategory mobCategory, boolean bl, boolean bl2, boolean bl3, boolean bl4, EntityDimensions entityDimensions) {
        this.factory = entityFactory;
        this.category = mobCategory;
        this.canSpawnFarFromPlayer = bl4;
        this.serialize = bl;
        this.summon = bl2;
        this.fireImmune = bl3;
        this.dimensions = entityDimensions;
    }

    @Nullable
    public Entity spawn(Level level, @Nullable ItemStack itemStack, @Nullable Player player, BlockPos blockPos, MobSpawnType mobSpawnType, boolean bl, boolean bl2) {
        return this.spawn(level, itemStack == null ? null : itemStack.getTag(), itemStack != null && itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null, player, blockPos, mobSpawnType, bl, bl2);
    }

    @Nullable
    public T spawn(Level level, @Nullable CompoundTag compoundTag, @Nullable Component component, @Nullable Player player, BlockPos blockPos, MobSpawnType mobSpawnType, boolean bl, boolean bl2) {
        T entity = this.create(level, compoundTag, component, player, blockPos, mobSpawnType, bl, bl2);
        level.addFreshEntity((Entity)entity);
        return entity;
    }

    @Nullable
    public T create(Level level, @Nullable CompoundTag compoundTag, @Nullable Component component, @Nullable Player player, BlockPos blockPos, MobSpawnType mobSpawnType, boolean bl, boolean bl2) {
        double d;
        T entity = this.create(level);
        if (entity == null) {
            return null;
        }
        if (bl) {
            ((Entity)entity).setPos((double)blockPos.getX() + 0.5, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5);
            d = EntityType.getYOffset(level, blockPos, bl2, ((Entity)entity).getBoundingBox());
        } else {
            d = 0.0;
        }
        ((Entity)entity).moveTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + d, (double)blockPos.getZ() + 0.5, Mth.wrapDegrees(level.random.nextFloat() * 360.0f), 0.0f);
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.yHeadRot = mob.yRot;
            mob.yBodyRot = mob.yRot;
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(new BlockPos(mob)), mobSpawnType, null, compoundTag);
            mob.playAmbientSound();
        }
        if (component != null && entity instanceof LivingEntity) {
            ((Entity)entity).setCustomName(component);
        }
        EntityType.updateCustomEntityTag(level, player, entity, compoundTag);
        return entity;
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
        if (compoundTag == null || !compoundTag.contains("EntityTag", 10)) {
            return;
        }
        MinecraftServer minecraftServer = level.getServer();
        if (minecraftServer == null || entity == null) {
            return;
        }
        if (!(level.isClientSide || !entity.onlyOpCanSetNbt() || player != null && minecraftServer.getPlayerList().isOp(player.getGameProfile()))) {
            return;
        }
        CompoundTag compoundTag2 = entity.saveWithoutId(new CompoundTag());
        UUID uUID = entity.getUUID();
        compoundTag2.merge(compoundTag.getCompound("EntityTag"));
        entity.setUUID(uUID);
        entity.load(compoundTag2);
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
            this.description = new TranslatableComponent(this.getDescriptionId(), new Object[0]);
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
    @Environment(value=EnvType.CLIENT)
    public static Entity create(int i, Level level) {
        return EntityType.create(level, Registry.ENTITY_TYPE.byId(i));
    }

    public static Optional<Entity> create(CompoundTag compoundTag, Level level) {
        return Util.ifElse(EntityType.by(compoundTag).map(entityType -> entityType.create(level)), entity -> entity.load(compoundTag), () -> LOGGER.warn("Skipping Entity with id {}", (Object)compoundTag.getString("id")));
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    private static Entity create(Level level, @Nullable EntityType<?> entityType) {
        return entityType == null ? null : (Entity)entityType.create(level);
    }

    public AABB getAABB(double d, double e, double f) {
        float g = this.getWidth() / 2.0f;
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
        return EntityType.loadStaticEntity(compoundTag, level).map(function).map(entity -> {
            if (compoundTag.contains("Passengers", 9)) {
                ListTag listTag = compoundTag.getList("Passengers", 10);
                for (int i = 0; i < listTag.size(); ++i) {
                    Entity entity2 = EntityType.loadEntityRecursive(listTag.getCompound(i), level, function);
                    if (entity2 == null) continue;
                    entity2.startRiding((Entity)entity, true);
                }
            }
            return entity;
        }).orElse(null);
    }

    private static Optional<Entity> loadStaticEntity(CompoundTag compoundTag, Level level) {
        try {
            return EntityType.create(compoundTag, level);
        } catch (RuntimeException runtimeException) {
            LOGGER.warn("Exception loading entity: ", (Throwable)runtimeException);
            return Optional.empty();
        }
    }

    public int chunkRange() {
        if (this == PLAYER) {
            return 32;
        }
        if (this == END_CRYSTAL) {
            return 16;
        }
        if (this == ENDER_DRAGON || this == TNT || this == FALLING_BLOCK || this == ITEM_FRAME || this == LEASH_KNOT || this == PAINTING || this == ARMOR_STAND || this == EXPERIENCE_ORB || this == AREA_EFFECT_CLOUD || this == EVOKER_FANGS) {
            return 10;
        }
        if (this == FISHING_BOBBER || this == ARROW || this == SPECTRAL_ARROW || this == TRIDENT || this == SMALL_FIREBALL || this == DRAGON_FIREBALL || this == FIREBALL || this == WITHER_SKULL || this == SNOWBALL || this == LLAMA_SPIT || this == ENDER_PEARL || this == EYE_OF_ENDER || this == EGG || this == POTION || this == EXPERIENCE_BOTTLE || this == FIREWORK_ROCKET || this == ITEM) {
            return 4;
        }
        return 5;
    }

    public int updateInterval() {
        if (this == PLAYER || this == EVOKER_FANGS) {
            return 2;
        }
        if (this == EYE_OF_ENDER) {
            return 4;
        }
        if (this == FISHING_BOBBER) {
            return 5;
        }
        if (this == SMALL_FIREBALL || this == DRAGON_FIREBALL || this == FIREBALL || this == WITHER_SKULL || this == SNOWBALL || this == LLAMA_SPIT || this == ENDER_PEARL || this == EGG || this == POTION || this == EXPERIENCE_BOTTLE || this == FIREWORK_ROCKET || this == TNT) {
            return 10;
        }
        if (this == ARROW || this == SPECTRAL_ARROW || this == TRIDENT || this == ITEM || this == FALLING_BLOCK || this == EXPERIENCE_ORB) {
            return 20;
        }
        if (this == ITEM_FRAME || this == LEASH_KNOT || this == PAINTING || this == AREA_EFFECT_CLOUD || this == END_CRYSTAL) {
            return Integer.MAX_VALUE;
        }
        return 3;
    }

    public boolean trackDeltas() {
        return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
    }

    public boolean is(Tag<EntityType<?>> tag) {
        return tag.contains(this);
    }

    public static interface EntityFactory<T extends Entity> {
        public T create(EntityType<T> var1, Level var2);
    }

    public static class Builder<T extends Entity> {
        private final EntityFactory<T> factory;
        private final MobCategory category;
        private boolean serialize = true;
        private boolean summon = true;
        private boolean fireImmune;
        private boolean canSpawnFarFromPlayer;
        private EntityDimensions dimensions = EntityDimensions.scalable(0.6f, 1.8f);

        private Builder(EntityFactory<T> entityFactory, MobCategory mobCategory) {
            this.factory = entityFactory;
            this.category = mobCategory;
            this.canSpawnFarFromPlayer = mobCategory == MobCategory.CREATURE || mobCategory == MobCategory.MISC;
        }

        public static <T extends Entity> Builder<T> of(EntityFactory<T> entityFactory, MobCategory mobCategory) {
            return new Builder<T>(entityFactory, mobCategory);
        }

        public static <T extends Entity> Builder<T> createNothing(MobCategory mobCategory) {
            return new Builder<Entity>((entityType, level) -> null, mobCategory);
        }

        public Builder<T> sized(float f, float g) {
            this.dimensions = EntityDimensions.scalable(f, g);
            return this;
        }

        public Builder<T> noSummon() {
            this.summon = false;
            return this;
        }

        public Builder<T> noSave() {
            this.serialize = false;
            return this;
        }

        public Builder<T> fireImmune() {
            this.fireImmune = true;
            return this;
        }

        public Builder<T> canSpawnFarFromPlayer() {
            this.canSpawnFarFromPlayer = true;
            return this;
        }

        public EntityType<T> build(String string) {
            if (this.serialize) {
                try {
                    DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion())).getChoiceType(References.ENTITY_TREE, string);
                } catch (IllegalArgumentException illegalArgumentException) {
                    if (SharedConstants.IS_RUNNING_IN_IDE) {
                        throw illegalArgumentException;
                    }
                    LOGGER.warn("No data fixer registered for entity {}", (Object)string);
                }
            }
            return new EntityType<T>(this.factory, this.category, this.serialize, this.summon, this.fireImmune, this.canSpawnFarFromPlayer, this.dimensions);
        }
    }
}

