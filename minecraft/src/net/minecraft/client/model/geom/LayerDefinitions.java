package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.ArrowModel;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.BeeStingerModel;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.BoggedModel;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.CreakingModel;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.DonkeyModel;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerEarsModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.SpinAttackEffectModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.dragon.EnderDragonModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(EnvType.CLIENT)
public class LayerDefinitions {
	private static final CubeDeformation FISH_PATTERN_DEFORMATION = new CubeDeformation(0.008F);
	private static final CubeDeformation OUTER_ARMOR_DEFORMATION = new CubeDeformation(1.0F);
	private static final CubeDeformation INNER_ARMOR_DEFORMATION = new CubeDeformation(0.5F);

	public static Map<ModelLayerLocation, LayerDefinition> createRoots() {
		Builder<ModelLayerLocation, LayerDefinition> builder = ImmutableMap.builder();
		LayerDefinition layerDefinition = LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64);
		LayerDefinition layerDefinition2 = LayerDefinition.create(HumanoidArmorModel.createBodyLayer(OUTER_ARMOR_DEFORMATION), 64, 32);
		LayerDefinition layerDefinition3 = LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.02F)), 64, 32);
		LayerDefinition layerDefinition4 = LayerDefinition.create(HumanoidArmorModel.createBodyLayer(INNER_ARMOR_DEFORMATION), 64, 32);
		LayerDefinition layerDefinition5 = MinecartModel.createBodyLayer();
		LayerDefinition layerDefinition6 = SkullModel.createMobHeadLayer();
		LayerDefinition layerDefinition7 = LayerDefinition.create(AbstractEquineModel.createBodyMesh(CubeDeformation.NONE), 64, 64);
		LayerDefinition layerDefinition8 = LayerDefinition.create(AbstractEquineModel.createBabyMesh(CubeDeformation.NONE), 64, 64);
		MeshTransformer meshTransformer = MeshTransformer.scaling(0.9375F);
		LayerDefinition layerDefinition9 = IllagerModel.createBodyLayer().apply(meshTransformer);
		LayerDefinition layerDefinition10 = AxolotlModel.createBodyLayer();
		LayerDefinition layerDefinition11 = BeeModel.createBodyLayer();
		LayerDefinition layerDefinition12 = CowModel.createBodyLayer();
		LayerDefinition layerDefinition13 = layerDefinition12.apply(CowModel.BABY_TRANSFORMER);
		LayerDefinition layerDefinition14 = ElytraModel.createLayer();
		LayerDefinition layerDefinition15 = LayerDefinition.create(OcelotModel.createBodyMesh(CubeDeformation.NONE), 64, 32);
		LayerDefinition layerDefinition16 = layerDefinition15.apply(CatModel.CAT_TRANSFORMER);
		LayerDefinition layerDefinition17 = LayerDefinition.create(OcelotModel.createBodyMesh(new CubeDeformation(0.01F)), 64, 32).apply(CatModel.CAT_TRANSFORMER);
		LayerDefinition layerDefinition18 = LayerDefinition.create(PiglinModel.createMesh(CubeDeformation.NONE), 64, 64);
		LayerDefinition layerDefinition19 = LayerDefinition.create(PiglinHeadModel.createHeadModel(), 64, 64);
		LayerDefinition layerDefinition20 = SkullModel.createHumanoidHeadLayer();
		LayerDefinition layerDefinition21 = LlamaModel.createBodyLayer(CubeDeformation.NONE);
		LayerDefinition layerDefinition22 = LlamaModel.createBodyLayer(new CubeDeformation(0.5F));
		LayerDefinition layerDefinition23 = StriderModel.createBodyLayer();
		LayerDefinition layerDefinition24 = HoglinModel.createBodyLayer();
		LayerDefinition layerDefinition25 = HoglinModel.createBabyLayer();
		LayerDefinition layerDefinition26 = SkeletonModel.createBodyLayer();
		LayerDefinition layerDefinition27 = LayerDefinition.create(VillagerModel.createBodyModel(), 64, 64).apply(meshTransformer);
		LayerDefinition layerDefinition28 = SpiderModel.createSpiderBodyLayer();
		LayerDefinition layerDefinition29 = ArmadilloModel.createBodyLayer();
		LayerDefinition layerDefinition30 = CamelModel.createBodyLayer();
		LayerDefinition layerDefinition31 = ChickenModel.createBodyLayer();
		LayerDefinition layerDefinition32 = GoatModel.createBodyLayer();
		LayerDefinition layerDefinition33 = PandaModel.createBodyLayer();
		LayerDefinition layerDefinition34 = PigModel.createBodyLayer(CubeDeformation.NONE);
		LayerDefinition layerDefinition35 = PigModel.createBodyLayer(new CubeDeformation(0.5F));
		LayerDefinition layerDefinition36 = PolarBearModel.createBodyLayer();
		LayerDefinition layerDefinition37 = SheepModel.createBodyLayer();
		LayerDefinition layerDefinition38 = SheepFurModel.createFurLayer();
		LayerDefinition layerDefinition39 = SnifferModel.createBodyLayer();
		LayerDefinition layerDefinition40 = TurtleModel.createBodyLayer();
		LayerDefinition layerDefinition41 = LayerDefinition.create(WolfModel.createMeshDefinition(CubeDeformation.NONE), 64, 32);
		LayerDefinition layerDefinition42 = LayerDefinition.create(WolfModel.createMeshDefinition(new CubeDeformation(0.2F)), 64, 32);
		LayerDefinition layerDefinition43 = ZombieVillagerModel.createBodyLayer();
		LayerDefinition layerDefinition44 = ArmorStandModel.createBodyLayer();
		LayerDefinition layerDefinition45 = ArmorStandArmorModel.createBodyLayer(INNER_ARMOR_DEFORMATION);
		LayerDefinition layerDefinition46 = ArmorStandArmorModel.createBodyLayer(OUTER_ARMOR_DEFORMATION);
		LayerDefinition layerDefinition47 = DrownedModel.createBodyLayer(CubeDeformation.NONE);
		LayerDefinition layerDefinition48 = DrownedModel.createBodyLayer(new CubeDeformation(0.25F));
		LayerDefinition layerDefinition49 = SquidModel.createBodyLayer();
		LayerDefinition layerDefinition50 = DolphinModel.createBodyLayer();
		LayerDefinition layerDefinition51 = SalmonModel.createBodyLayer();
		builder.put(ModelLayers.ALLAY, AllayModel.createBodyLayer());
		builder.put(ModelLayers.ARMADILLO, layerDefinition29);
		builder.put(ModelLayers.ARMADILLO_BABY, layerDefinition29.apply(ArmadilloModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ARMOR_STAND, layerDefinition44);
		builder.put(ModelLayers.ARMOR_STAND_INNER_ARMOR, layerDefinition45);
		builder.put(ModelLayers.ARMOR_STAND_OUTER_ARMOR, layerDefinition46);
		builder.put(ModelLayers.ARMOR_STAND_SMALL, layerDefinition44.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ARMOR_STAND_SMALL_INNER_ARMOR, layerDefinition45.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ARMOR_STAND_SMALL_OUTER_ARMOR, layerDefinition46.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ARROW, ArrowModel.createBodyLayer());
		builder.put(ModelLayers.AXOLOTL, layerDefinition10);
		builder.put(ModelLayers.AXOLOTL_BABY, layerDefinition10.apply(AxolotlModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.BANNER, BannerRenderer.createBodyLayer());
		builder.put(ModelLayers.BAT, BatModel.createBodyLayer());
		builder.put(ModelLayers.BED_FOOT, BedRenderer.createFootLayer());
		builder.put(ModelLayers.BED_HEAD, BedRenderer.createHeadLayer());
		builder.put(ModelLayers.BEE, layerDefinition11);
		builder.put(ModelLayers.BEE_BABY, layerDefinition11.apply(BeeModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.BEE_STINGER, BeeStingerModel.createBodyLayer());
		builder.put(ModelLayers.BELL, BellModel.createBodyLayer());
		builder.put(ModelLayers.BLAZE, BlazeModel.createBodyLayer());
		builder.put(ModelLayers.BOAT_WATER_PATCH, BoatModel.createWaterPatch());
		builder.put(ModelLayers.BOGGED, BoggedModel.createBodyLayer());
		builder.put(ModelLayers.BOGGED_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.BOGGED_OUTER_ARMOR, layerDefinition2);
		builder.put(ModelLayers.BOGGED_OUTER_LAYER, LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.2F), 0.0F), 64, 32));
		builder.put(ModelLayers.BOOK, BookModel.createBodyLayer());
		builder.put(ModelLayers.BREEZE, BreezeModel.createBodyLayer(32, 32));
		builder.put(ModelLayers.BREEZE_WIND, BreezeModel.createBodyLayer(128, 128));
		builder.put(ModelLayers.CAT, layerDefinition16);
		builder.put(ModelLayers.CAT_BABY, layerDefinition16.apply(CatModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.CAT_COLLAR, layerDefinition17);
		builder.put(ModelLayers.CAT_BABY_COLLAR, layerDefinition17.apply(CatModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.CAMEL, layerDefinition30);
		builder.put(ModelLayers.CAMEL_BABY, layerDefinition30.apply(CamelModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.CAVE_SPIDER, layerDefinition28.apply(MeshTransformer.scaling(0.7F)));
		builder.put(ModelLayers.CHEST, ChestModel.createSingleBodyLayer());
		builder.put(ModelLayers.CHEST_MINECART, layerDefinition5);
		builder.put(ModelLayers.CHICKEN, layerDefinition31);
		builder.put(ModelLayers.CHICKEN_BABY, layerDefinition31.apply(ChickenModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.COD, CodModel.createBodyLayer());
		builder.put(ModelLayers.COMMAND_BLOCK_MINECART, layerDefinition5);
		builder.put(ModelLayers.CONDUIT_EYE, ConduitRenderer.createEyeLayer());
		builder.put(ModelLayers.CONDUIT_WIND, ConduitRenderer.createWindLayer());
		builder.put(ModelLayers.CONDUIT_SHELL, ConduitRenderer.createShellLayer());
		builder.put(ModelLayers.CONDUIT_CAGE, ConduitRenderer.createCageLayer());
		builder.put(ModelLayers.COW, layerDefinition12);
		builder.put(ModelLayers.COW_BABY, layerDefinition13);
		builder.put(ModelLayers.CREAKING, CreakingModel.createBodyLayer());
		builder.put(ModelLayers.CREEPER, CreeperModel.createBodyLayer(CubeDeformation.NONE));
		builder.put(ModelLayers.CREEPER_ARMOR, CreeperModel.createBodyLayer(new CubeDeformation(2.0F)));
		builder.put(ModelLayers.CREEPER_HEAD, layerDefinition6);
		builder.put(ModelLayers.DECORATED_POT_BASE, DecoratedPotRenderer.createBaseLayer());
		builder.put(ModelLayers.DECORATED_POT_SIDES, DecoratedPotRenderer.createSidesLayer());
		builder.put(ModelLayers.DOLPHIN, layerDefinition50);
		builder.put(ModelLayers.DOLPHIN_BABY, layerDefinition50.apply(DolphinModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.DONKEY, DonkeyModel.createBodyLayer());
		builder.put(ModelLayers.DONKEY_BABY, DonkeyModel.createBabyLayer());
		builder.put(ModelLayers.DOUBLE_CHEST_LEFT, ChestModel.createDoubleBodyLeftLayer());
		builder.put(ModelLayers.DOUBLE_CHEST_RIGHT, ChestModel.createDoubleBodyRightLayer());
		builder.put(ModelLayers.DRAGON_SKULL, DragonHeadModel.createHeadLayer());
		builder.put(ModelLayers.DROWNED, layerDefinition47);
		builder.put(ModelLayers.DROWNED_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.DROWNED_OUTER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.DROWNED_OUTER_LAYER, layerDefinition48);
		builder.put(ModelLayers.DROWNED_BABY, layerDefinition47.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.DROWNED_BABY_INNER_ARMOR, layerDefinition4.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.DROWNED_BABY_OUTER_ARMOR, layerDefinition4.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.DROWNED_BABY_OUTER_LAYER, layerDefinition48.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ELDER_GUARDIAN, GuardianModel.createElderGuardianLayer());
		builder.put(ModelLayers.ELYTRA, layerDefinition14);
		builder.put(ModelLayers.ELYTRA_BABY, layerDefinition14.apply(ElytraModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ENDERMAN, EndermanModel.createBodyLayer());
		builder.put(ModelLayers.ENDERMITE, EndermiteModel.createBodyLayer());
		builder.put(ModelLayers.ENDER_DRAGON, EnderDragonModel.createBodyLayer());
		builder.put(ModelLayers.END_CRYSTAL, EndCrystalModel.createBodyLayer());
		builder.put(ModelLayers.EVOKER, layerDefinition9);
		builder.put(ModelLayers.EVOKER_FANGS, EvokerFangsModel.createBodyLayer());
		builder.put(ModelLayers.FOX, FoxModel.createBodyLayer());
		builder.put(ModelLayers.FOX_BABY, FoxModel.createBodyLayer().apply(FoxModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.FROG, FrogModel.createBodyLayer());
		builder.put(ModelLayers.FURNACE_MINECART, layerDefinition5);
		builder.put(ModelLayers.GHAST, GhastModel.createBodyLayer());
		MeshTransformer meshTransformer2 = MeshTransformer.scaling(6.0F);
		builder.put(ModelLayers.GIANT, layerDefinition.apply(meshTransformer2));
		builder.put(ModelLayers.GIANT_INNER_ARMOR, layerDefinition4.apply(meshTransformer2));
		builder.put(ModelLayers.GIANT_OUTER_ARMOR, layerDefinition2.apply(meshTransformer2));
		builder.put(ModelLayers.GLOW_SQUID, layerDefinition49);
		builder.put(ModelLayers.GLOW_SQUID_BABY, layerDefinition49.apply(SquidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.GOAT, layerDefinition32);
		builder.put(ModelLayers.GOAT_BABY, layerDefinition32.apply(GoatModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.GUARDIAN, GuardianModel.createBodyLayer());
		builder.put(ModelLayers.HOGLIN, layerDefinition24);
		builder.put(ModelLayers.HOGLIN_BABY, layerDefinition25);
		builder.put(ModelLayers.HOPPER_MINECART, layerDefinition5);
		builder.put(ModelLayers.HORSE, layerDefinition7);
		builder.put(ModelLayers.HORSE_BABY, layerDefinition8);
		builder.put(ModelLayers.HORSE_ARMOR, LayerDefinition.create(AbstractEquineModel.createBodyMesh(new CubeDeformation(0.1F)), 64, 64));
		builder.put(ModelLayers.HORSE_BABY_ARMOR, LayerDefinition.create(AbstractEquineModel.createBabyMesh(new CubeDeformation(0.1F)), 64, 64));
		MeshTransformer meshTransformer3 = MeshTransformer.scaling(1.0625F);
		builder.put(ModelLayers.HUSK, layerDefinition.apply(meshTransformer3));
		builder.put(ModelLayers.HUSK_INNER_ARMOR, layerDefinition4.apply(meshTransformer3));
		builder.put(ModelLayers.HUSK_OUTER_ARMOR, layerDefinition2.apply(meshTransformer3));
		builder.put(ModelLayers.HUSK_BABY, layerDefinition.apply(HumanoidModel.BABY_TRANSFORMER).apply(meshTransformer3));
		builder.put(ModelLayers.HUSK_BABY_INNER_ARMOR, layerDefinition4.apply(HumanoidModel.BABY_TRANSFORMER).apply(meshTransformer3));
		builder.put(ModelLayers.HUSK_BABY_OUTER_ARMOR, layerDefinition2.apply(HumanoidModel.BABY_TRANSFORMER).apply(meshTransformer3));
		builder.put(ModelLayers.ILLUSIONER, layerDefinition9);
		builder.put(ModelLayers.IRON_GOLEM, IronGolemModel.createBodyLayer());
		builder.put(ModelLayers.LEASH_KNOT, LeashKnotModel.createBodyLayer());
		builder.put(ModelLayers.LLAMA, layerDefinition21);
		builder.put(ModelLayers.LLAMA_BABY, layerDefinition21.apply(LlamaModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.LLAMA_DECOR, layerDefinition22);
		builder.put(ModelLayers.LLAMA_BABY_DECOR, layerDefinition22.apply(LlamaModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.LLAMA_SPIT, LlamaSpitModel.createBodyLayer());
		builder.put(ModelLayers.MAGMA_CUBE, LavaSlimeModel.createBodyLayer());
		builder.put(ModelLayers.MINECART, layerDefinition5);
		builder.put(ModelLayers.MOOSHROOM, layerDefinition12);
		builder.put(ModelLayers.MOOSHROOM_BABY, layerDefinition13);
		builder.put(ModelLayers.MULE, DonkeyModel.createBodyLayer());
		builder.put(ModelLayers.MULE_BABY, DonkeyModel.createBabyLayer());
		builder.put(ModelLayers.OCELOT, layerDefinition15);
		builder.put(ModelLayers.OCELOT_BABY, layerDefinition15.apply(OcelotModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PANDA, layerDefinition33);
		builder.put(ModelLayers.PANDA_BABY, layerDefinition33.apply(PandaModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PARROT, ParrotModel.createBodyLayer());
		builder.put(ModelLayers.PHANTOM, PhantomModel.createBodyLayer());
		builder.put(ModelLayers.PIG, layerDefinition34);
		builder.put(ModelLayers.PIG_BABY, layerDefinition34.apply(PigModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PIG_SADDLE, layerDefinition35);
		builder.put(ModelLayers.PIG_BABY_SADDLE, layerDefinition35.apply(PigModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PIGLIN, layerDefinition18);
		builder.put(ModelLayers.PIGLIN_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.PIGLIN_OUTER_ARMOR, layerDefinition3);
		builder.put(ModelLayers.PIGLIN_BRUTE, layerDefinition18);
		builder.put(ModelLayers.PIGLIN_BRUTE_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, layerDefinition3);
		builder.put(ModelLayers.PIGLIN_BABY, layerDefinition18.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PIGLIN_BABY_INNER_ARMOR, layerDefinition4.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PIGLIN_BABY_OUTER_ARMOR, layerDefinition3.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PIGLIN_HEAD, layerDefinition19);
		builder.put(ModelLayers.PILLAGER, layerDefinition9);
		builder.put(ModelLayers.PLAYER, LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64));
		builder.put(ModelLayers.PLAYER_EARS, PlayerEarsModel.createEarsLayer());
		builder.put(ModelLayers.PLAYER_CAPE, PlayerCapeModel.createCapeLayer());
		builder.put(ModelLayers.PLAYER_HEAD, layerDefinition20);
		builder.put(ModelLayers.PLAYER_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.PLAYER_OUTER_ARMOR, layerDefinition2);
		builder.put(ModelLayers.PLAYER_SLIM, LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64));
		builder.put(ModelLayers.PLAYER_SLIM_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.PLAYER_SLIM_OUTER_ARMOR, layerDefinition2);
		builder.put(ModelLayers.PLAYER_SPIN_ATTACK, SpinAttackEffectModel.createLayer());
		builder.put(ModelLayers.POLAR_BEAR, layerDefinition36);
		builder.put(ModelLayers.POLAR_BEAR_BABY, layerDefinition36.apply(PolarBearModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.PUFFERFISH_BIG, PufferfishBigModel.createBodyLayer());
		builder.put(ModelLayers.PUFFERFISH_MEDIUM, PufferfishMidModel.createBodyLayer());
		builder.put(ModelLayers.PUFFERFISH_SMALL, PufferfishSmallModel.createBodyLayer());
		builder.put(ModelLayers.RABBIT, RabbitModel.createBodyLayer(false));
		builder.put(ModelLayers.RABBIT_BABY, RabbitModel.createBodyLayer(true));
		builder.put(ModelLayers.RAVAGER, RavagerModel.createBodyLayer());
		builder.put(ModelLayers.SALMON, layerDefinition51);
		builder.put(ModelLayers.SALMON_SMALL, layerDefinition51.apply(SalmonModel.SMALL_TRANSFORMER));
		builder.put(ModelLayers.SALMON_LARGE, layerDefinition51.apply(SalmonModel.LARGE_TRANSFORMER));
		builder.put(ModelLayers.SHEEP, layerDefinition37);
		builder.put(ModelLayers.SHEEP_BABY, layerDefinition37.apply(SheepModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.SHEEP_WOOL, layerDefinition38);
		builder.put(ModelLayers.SHEEP_BABY_WOOL, layerDefinition38.apply(SheepModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.SHIELD, ShieldModel.createLayer());
		builder.put(ModelLayers.SHULKER, ShulkerModel.createBodyLayer());
		builder.put(ModelLayers.SHULKER_BOX, ShulkerModel.createBoxLayer());
		builder.put(ModelLayers.SHULKER_BULLET, ShulkerBulletModel.createBodyLayer());
		builder.put(ModelLayers.SILVERFISH, SilverfishModel.createBodyLayer());
		builder.put(ModelLayers.SKELETON, layerDefinition26);
		builder.put(ModelLayers.SKELETON_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.SKELETON_OUTER_ARMOR, layerDefinition2);
		builder.put(ModelLayers.SKELETON_HORSE, layerDefinition7);
		builder.put(ModelLayers.SKELETON_HORSE_BABY, layerDefinition8);
		builder.put(ModelLayers.SKELETON_SKULL, layerDefinition6);
		builder.put(ModelLayers.SLIME, SlimeModel.createInnerBodyLayer());
		builder.put(ModelLayers.SLIME_OUTER, SlimeModel.createOuterBodyLayer());
		builder.put(ModelLayers.SNIFFER, layerDefinition39);
		builder.put(ModelLayers.SNIFFER_BABY, layerDefinition39.apply(SnifferModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.SNOW_GOLEM, SnowGolemModel.createBodyLayer());
		builder.put(ModelLayers.SPAWNER_MINECART, layerDefinition5);
		builder.put(ModelLayers.SPIDER, layerDefinition28);
		builder.put(ModelLayers.SQUID, layerDefinition49);
		builder.put(ModelLayers.SQUID_BABY, layerDefinition49.apply(SquidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.STRAY, layerDefinition26);
		builder.put(ModelLayers.STRAY_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.STRAY_OUTER_ARMOR, layerDefinition2);
		builder.put(ModelLayers.STRAY_OUTER_LAYER, LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25F), 0.0F), 64, 32));
		builder.put(ModelLayers.STRIDER, layerDefinition23);
		builder.put(ModelLayers.STRIDER_SADDLE, layerDefinition23);
		builder.put(ModelLayers.TADPOLE, TadpoleModel.createBodyLayer());
		builder.put(ModelLayers.TNT_MINECART, layerDefinition5);
		builder.put(ModelLayers.TRADER_LLAMA, layerDefinition21);
		builder.put(ModelLayers.TRADER_LLAMA_BABY, layerDefinition21.apply(LlamaModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.TRIDENT, TridentModel.createLayer());
		builder.put(ModelLayers.TROPICAL_FISH_LARGE, TropicalFishModelB.createBodyLayer(CubeDeformation.NONE));
		builder.put(ModelLayers.TROPICAL_FISH_LARGE_PATTERN, TropicalFishModelB.createBodyLayer(FISH_PATTERN_DEFORMATION));
		builder.put(ModelLayers.TROPICAL_FISH_SMALL, TropicalFishModelA.createBodyLayer(CubeDeformation.NONE));
		builder.put(ModelLayers.TROPICAL_FISH_SMALL_PATTERN, TropicalFishModelA.createBodyLayer(FISH_PATTERN_DEFORMATION));
		builder.put(ModelLayers.TURTLE, layerDefinition40);
		builder.put(ModelLayers.TURTLE_BABY, layerDefinition40.apply(TurtleModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.VEX, VexModel.createBodyLayer());
		builder.put(ModelLayers.VILLAGER, layerDefinition27);
		builder.put(ModelLayers.VINDICATOR, layerDefinition9);
		builder.put(ModelLayers.WARDEN, WardenModel.createBodyLayer());
		builder.put(ModelLayers.WANDERING_TRADER, layerDefinition27);
		builder.put(ModelLayers.WIND_CHARGE, WindChargeModel.createBodyLayer());
		builder.put(ModelLayers.WITCH, WitchModel.createBodyLayer().apply(meshTransformer));
		builder.put(ModelLayers.WITHER, WitherBossModel.createBodyLayer(CubeDeformation.NONE));
		builder.put(ModelLayers.WITHER_ARMOR, WitherBossModel.createBodyLayer(INNER_ARMOR_DEFORMATION));
		builder.put(ModelLayers.WITHER_SKULL, WitherSkullRenderer.createSkullLayer());
		MeshTransformer meshTransformer4 = MeshTransformer.scaling(1.2F);
		builder.put(ModelLayers.WITHER_SKELETON, layerDefinition26.apply(meshTransformer4));
		builder.put(ModelLayers.WITHER_SKELETON_INNER_ARMOR, layerDefinition4.apply(meshTransformer4));
		builder.put(ModelLayers.WITHER_SKELETON_OUTER_ARMOR, layerDefinition2.apply(meshTransformer4));
		builder.put(ModelLayers.WITHER_SKELETON_SKULL, layerDefinition6);
		builder.put(ModelLayers.WOLF, layerDefinition41);
		builder.put(ModelLayers.WOLF_ARMOR, layerDefinition42);
		builder.put(ModelLayers.WOLF_BABY, layerDefinition41.apply(WolfModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.WOLF_BABY_ARMOR, layerDefinition42.apply(WolfModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOGLIN, layerDefinition24);
		builder.put(ModelLayers.ZOGLIN_BABY, layerDefinition25);
		builder.put(ModelLayers.ZOMBIE, layerDefinition);
		builder.put(ModelLayers.ZOMBIE_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.ZOMBIE_OUTER_ARMOR, layerDefinition2);
		builder.put(ModelLayers.ZOMBIE_BABY, layerDefinition.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIE_BABY_INNER_ARMOR, layerDefinition4.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIE_BABY_OUTER_ARMOR, layerDefinition2.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIE_HEAD, layerDefinition20);
		builder.put(ModelLayers.ZOMBIE_HORSE, layerDefinition7);
		builder.put(ModelLayers.ZOMBIE_HORSE_BABY, layerDefinition8);
		builder.put(ModelLayers.ZOMBIE_VILLAGER, layerDefinition43);
		builder.put(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR, ZombieVillagerModel.createArmorLayer(INNER_ARMOR_DEFORMATION));
		builder.put(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR, ZombieVillagerModel.createArmorLayer(OUTER_ARMOR_DEFORMATION));
		builder.put(ModelLayers.ZOMBIE_VILLAGER_BABY, layerDefinition43.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIE_VILLAGER_BABY_INNER_ARMOR, ZombieVillagerModel.createArmorLayer(INNER_ARMOR_DEFORMATION).apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIE_VILLAGER_BABY_OUTER_ARMOR, ZombieVillagerModel.createArmorLayer(OUTER_ARMOR_DEFORMATION).apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIFIED_PIGLIN, layerDefinition18);
		builder.put(ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, layerDefinition4);
		builder.put(ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, layerDefinition3);
		builder.put(ModelLayers.ZOMBIFIED_PIGLIN_BABY, layerDefinition18.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIFIED_PIGLIN_BABY_INNER_ARMOR, layerDefinition4.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.ZOMBIFIED_PIGLIN_BABY_OUTER_ARMOR, layerDefinition3.apply(HumanoidModel.BABY_TRANSFORMER));
		builder.put(ModelLayers.BAMBOO_RAFT, RaftModel.createRaftModel());
		builder.put(ModelLayers.BAMBOO_CHEST_RAFT, RaftModel.createChestRaftModel());
		LayerDefinition layerDefinition52 = BoatModel.createBoatModel();
		LayerDefinition layerDefinition53 = BoatModel.createChestBoatModel();
		builder.put(ModelLayers.OAK_BOAT, layerDefinition52);
		builder.put(ModelLayers.OAK_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.SPRUCE_BOAT, layerDefinition52);
		builder.put(ModelLayers.SPRUCE_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.BIRCH_BOAT, layerDefinition52);
		builder.put(ModelLayers.BIRCH_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.JUNGLE_BOAT, layerDefinition52);
		builder.put(ModelLayers.JUNGLE_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.ACACIA_BOAT, layerDefinition52);
		builder.put(ModelLayers.ACACIA_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.CHERRY_BOAT, layerDefinition52);
		builder.put(ModelLayers.CHERRY_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.DARK_OAK_BOAT, layerDefinition52);
		builder.put(ModelLayers.DARK_OAK_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.PALE_OAK_BOAT, layerDefinition52);
		builder.put(ModelLayers.PALE_OAK_CHEST_BOAT, layerDefinition53);
		builder.put(ModelLayers.MANGROVE_BOAT, layerDefinition52);
		builder.put(ModelLayers.MANGROVE_CHEST_BOAT, layerDefinition53);
		LayerDefinition layerDefinition54 = SignRenderer.createSignLayer(true);
		LayerDefinition layerDefinition55 = SignRenderer.createSignLayer(false);
		LayerDefinition layerDefinition56 = HangingSignRenderer.createHangingSignLayer();
		WoodType.values().forEach(woodType -> {
			builder.put(ModelLayers.createStandingSignModelName(woodType), layerDefinition54);
			builder.put(ModelLayers.createWallSignModelName(woodType), layerDefinition55);
			builder.put(ModelLayers.createHangingSignModelName(woodType), layerDefinition56);
		});
		ImmutableMap<ModelLayerLocation, LayerDefinition> immutableMap = builder.build();
		List<ModelLayerLocation> list = (List<ModelLayerLocation>)ModelLayers.getKnownLocations()
			.filter(modelLayerLocation -> !immutableMap.containsKey(modelLayerLocation))
			.collect(Collectors.toList());
		if (!list.isEmpty()) {
			throw new IllegalStateException("Missing layer definitions: " + list);
		} else {
			return immutableMap;
		}
	}
}
