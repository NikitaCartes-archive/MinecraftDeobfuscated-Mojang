package net.minecraft.util.datafix;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.AbstractArrowPickupFix;
import net.minecraft.util.datafix.fixes.AddFlagIfNotPresentFix;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.AdvancementsFix;
import net.minecraft.util.datafix.fixes.AdvancementsRenameFix;
import net.minecraft.util.datafix.fixes.AttributesRename;
import net.minecraft.util.datafix.fixes.BedItemColorFix;
import net.minecraft.util.datafix.fixes.BiomeFix;
import net.minecraft.util.datafix.fixes.BitStorageAlignFix;
import net.minecraft.util.datafix.fixes.BlendingDataFix;
import net.minecraft.util.datafix.fixes.BlendingDataRemoveFromNetherEndFix;
import net.minecraft.util.datafix.fixes.BlockEntityBannerColorFix;
import net.minecraft.util.datafix.fixes.BlockEntityBlockStateFix;
import net.minecraft.util.datafix.fixes.BlockEntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.BlockEntityIdFix;
import net.minecraft.util.datafix.fixes.BlockEntityJukeboxFix;
import net.minecraft.util.datafix.fixes.BlockEntityKeepPacked;
import net.minecraft.util.datafix.fixes.BlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.BlockEntityShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.BlockEntitySignDoubleSidedEditableTextFix;
import net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix;
import net.minecraft.util.datafix.fixes.BlockEntityUUIDFix;
import net.minecraft.util.datafix.fixes.BlockNameFlatteningFix;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.BlockRenameFixWithJigsaw;
import net.minecraft.util.datafix.fixes.BlockStateStructureTemplateFix;
import net.minecraft.util.datafix.fixes.CatTypeFix;
import net.minecraft.util.datafix.fixes.CauldronRenameFix;
import net.minecraft.util.datafix.fixes.CavesAndCliffsRenames;
import net.minecraft.util.datafix.fixes.ChunkBedBlockEntityInjecterFix;
import net.minecraft.util.datafix.fixes.ChunkBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkDeleteIgnoredLightDataFix;
import net.minecraft.util.datafix.fixes.ChunkDeleteLightFix;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkLightRemoveFix;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.util.datafix.fixes.ChunkProtoTickListFix;
import net.minecraft.util.datafix.fixes.ChunkRenamesFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix2;
import net.minecraft.util.datafix.fixes.ChunkStructuresTemplateRenameFix;
import net.minecraft.util.datafix.fixes.ChunkToProtochunkFix;
import net.minecraft.util.datafix.fixes.ColorlessShulkerEntityFix;
import net.minecraft.util.datafix.fixes.CriteriaRenameFix;
import net.minecraft.util.datafix.fixes.DecoratedPotFieldRenameFix;
import net.minecraft.util.datafix.fixes.DropInvalidSignDataFix;
import net.minecraft.util.datafix.fixes.DyeItemRenameFix;
import net.minecraft.util.datafix.fixes.EffectDurationFix;
import net.minecraft.util.datafix.fixes.EntityArmorStandSilentFix;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.EntityBrushableBlockFieldsRenameFix;
import net.minecraft.util.datafix.fixes.EntityCatSplitFix;
import net.minecraft.util.datafix.fixes.EntityCodSalmonFix;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.EntityElderGuardianSplitFix;
import net.minecraft.util.datafix.fixes.EntityEquipmentToArmorAndHandFix;
import net.minecraft.util.datafix.fixes.EntityGoatMissingStateFix;
import net.minecraft.util.datafix.fixes.EntityHealthFix;
import net.minecraft.util.datafix.fixes.EntityHorseSaddleFix;
import net.minecraft.util.datafix.fixes.EntityHorseSplitFix;
import net.minecraft.util.datafix.fixes.EntityIdFix;
import net.minecraft.util.datafix.fixes.EntityItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityMinecartIdentifiersFix;
import net.minecraft.util.datafix.fixes.EntityPaintingFieldsRenameFix;
import net.minecraft.util.datafix.fixes.EntityPaintingItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityPaintingMotiveFix;
import net.minecraft.util.datafix.fixes.EntityProjectileOwnerFix;
import net.minecraft.util.datafix.fixes.EntityPufferfishRenameFix;
import net.minecraft.util.datafix.fixes.EntityRavagerRenameFix;
import net.minecraft.util.datafix.fixes.EntityRedundantChanceTagsFix;
import net.minecraft.util.datafix.fixes.EntityRidingToPassengersFix;
import net.minecraft.util.datafix.fixes.EntityShulkerColorFix;
import net.minecraft.util.datafix.fixes.EntityShulkerRotationFix;
import net.minecraft.util.datafix.fixes.EntitySkeletonSplitFix;
import net.minecraft.util.datafix.fixes.EntityStringUuidFix;
import net.minecraft.util.datafix.fixes.EntityTheRenameningFix;
import net.minecraft.util.datafix.fixes.EntityTippedArrowFix;
import net.minecraft.util.datafix.fixes.EntityUUIDFix;
import net.minecraft.util.datafix.fixes.EntityVariantFix;
import net.minecraft.util.datafix.fixes.EntityWolfColorFix;
import net.minecraft.util.datafix.fixes.EntityZombieSplitFix;
import net.minecraft.util.datafix.fixes.EntityZombieVillagerTypeFix;
import net.minecraft.util.datafix.fixes.EntityZombifiedPiglinRenameFix;
import net.minecraft.util.datafix.fixes.FeatureFlagRemoveFix;
import net.minecraft.util.datafix.fixes.FilteredBooksFix;
import net.minecraft.util.datafix.fixes.FilteredSignsFix;
import net.minecraft.util.datafix.fixes.FixProjectileStoredItem;
import net.minecraft.util.datafix.fixes.ForcePoiRebuild;
import net.minecraft.util.datafix.fixes.FurnaceRecipeFix;
import net.minecraft.util.datafix.fixes.GoatHornIdFix;
import net.minecraft.util.datafix.fixes.GossipUUIDFix;
import net.minecraft.util.datafix.fixes.HeightmapRenamingFix;
import net.minecraft.util.datafix.fixes.IglooMetadataRemovalFix;
import net.minecraft.util.datafix.fixes.ItemBannerColorFix;
import net.minecraft.util.datafix.fixes.ItemCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemLoreFix;
import net.minecraft.util.datafix.fixes.ItemPotionFix;
import net.minecraft.util.datafix.fixes.ItemRemoveBlockEntityTagFix;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.ItemShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.ItemSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackEnchantmentNamesFix;
import net.minecraft.util.datafix.fixes.ItemStackMapIdFix;
import net.minecraft.util.datafix.fixes.ItemStackSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.util.datafix.fixes.ItemStackUUIDFix;
import net.minecraft.util.datafix.fixes.ItemWaterPotionFix;
import net.minecraft.util.datafix.fixes.ItemWrittenBookPagesStrictJsonFix;
import net.minecraft.util.datafix.fixes.JigsawPropertiesFix;
import net.minecraft.util.datafix.fixes.JigsawRotationFix;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.util.datafix.fixes.LegacyDragonFightFix;
import net.minecraft.util.datafix.fixes.LevelDataGeneratorOptionsFix;
import net.minecraft.util.datafix.fixes.LevelFlatGeneratorInfoFix;
import net.minecraft.util.datafix.fixes.LevelLegacyWorldGenSettingsFix;
import net.minecraft.util.datafix.fixes.LevelUUIDFix;
import net.minecraft.util.datafix.fixes.MapIdFix;
import net.minecraft.util.datafix.fixes.MemoryExpiryDataFix;
import net.minecraft.util.datafix.fixes.MissingDimensionFix;
import net.minecraft.util.datafix.fixes.MobEffectIdFix;
import net.minecraft.util.datafix.fixes.MobSpawnerEntityIdentifiersFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.NamespacedTypeRenameFix;
import net.minecraft.util.datafix.fixes.NewVillageFix;
import net.minecraft.util.datafix.fixes.ObjectiveDisplayNameFix;
import net.minecraft.util.datafix.fixes.ObjectiveRenderTypeFix;
import net.minecraft.util.datafix.fixes.OminousBannerBlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.OminousBannerRenameFix;
import net.minecraft.util.datafix.fixes.OptionsAccessibilityOnboardFix;
import net.minecraft.util.datafix.fixes.OptionsAddTextBackgroundFix;
import net.minecraft.util.datafix.fixes.OptionsAmbientOcclusionFix;
import net.minecraft.util.datafix.fixes.OptionsForceVBOFix;
import net.minecraft.util.datafix.fixes.OptionsKeyLwjgl3Fix;
import net.minecraft.util.datafix.fixes.OptionsKeyTranslationFix;
import net.minecraft.util.datafix.fixes.OptionsLowerCaseLanguageFix;
import net.minecraft.util.datafix.fixes.OptionsProgrammerArtFix;
import net.minecraft.util.datafix.fixes.OptionsRenameFieldFix;
import net.minecraft.util.datafix.fixes.OverreachingTickFix;
import net.minecraft.util.datafix.fixes.PlayerUUIDFix;
import net.minecraft.util.datafix.fixes.PoiTypeRemoveFix;
import net.minecraft.util.datafix.fixes.PoiTypeRenameFix;
import net.minecraft.util.datafix.fixes.PrimedTntBlockStateFixer;
import net.minecraft.util.datafix.fixes.RandomSequenceSettingsFix;
import net.minecraft.util.datafix.fixes.RecipesFix;
import net.minecraft.util.datafix.fixes.RecipesRenameningFix;
import net.minecraft.util.datafix.fixes.RedstoneWireConnectionsFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.RemapChunkStatusFix;
import net.minecraft.util.datafix.fixes.RemoveGolemGossipFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFansFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFix;
import net.minecraft.util.datafix.fixes.ReorganizePoi;
import net.minecraft.util.datafix.fixes.SavedDataFeaturePoolElementFix;
import net.minecraft.util.datafix.fixes.SavedDataUUIDFix;
import net.minecraft.util.datafix.fixes.ScoreboardDisplaySlotFix;
import net.minecraft.util.datafix.fixes.SpawnerDataFix;
import net.minecraft.util.datafix.fixes.StatsCounterFix;
import net.minecraft.util.datafix.fixes.StatsRenameFix;
import net.minecraft.util.datafix.fixes.StriderGravityFix;
import net.minecraft.util.datafix.fixes.StructureReferenceCountFix;
import net.minecraft.util.datafix.fixes.StructureSettingsFlattenFix;
import net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix;
import net.minecraft.util.datafix.fixes.TeamDisplayNameFix;
import net.minecraft.util.datafix.fixes.TrappedChestBlockEntityFix;
import net.minecraft.util.datafix.fixes.VariantRenameFix;
import net.minecraft.util.datafix.fixes.VillagerDataFix;
import net.minecraft.util.datafix.fixes.VillagerFollowRangeFix;
import net.minecraft.util.datafix.fixes.VillagerRebuildLevelAndXpFix;
import net.minecraft.util.datafix.fixes.VillagerTradeFix;
import net.minecraft.util.datafix.fixes.WallPropertyFix;
import net.minecraft.util.datafix.fixes.WeaponSmithChestLootTableFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsDisallowOldCustomWorldsFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsFix;
import net.minecraft.util.datafix.fixes.WorldGenSettingsHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.WriteAndReadFix;
import net.minecraft.util.datafix.fixes.ZombieVillagerRebuildXpFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;
import net.minecraft.util.datafix.schemas.V102;
import net.minecraft.util.datafix.schemas.V1022;
import net.minecraft.util.datafix.schemas.V106;
import net.minecraft.util.datafix.schemas.V107;
import net.minecraft.util.datafix.schemas.V1125;
import net.minecraft.util.datafix.schemas.V135;
import net.minecraft.util.datafix.schemas.V143;
import net.minecraft.util.datafix.schemas.V1451;
import net.minecraft.util.datafix.schemas.V1451_1;
import net.minecraft.util.datafix.schemas.V1451_2;
import net.minecraft.util.datafix.schemas.V1451_3;
import net.minecraft.util.datafix.schemas.V1451_4;
import net.minecraft.util.datafix.schemas.V1451_5;
import net.minecraft.util.datafix.schemas.V1451_6;
import net.minecraft.util.datafix.schemas.V1460;
import net.minecraft.util.datafix.schemas.V1466;
import net.minecraft.util.datafix.schemas.V1470;
import net.minecraft.util.datafix.schemas.V1481;
import net.minecraft.util.datafix.schemas.V1483;
import net.minecraft.util.datafix.schemas.V1486;
import net.minecraft.util.datafix.schemas.V1510;
import net.minecraft.util.datafix.schemas.V1800;
import net.minecraft.util.datafix.schemas.V1801;
import net.minecraft.util.datafix.schemas.V1904;
import net.minecraft.util.datafix.schemas.V1906;
import net.minecraft.util.datafix.schemas.V1909;
import net.minecraft.util.datafix.schemas.V1920;
import net.minecraft.util.datafix.schemas.V1928;
import net.minecraft.util.datafix.schemas.V1929;
import net.minecraft.util.datafix.schemas.V1931;
import net.minecraft.util.datafix.schemas.V2100;
import net.minecraft.util.datafix.schemas.V2501;
import net.minecraft.util.datafix.schemas.V2502;
import net.minecraft.util.datafix.schemas.V2505;
import net.minecraft.util.datafix.schemas.V2509;
import net.minecraft.util.datafix.schemas.V2519;
import net.minecraft.util.datafix.schemas.V2522;
import net.minecraft.util.datafix.schemas.V2551;
import net.minecraft.util.datafix.schemas.V2568;
import net.minecraft.util.datafix.schemas.V2571;
import net.minecraft.util.datafix.schemas.V2684;
import net.minecraft.util.datafix.schemas.V2686;
import net.minecraft.util.datafix.schemas.V2688;
import net.minecraft.util.datafix.schemas.V2704;
import net.minecraft.util.datafix.schemas.V2707;
import net.minecraft.util.datafix.schemas.V2831;
import net.minecraft.util.datafix.schemas.V2832;
import net.minecraft.util.datafix.schemas.V2842;
import net.minecraft.util.datafix.schemas.V3076;
import net.minecraft.util.datafix.schemas.V3078;
import net.minecraft.util.datafix.schemas.V3081;
import net.minecraft.util.datafix.schemas.V3082;
import net.minecraft.util.datafix.schemas.V3083;
import net.minecraft.util.datafix.schemas.V3202;
import net.minecraft.util.datafix.schemas.V3203;
import net.minecraft.util.datafix.schemas.V3204;
import net.minecraft.util.datafix.schemas.V3325;
import net.minecraft.util.datafix.schemas.V3326;
import net.minecraft.util.datafix.schemas.V3327;
import net.minecraft.util.datafix.schemas.V3328;
import net.minecraft.util.datafix.schemas.V3438;
import net.minecraft.util.datafix.schemas.V3448;
import net.minecraft.util.datafix.schemas.V3682;
import net.minecraft.util.datafix.schemas.V3683;
import net.minecraft.util.datafix.schemas.V3685;
import net.minecraft.util.datafix.schemas.V3689;
import net.minecraft.util.datafix.schemas.V501;
import net.minecraft.util.datafix.schemas.V700;
import net.minecraft.util.datafix.schemas.V701;
import net.minecraft.util.datafix.schemas.V702;
import net.minecraft.util.datafix.schemas.V703;
import net.minecraft.util.datafix.schemas.V704;
import net.minecraft.util.datafix.schemas.V705;
import net.minecraft.util.datafix.schemas.V808;
import net.minecraft.util.datafix.schemas.V99;

public class DataFixers {
	private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
	private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;
	private static final DataFixer dataFixer = createFixerUpper(SharedConstants.DATA_FIX_TYPES_TO_OPTIMIZE);
	public static final int BLENDING_VERSION = 3441;

	private DataFixers() {
	}

	public static DataFixer getDataFixer() {
		return dataFixer;
	}

	private static synchronized DataFixer createFixerUpper(Set<TypeReference> set) {
		DataFixerBuilder dataFixerBuilder = new DataFixerBuilder(SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		addFixers(dataFixerBuilder);
		if (set.isEmpty()) {
			return dataFixerBuilder.buildUnoptimized();
		} else {
			Executor executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Datafixer Bootstrap").setDaemon(true).setPriority(1).build());
			return dataFixerBuilder.buildOptimized(set, executor);
		}
	}

	private static void addFixers(DataFixerBuilder dataFixerBuilder) {
		dataFixerBuilder.addSchema(99, V99::new);
		Schema schema = dataFixerBuilder.addSchema(100, V100::new);
		dataFixerBuilder.addFixer(new EntityEquipmentToArmorAndHandFix(schema, true));
		Schema schema2 = dataFixerBuilder.addSchema(101, SAME);
		dataFixerBuilder.addFixer(new BlockEntitySignTextStrictJsonFix(schema2, false));
		Schema schema3 = dataFixerBuilder.addSchema(102, V102::new);
		dataFixerBuilder.addFixer(new ItemIdFix(schema3, true));
		dataFixerBuilder.addFixer(new ItemPotionFix(schema3, false));
		Schema schema4 = dataFixerBuilder.addSchema(105, SAME);
		dataFixerBuilder.addFixer(new ItemSpawnEggFix(schema4, true));
		Schema schema5 = dataFixerBuilder.addSchema(106, V106::new);
		dataFixerBuilder.addFixer(new MobSpawnerEntityIdentifiersFix(schema5, true));
		Schema schema6 = dataFixerBuilder.addSchema(107, V107::new);
		dataFixerBuilder.addFixer(new EntityMinecartIdentifiersFix(schema6, true));
		Schema schema7 = dataFixerBuilder.addSchema(108, SAME);
		dataFixerBuilder.addFixer(new EntityStringUuidFix(schema7, true));
		Schema schema8 = dataFixerBuilder.addSchema(109, SAME);
		dataFixerBuilder.addFixer(new EntityHealthFix(schema8, true));
		Schema schema9 = dataFixerBuilder.addSchema(110, SAME);
		dataFixerBuilder.addFixer(new EntityHorseSaddleFix(schema9, true));
		Schema schema10 = dataFixerBuilder.addSchema(111, SAME);
		dataFixerBuilder.addFixer(new EntityPaintingItemFrameDirectionFix(schema10, true));
		Schema schema11 = dataFixerBuilder.addSchema(113, SAME);
		dataFixerBuilder.addFixer(new EntityRedundantChanceTagsFix(schema11, true));
		Schema schema12 = dataFixerBuilder.addSchema(135, V135::new);
		dataFixerBuilder.addFixer(new EntityRidingToPassengersFix(schema12, true));
		Schema schema13 = dataFixerBuilder.addSchema(143, V143::new);
		dataFixerBuilder.addFixer(new EntityTippedArrowFix(schema13, true));
		Schema schema14 = dataFixerBuilder.addSchema(147, SAME);
		dataFixerBuilder.addFixer(new EntityArmorStandSilentFix(schema14, true));
		Schema schema15 = dataFixerBuilder.addSchema(165, SAME);
		dataFixerBuilder.addFixer(new ItemWrittenBookPagesStrictJsonFix(schema15, true));
		Schema schema16 = dataFixerBuilder.addSchema(501, V501::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema16, "Add 1.10 entities fix", References.ENTITY));
		Schema schema17 = dataFixerBuilder.addSchema(502, SAME);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema17,
				"cooked_fished item renamer",
				string -> Objects.equals(NamespacedSchema.ensureNamespaced(string), "minecraft:cooked_fished") ? "minecraft:cooked_fish" : string
			)
		);
		dataFixerBuilder.addFixer(new EntityZombieVillagerTypeFix(schema17, false));
		Schema schema18 = dataFixerBuilder.addSchema(505, SAME);
		dataFixerBuilder.addFixer(new OptionsForceVBOFix(schema18, false));
		Schema schema19 = dataFixerBuilder.addSchema(700, V700::new);
		dataFixerBuilder.addFixer(new EntityElderGuardianSplitFix(schema19, true));
		Schema schema20 = dataFixerBuilder.addSchema(701, V701::new);
		dataFixerBuilder.addFixer(new EntitySkeletonSplitFix(schema20, true));
		Schema schema21 = dataFixerBuilder.addSchema(702, V702::new);
		dataFixerBuilder.addFixer(new EntityZombieSplitFix(schema21, true));
		Schema schema22 = dataFixerBuilder.addSchema(703, V703::new);
		dataFixerBuilder.addFixer(new EntityHorseSplitFix(schema22, true));
		Schema schema23 = dataFixerBuilder.addSchema(704, V704::new);
		dataFixerBuilder.addFixer(new BlockEntityIdFix(schema23, true));
		Schema schema24 = dataFixerBuilder.addSchema(705, V705::new);
		dataFixerBuilder.addFixer(new EntityIdFix(schema24, true));
		Schema schema25 = dataFixerBuilder.addSchema(804, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemBannerColorFix(schema25, true));
		Schema schema26 = dataFixerBuilder.addSchema(806, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemWaterPotionFix(schema26, false));
		Schema schema27 = dataFixerBuilder.addSchema(808, V808::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema27, "added shulker box", References.BLOCK_ENTITY));
		Schema schema28 = dataFixerBuilder.addSchema(808, 1, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityShulkerColorFix(schema28, false));
		Schema schema29 = dataFixerBuilder.addSchema(813, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemShulkerBoxColorFix(schema29, false));
		dataFixerBuilder.addFixer(new BlockEntityShulkerBoxColorFix(schema29, false));
		Schema schema30 = dataFixerBuilder.addSchema(816, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsLowerCaseLanguageFix(schema30, false));
		Schema schema31 = dataFixerBuilder.addSchema(820, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema31, "totem item renamer", createRenamer("minecraft:totem", "minecraft:totem_of_undying")));
		Schema schema32 = dataFixerBuilder.addSchema(1022, V1022::new);
		dataFixerBuilder.addFixer(new WriteAndReadFix(schema32, "added shoulder entities to players", References.PLAYER));
		Schema schema33 = dataFixerBuilder.addSchema(1125, V1125::new);
		dataFixerBuilder.addFixer(new ChunkBedBlockEntityInjecterFix(schema33, true));
		dataFixerBuilder.addFixer(new BedItemColorFix(schema33, false));
		Schema schema34 = dataFixerBuilder.addSchema(1344, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsKeyLwjgl3Fix(schema34, false));
		Schema schema35 = dataFixerBuilder.addSchema(1446, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsKeyTranslationFix(schema35, false));
		Schema schema36 = dataFixerBuilder.addSchema(1450, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlockStateStructureTemplateFix(schema36, false));
		Schema schema37 = dataFixerBuilder.addSchema(1451, V1451::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema37, "AddTrappedChestFix", References.BLOCK_ENTITY));
		Schema schema38 = dataFixerBuilder.addSchema(1451, 1, V1451_1::new);
		dataFixerBuilder.addFixer(new ChunkPalettedStorageFix(schema38, true));
		Schema schema39 = dataFixerBuilder.addSchema(1451, 2, V1451_2::new);
		dataFixerBuilder.addFixer(new BlockEntityBlockStateFix(schema39, true));
		Schema schema40 = dataFixerBuilder.addSchema(1451, 3, V1451_3::new);
		dataFixerBuilder.addFixer(new EntityBlockStateFix(schema40, true));
		dataFixerBuilder.addFixer(new ItemStackMapIdFix(schema40, false));
		Schema schema41 = dataFixerBuilder.addSchema(1451, 4, V1451_4::new);
		dataFixerBuilder.addFixer(new BlockNameFlatteningFix(schema41, true));
		dataFixerBuilder.addFixer(new ItemStackTheFlatteningFix(schema41, false));
		Schema schema42 = dataFixerBuilder.addSchema(1451, 5, V1451_5::new);
		dataFixerBuilder.addFixer(
			new ItemRemoveBlockEntityTagFix(
				schema42,
				false,
				Set.of(
					"minecraft:note_block",
					"minecraft:flower_pot",
					"minecraft:dandelion",
					"minecraft:poppy",
					"minecraft:blue_orchid",
					"minecraft:allium",
					"minecraft:azure_bluet",
					"minecraft:red_tulip",
					"minecraft:orange_tulip",
					"minecraft:white_tulip",
					"minecraft:pink_tulip",
					"minecraft:oxeye_daisy",
					"minecraft:cactus",
					"minecraft:brown_mushroom",
					"minecraft:red_mushroom",
					"minecraft:oak_sapling",
					"minecraft:spruce_sapling",
					"minecraft:birch_sapling",
					"minecraft:jungle_sapling",
					"minecraft:acacia_sapling",
					"minecraft:dark_oak_sapling",
					"minecraft:dead_bush",
					"minecraft:fern"
				)
			)
		);
		dataFixerBuilder.addFixer(new AddNewChoices(schema42, "RemoveNoteBlockFlowerPotFix", References.BLOCK_ENTITY));
		dataFixerBuilder.addFixer(new ItemStackSpawnEggFix(schema42, false, "minecraft:spawn_egg"));
		dataFixerBuilder.addFixer(new EntityWolfColorFix(schema42, false));
		dataFixerBuilder.addFixer(new BlockEntityBannerColorFix(schema42, false));
		dataFixerBuilder.addFixer(new LevelFlatGeneratorInfoFix(schema42, false));
		Schema schema43 = dataFixerBuilder.addSchema(1451, 6, V1451_6::new);
		dataFixerBuilder.addFixer(new StatsCounterFix(schema43, true));
		dataFixerBuilder.addFixer(new BlockEntityJukeboxFix(schema43, false));
		Schema schema44 = dataFixerBuilder.addSchema(1451, 7, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerTradeFix(schema44, false));
		Schema schema45 = dataFixerBuilder.addSchema(1456, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityItemFrameDirectionFix(schema45, false));
		Schema schema46 = dataFixerBuilder.addSchema(1458, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityCustomNameToComponentFix(schema46, false));
		dataFixerBuilder.addFixer(new ItemCustomNameToComponentFix(schema46, false));
		dataFixerBuilder.addFixer(new BlockEntityCustomNameToComponentFix(schema46, false));
		Schema schema47 = dataFixerBuilder.addSchema(1460, V1460::new);
		dataFixerBuilder.addFixer(new EntityPaintingMotiveFix(schema47, false));
		Schema schema48 = dataFixerBuilder.addSchema(1466, V1466::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema48, "Add DUMMY block entity", References.BLOCK_ENTITY));
		dataFixerBuilder.addFixer(new ChunkToProtochunkFix(schema48, true));
		Schema schema49 = dataFixerBuilder.addSchema(1470, V1470::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema49, "Add 1.13 entities fix", References.ENTITY));
		Schema schema50 = dataFixerBuilder.addSchema(1474, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ColorlessShulkerEntityFix(schema50, false));
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema50,
				"Colorless shulker block fixer",
				string -> Objects.equals(NamespacedSchema.ensureNamespaced(string), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : string
			)
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema50,
				"Colorless shulker item fixer",
				string -> Objects.equals(NamespacedSchema.ensureNamespaced(string), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : string
			)
		);
		Schema schema51 = dataFixerBuilder.addSchema(1475, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema51, "Flowing fixer", createRenamer(ImmutableMap.of("minecraft:flowing_water", "minecraft:water", "minecraft:flowing_lava", "minecraft:lava"))
			)
		);
		Schema schema52 = dataFixerBuilder.addSchema(1480, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(BlockRenameFix.create(schema52, "Rename coral blocks", createRenamer(RenamedCoralFix.RENAMED_IDS)));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema52, "Rename coral items", createRenamer(RenamedCoralFix.RENAMED_IDS)));
		Schema schema53 = dataFixerBuilder.addSchema(1481, V1481::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema53, "Add conduit", References.BLOCK_ENTITY));
		Schema schema54 = dataFixerBuilder.addSchema(1483, V1483::new);
		dataFixerBuilder.addFixer(new EntityPufferfishRenameFix(schema54, true));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema54, "Rename pufferfish egg item", createRenamer(EntityPufferfishRenameFix.RENAMED_IDS)));
		Schema schema55 = dataFixerBuilder.addSchema(1484, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema55,
				"Rename seagrass items",
				createRenamer(ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass"))
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema55,
				"Rename seagrass blocks",
				createRenamer(ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass"))
			)
		);
		dataFixerBuilder.addFixer(new HeightmapRenamingFix(schema55, false));
		Schema schema56 = dataFixerBuilder.addSchema(1486, V1486::new);
		dataFixerBuilder.addFixer(new EntityCodSalmonFix(schema56, true));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema56, "Rename cod/salmon egg items", createRenamer(EntityCodSalmonFix.RENAMED_EGG_IDS)));
		Schema schema57 = dataFixerBuilder.addSchema(1487, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema57,
				"Rename prismarine_brick(s)_* blocks",
				createRenamer(
					ImmutableMap.of(
						"minecraft:prismarine_bricks_slab", "minecraft:prismarine_brick_slab", "minecraft:prismarine_bricks_stairs", "minecraft:prismarine_brick_stairs"
					)
				)
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema57,
				"Rename prismarine_brick(s)_* items",
				createRenamer(
					ImmutableMap.of(
						"minecraft:prismarine_bricks_slab", "minecraft:prismarine_brick_slab", "minecraft:prismarine_bricks_stairs", "minecraft:prismarine_brick_stairs"
					)
				)
			)
		);
		Schema schema58 = dataFixerBuilder.addSchema(1488, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema58, "Rename kelp/kelptop", createRenamer(ImmutableMap.of("minecraft:kelp_top", "minecraft:kelp", "minecraft:kelp", "minecraft:kelp_plant"))
			)
		);
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema58, "Rename kelptop", createRenamer("minecraft:kelp_top", "minecraft:kelp")));
		dataFixerBuilder.addFixer(
			new NamedEntityFix(schema58, false, "Command block block entity custom name fix", References.BLOCK_ENTITY, "minecraft:command_block") {
				@Override
				protected Typed<?> fix(Typed<?> typed) {
					return typed.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName);
				}
			}
		);
		dataFixerBuilder.addFixer(
			new NamedEntityFix(schema58, false, "Command block minecart custom name fix", References.ENTITY, "minecraft:commandblock_minecart") {
				@Override
				protected Typed<?> fix(Typed<?> typed) {
					return typed.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName);
				}
			}
		);
		dataFixerBuilder.addFixer(new IglooMetadataRemovalFix(schema58, false));
		Schema schema59 = dataFixerBuilder.addSchema(1490, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(BlockRenameFix.create(schema59, "Rename melon_block", createRenamer("minecraft:melon_block", "minecraft:melon")));
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema59,
				"Rename melon_block/melon/speckled_melon",
				createRenamer(
					ImmutableMap.of(
						"minecraft:melon_block", "minecraft:melon", "minecraft:melon", "minecraft:melon_slice", "minecraft:speckled_melon", "minecraft:glistering_melon_slice"
					)
				)
			)
		);
		Schema schema60 = dataFixerBuilder.addSchema(1492, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkStructuresTemplateRenameFix(schema60, false));
		Schema schema61 = dataFixerBuilder.addSchema(1494, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemStackEnchantmentNamesFix(schema61, false));
		Schema schema62 = dataFixerBuilder.addSchema(1496, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new LeavesFix(schema62, false));
		Schema schema63 = dataFixerBuilder.addSchema(1500, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlockEntityKeepPacked(schema63, false));
		Schema schema64 = dataFixerBuilder.addSchema(1501, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new AdvancementsFix(schema64, false));
		Schema schema65 = dataFixerBuilder.addSchema(1502, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new NamespacedTypeRenameFix(schema65, "Recipes fix", References.RECIPE, createRenamer(RecipesFix.RECIPES)));
		Schema schema66 = dataFixerBuilder.addSchema(1506, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new LevelDataGeneratorOptionsFix(schema66, false));
		Schema schema67 = dataFixerBuilder.addSchema(1510, V1510::new);
		dataFixerBuilder.addFixer(BlockRenameFix.create(schema67, "Block renamening fix", createRenamer(EntityTheRenameningFix.RENAMED_BLOCKS)));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema67, "Item renamening fix", createRenamer(EntityTheRenameningFix.RENAMED_ITEMS)));
		dataFixerBuilder.addFixer(new NamespacedTypeRenameFix(schema67, "Recipes renamening fix", References.RECIPE, createRenamer(RecipesRenameningFix.RECIPES)));
		dataFixerBuilder.addFixer(new EntityTheRenameningFix(schema67, true));
		dataFixerBuilder.addFixer(
			new StatsRenameFix(
				schema67,
				"SwimStatsRenameFix",
				ImmutableMap.of("minecraft:swim_one_cm", "minecraft:walk_on_water_one_cm", "minecraft:dive_one_cm", "minecraft:walk_under_water_one_cm")
			)
		);
		Schema schema68 = dataFixerBuilder.addSchema(1514, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ObjectiveDisplayNameFix(schema68, false));
		dataFixerBuilder.addFixer(new TeamDisplayNameFix(schema68, false));
		dataFixerBuilder.addFixer(new ObjectiveRenderTypeFix(schema68, false));
		Schema schema69 = dataFixerBuilder.addSchema(1515, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(BlockRenameFix.create(schema69, "Rename coral fan blocks", createRenamer(RenamedCoralFansFix.RENAMED_IDS)));
		Schema schema70 = dataFixerBuilder.addSchema(1624, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new TrappedChestBlockEntityFix(schema70, false));
		Schema schema71 = dataFixerBuilder.addSchema(1800, V1800::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema71, "Added 1.14 mobs fix", References.ENTITY));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema71, "Rename dye items", createRenamer(DyeItemRenameFix.RENAMED_IDS)));
		Schema schema72 = dataFixerBuilder.addSchema(1801, V1801::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema72, "Added Illager Beast", References.ENTITY));
		Schema schema73 = dataFixerBuilder.addSchema(1802, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema73,
				"Rename sign blocks & stone slabs",
				createRenamer(
					ImmutableMap.of(
						"minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign", "minecraft:wall_sign", "minecraft:oak_wall_sign"
					)
				)
			)
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema73,
				"Rename sign item & stone slabs",
				createRenamer(ImmutableMap.of("minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign"))
			)
		);
		Schema schema74 = dataFixerBuilder.addSchema(1803, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemLoreFix(schema74, false));
		Schema schema75 = dataFixerBuilder.addSchema(1904, V1904::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema75, "Added Cats", References.ENTITY));
		dataFixerBuilder.addFixer(new EntityCatSplitFix(schema75, false));
		Schema schema76 = dataFixerBuilder.addSchema(1905, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkStatusFix(schema76, false));
		Schema schema77 = dataFixerBuilder.addSchema(1906, V1906::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema77, "Add POI Blocks", References.BLOCK_ENTITY));
		Schema schema78 = dataFixerBuilder.addSchema(1909, V1909::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema78, "Add jigsaw", References.BLOCK_ENTITY));
		Schema schema79 = dataFixerBuilder.addSchema(1911, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkStatusFix2(schema79, false));
		Schema schema80 = dataFixerBuilder.addSchema(1914, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new WeaponSmithChestLootTableFix(schema80, false));
		Schema schema81 = dataFixerBuilder.addSchema(1917, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new CatTypeFix(schema81, false));
		Schema schema82 = dataFixerBuilder.addSchema(1918, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerDataFix(schema82, "minecraft:villager"));
		dataFixerBuilder.addFixer(new VillagerDataFix(schema82, "minecraft:zombie_villager"));
		Schema schema83 = dataFixerBuilder.addSchema(1920, V1920::new);
		dataFixerBuilder.addFixer(new NewVillageFix(schema83, false));
		dataFixerBuilder.addFixer(new AddNewChoices(schema83, "Add campfire", References.BLOCK_ENTITY));
		Schema schema84 = dataFixerBuilder.addSchema(1925, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new MapIdFix(schema84, false));
		Schema schema85 = dataFixerBuilder.addSchema(1928, V1928::new);
		dataFixerBuilder.addFixer(new EntityRavagerRenameFix(schema85, true));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema85, "Rename ravager egg item", createRenamer(EntityRavagerRenameFix.RENAMED_IDS)));
		Schema schema86 = dataFixerBuilder.addSchema(1929, V1929::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema86, "Add Wandering Trader and Trader Llama", References.ENTITY));
		Schema schema87 = dataFixerBuilder.addSchema(1931, V1931::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema87, "Added Fox", References.ENTITY));
		Schema schema88 = dataFixerBuilder.addSchema(1936, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsAddTextBackgroundFix(schema88, false));
		Schema schema89 = dataFixerBuilder.addSchema(1946, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ReorganizePoi(schema89, false));
		Schema schema90 = dataFixerBuilder.addSchema(1948, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OminousBannerRenameFix(schema90));
		Schema schema91 = dataFixerBuilder.addSchema(1953, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OminousBannerBlockEntityRenameFix(schema91, false));
		Schema schema92 = dataFixerBuilder.addSchema(1955, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerRebuildLevelAndXpFix(schema92, false));
		dataFixerBuilder.addFixer(new ZombieVillagerRebuildXpFix(schema92, false));
		Schema schema93 = dataFixerBuilder.addSchema(1961, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkLightRemoveFix(schema93, false));
		Schema schema94 = dataFixerBuilder.addSchema(1963, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new RemoveGolemGossipFix(schema94, false));
		Schema schema95 = dataFixerBuilder.addSchema(2100, V2100::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema95, "Added Bee and Bee Stinger", References.ENTITY));
		dataFixerBuilder.addFixer(new AddNewChoices(schema95, "Add beehive", References.BLOCK_ENTITY));
		dataFixerBuilder.addFixer(
			new NamespacedTypeRenameFix(schema95, "Rename sugar recipe", References.RECIPE, createRenamer("minecraft:sugar", "sugar_from_sugar_cane"))
		);
		dataFixerBuilder.addFixer(
			new AdvancementsRenameFix(
				schema95, false, "Rename sugar recipe advancement", createRenamer("minecraft:recipes/misc/sugar", "minecraft:recipes/misc/sugar_from_sugar_cane")
			)
		);
		Schema schema96 = dataFixerBuilder.addSchema(2202, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkBiomeFix(schema96, false));
		Schema schema97 = dataFixerBuilder.addSchema(2209, SAME_NAMESPACED);
		UnaryOperator<String> unaryOperator = createRenamer("minecraft:bee_hive", "minecraft:beehive");
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema97, "Rename bee_hive item to beehive", unaryOperator));
		dataFixerBuilder.addFixer(new PoiTypeRenameFix(schema97, "Rename bee_hive poi to beehive", unaryOperator));
		dataFixerBuilder.addFixer(BlockRenameFix.create(schema97, "Rename bee_hive block to beehive", unaryOperator));
		Schema schema98 = dataFixerBuilder.addSchema(2211, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new StructureReferenceCountFix(schema98, false));
		Schema schema99 = dataFixerBuilder.addSchema(2218, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ForcePoiRebuild(schema99, false));
		Schema schema100 = dataFixerBuilder.addSchema(2501, V2501::new);
		dataFixerBuilder.addFixer(new FurnaceRecipeFix(schema100, true));
		Schema schema101 = dataFixerBuilder.addSchema(2502, V2502::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema101, "Added Hoglin", References.ENTITY));
		Schema schema102 = dataFixerBuilder.addSchema(2503, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new WallPropertyFix(schema102, false));
		dataFixerBuilder.addFixer(
			new AdvancementsRenameFix(
				schema102, false, "Composter category change", createRenamer("minecraft:recipes/misc/composter", "minecraft:recipes/decorations/composter")
			)
		);
		Schema schema103 = dataFixerBuilder.addSchema(2505, V2505::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema103, "Added Piglin", References.ENTITY));
		dataFixerBuilder.addFixer(new MemoryExpiryDataFix(schema103, "minecraft:villager"));
		Schema schema104 = dataFixerBuilder.addSchema(2508, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema104,
				"Renamed fungi items to fungus",
				createRenamer(ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus"))
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema104,
				"Renamed fungi blocks to fungus",
				createRenamer(ImmutableMap.of("minecraft:warped_fungi", "minecraft:warped_fungus", "minecraft:crimson_fungi", "minecraft:crimson_fungus"))
			)
		);
		Schema schema105 = dataFixerBuilder.addSchema(2509, V2509::new);
		dataFixerBuilder.addFixer(new EntityZombifiedPiglinRenameFix(schema105));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema105, "Rename zombie pigman egg item", createRenamer(EntityZombifiedPiglinRenameFix.RENAMED_IDS)));
		Schema schema106 = dataFixerBuilder.addSchema(2511, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityProjectileOwnerFix(schema106));
		Schema schema107 = dataFixerBuilder.addSchema(2514, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityUUIDFix(schema107));
		dataFixerBuilder.addFixer(new BlockEntityUUIDFix(schema107));
		dataFixerBuilder.addFixer(new PlayerUUIDFix(schema107));
		dataFixerBuilder.addFixer(new LevelUUIDFix(schema107));
		dataFixerBuilder.addFixer(new SavedDataUUIDFix(schema107));
		dataFixerBuilder.addFixer(new ItemStackUUIDFix(schema107));
		Schema schema108 = dataFixerBuilder.addSchema(2516, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new GossipUUIDFix(schema108, "minecraft:villager"));
		dataFixerBuilder.addFixer(new GossipUUIDFix(schema108, "minecraft:zombie_villager"));
		Schema schema109 = dataFixerBuilder.addSchema(2518, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new JigsawPropertiesFix(schema109, false));
		dataFixerBuilder.addFixer(new JigsawRotationFix(schema109, false));
		Schema schema110 = dataFixerBuilder.addSchema(2519, V2519::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema110, "Added Strider", References.ENTITY));
		Schema schema111 = dataFixerBuilder.addSchema(2522, V2522::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema111, "Added Zoglin", References.ENTITY));
		Schema schema112 = dataFixerBuilder.addSchema(2523, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new AttributesRename(schema112));
		Schema schema113 = dataFixerBuilder.addSchema(2527, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BitStorageAlignFix(schema113));
		Schema schema114 = dataFixerBuilder.addSchema(2528, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema114,
				"Rename soul fire torch and soul fire lantern",
				createRenamer(ImmutableMap.of("minecraft:soul_fire_torch", "minecraft:soul_torch", "minecraft:soul_fire_lantern", "minecraft:soul_lantern"))
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema114,
				"Rename soul fire torch and soul fire lantern",
				createRenamer(
					ImmutableMap.of(
						"minecraft:soul_fire_torch",
						"minecraft:soul_torch",
						"minecraft:soul_fire_wall_torch",
						"minecraft:soul_wall_torch",
						"minecraft:soul_fire_lantern",
						"minecraft:soul_lantern"
					)
				)
			)
		);
		Schema schema115 = dataFixerBuilder.addSchema(2529, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new StriderGravityFix(schema115, false));
		Schema schema116 = dataFixerBuilder.addSchema(2531, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new RedstoneWireConnectionsFix(schema116));
		Schema schema117 = dataFixerBuilder.addSchema(2533, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerFollowRangeFix(schema117));
		Schema schema118 = dataFixerBuilder.addSchema(2535, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityShulkerRotationFix(schema118));
		Schema schema119 = dataFixerBuilder.addSchema(2538, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new LevelLegacyWorldGenSettingsFix(schema119));
		Schema schema120 = dataFixerBuilder.addSchema(2550, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new WorldGenSettingsFix(schema120));
		Schema schema121 = dataFixerBuilder.addSchema(2551, V2551::new);
		dataFixerBuilder.addFixer(new WriteAndReadFix(schema121, "add types to WorldGenData", References.WORLD_GEN_SETTINGS));
		Schema schema122 = dataFixerBuilder.addSchema(2552, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new NamespacedTypeRenameFix(schema122, "Nether biome rename", References.BIOME, createRenamer("minecraft:nether", "minecraft:nether_wastes"))
		);
		Schema schema123 = dataFixerBuilder.addSchema(2553, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new NamespacedTypeRenameFix(schema123, "Biomes fix", References.BIOME, createRenamer(BiomeFix.BIOMES)));
		Schema schema124 = dataFixerBuilder.addSchema(2558, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new MissingDimensionFix(schema124, false));
		dataFixerBuilder.addFixer(new OptionsRenameFieldFix(schema124, false, "Rename swapHands setting", "key_key.swapHands", "key_key.swapOffhand"));
		Schema schema125 = dataFixerBuilder.addSchema(2568, V2568::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema125, "Added Piglin Brute", References.ENTITY));
		Schema schema126 = dataFixerBuilder.addSchema(2571, V2571::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema126, "Added Goat", References.ENTITY));
		Schema schema127 = dataFixerBuilder.addSchema(2679, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new CauldronRenameFix(schema127, false));
		Schema schema128 = dataFixerBuilder.addSchema(2680, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema128, "Renamed grass path item to dirt path", createRenamer("minecraft:grass_path", "minecraft:dirt_path"))
		);
		dataFixerBuilder.addFixer(
			BlockRenameFixWithJigsaw.create(schema128, "Renamed grass path block to dirt path", createRenamer("minecraft:grass_path", "minecraft:dirt_path"))
		);
		Schema schema129 = dataFixerBuilder.addSchema(2684, V2684::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema129, "Added Sculk Sensor", References.BLOCK_ENTITY));
		Schema schema130 = dataFixerBuilder.addSchema(2686, V2686::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema130, "Added Axolotl", References.ENTITY));
		Schema schema131 = dataFixerBuilder.addSchema(2688, V2688::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema131, "Added Glow Squid", References.ENTITY));
		dataFixerBuilder.addFixer(new AddNewChoices(schema131, "Added Glow Item Frame", References.ENTITY));
		Schema schema132 = dataFixerBuilder.addSchema(2690, SAME_NAMESPACED);
		ImmutableMap<String, String> immutableMap = ImmutableMap.<String, String>builder()
			.put("minecraft:weathered_copper_block", "minecraft:oxidized_copper_block")
			.put("minecraft:semi_weathered_copper_block", "minecraft:weathered_copper_block")
			.put("minecraft:lightly_weathered_copper_block", "minecraft:exposed_copper_block")
			.put("minecraft:weathered_cut_copper", "minecraft:oxidized_cut_copper")
			.put("minecraft:semi_weathered_cut_copper", "minecraft:weathered_cut_copper")
			.put("minecraft:lightly_weathered_cut_copper", "minecraft:exposed_cut_copper")
			.put("minecraft:weathered_cut_copper_stairs", "minecraft:oxidized_cut_copper_stairs")
			.put("minecraft:semi_weathered_cut_copper_stairs", "minecraft:weathered_cut_copper_stairs")
			.put("minecraft:lightly_weathered_cut_copper_stairs", "minecraft:exposed_cut_copper_stairs")
			.put("minecraft:weathered_cut_copper_slab", "minecraft:oxidized_cut_copper_slab")
			.put("minecraft:semi_weathered_cut_copper_slab", "minecraft:weathered_cut_copper_slab")
			.put("minecraft:lightly_weathered_cut_copper_slab", "minecraft:exposed_cut_copper_slab")
			.put("minecraft:waxed_semi_weathered_copper", "minecraft:waxed_weathered_copper")
			.put("minecraft:waxed_lightly_weathered_copper", "minecraft:waxed_exposed_copper")
			.put("minecraft:waxed_semi_weathered_cut_copper", "minecraft:waxed_weathered_cut_copper")
			.put("minecraft:waxed_lightly_weathered_cut_copper", "minecraft:waxed_exposed_cut_copper")
			.put("minecraft:waxed_semi_weathered_cut_copper_stairs", "minecraft:waxed_weathered_cut_copper_stairs")
			.put("minecraft:waxed_lightly_weathered_cut_copper_stairs", "minecraft:waxed_exposed_cut_copper_stairs")
			.put("minecraft:waxed_semi_weathered_cut_copper_slab", "minecraft:waxed_weathered_cut_copper_slab")
			.put("minecraft:waxed_lightly_weathered_cut_copper_slab", "minecraft:waxed_exposed_cut_copper_slab")
			.build();
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema132, "Renamed copper block items to new oxidized terms", createRenamer(immutableMap)));
		dataFixerBuilder.addFixer(BlockRenameFixWithJigsaw.create(schema132, "Renamed copper blocks to new oxidized terms", createRenamer(immutableMap)));
		Schema schema133 = dataFixerBuilder.addSchema(2691, SAME_NAMESPACED);
		ImmutableMap<String, String> immutableMap2 = ImmutableMap.<String, String>builder()
			.put("minecraft:waxed_copper", "minecraft:waxed_copper_block")
			.put("minecraft:oxidized_copper_block", "minecraft:oxidized_copper")
			.put("minecraft:weathered_copper_block", "minecraft:weathered_copper")
			.put("minecraft:exposed_copper_block", "minecraft:exposed_copper")
			.build();
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema133, "Rename copper item suffixes", createRenamer(immutableMap2)));
		dataFixerBuilder.addFixer(BlockRenameFixWithJigsaw.create(schema133, "Rename copper blocks suffixes", createRenamer(immutableMap2)));
		Schema schema134 = dataFixerBuilder.addSchema(2693, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new AddFlagIfNotPresentFix(schema134, References.WORLD_GEN_SETTINGS, "has_increased_height_already", false));
		Schema schema135 = dataFixerBuilder.addSchema(2696, SAME_NAMESPACED);
		ImmutableMap<String, String> immutableMap3 = ImmutableMap.<String, String>builder()
			.put("minecraft:grimstone", "minecraft:deepslate")
			.put("minecraft:grimstone_slab", "minecraft:cobbled_deepslate_slab")
			.put("minecraft:grimstone_stairs", "minecraft:cobbled_deepslate_stairs")
			.put("minecraft:grimstone_wall", "minecraft:cobbled_deepslate_wall")
			.put("minecraft:polished_grimstone", "minecraft:polished_deepslate")
			.put("minecraft:polished_grimstone_slab", "minecraft:polished_deepslate_slab")
			.put("minecraft:polished_grimstone_stairs", "minecraft:polished_deepslate_stairs")
			.put("minecraft:polished_grimstone_wall", "minecraft:polished_deepslate_wall")
			.put("minecraft:grimstone_tiles", "minecraft:deepslate_tiles")
			.put("minecraft:grimstone_tile_slab", "minecraft:deepslate_tile_slab")
			.put("minecraft:grimstone_tile_stairs", "minecraft:deepslate_tile_stairs")
			.put("minecraft:grimstone_tile_wall", "minecraft:deepslate_tile_wall")
			.put("minecraft:grimstone_bricks", "minecraft:deepslate_bricks")
			.put("minecraft:grimstone_brick_slab", "minecraft:deepslate_brick_slab")
			.put("minecraft:grimstone_brick_stairs", "minecraft:deepslate_brick_stairs")
			.put("minecraft:grimstone_brick_wall", "minecraft:deepslate_brick_wall")
			.put("minecraft:chiseled_grimstone", "minecraft:chiseled_deepslate")
			.build();
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema135, "Renamed grimstone block items to deepslate", createRenamer(immutableMap3)));
		dataFixerBuilder.addFixer(BlockRenameFixWithJigsaw.create(schema135, "Renamed grimstone blocks to deepslate", createRenamer(immutableMap3)));
		Schema schema136 = dataFixerBuilder.addSchema(2700, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFixWithJigsaw.create(
				schema136,
				"Renamed cave vines blocks",
				createRenamer(ImmutableMap.of("minecraft:cave_vines_head", "minecraft:cave_vines", "minecraft:cave_vines_body", "minecraft:cave_vines_plant"))
			)
		);
		Schema schema137 = dataFixerBuilder.addSchema(2701, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new SavedDataFeaturePoolElementFix(schema137));
		Schema schema138 = dataFixerBuilder.addSchema(2702, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new AbstractArrowPickupFix(schema138));
		Schema schema139 = dataFixerBuilder.addSchema(2704, V2704::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema139, "Added Goat", References.ENTITY));
		Schema schema140 = dataFixerBuilder.addSchema(2707, V2707::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema140, "Added Marker", References.ENTITY));
		dataFixerBuilder.addFixer(new AddFlagIfNotPresentFix(schema140, References.WORLD_GEN_SETTINGS, "has_increased_height_already", true));
		Schema schema141 = dataFixerBuilder.addSchema(2710, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new StatsRenameFix(schema141, "Renamed play_one_minute stat to play_time", ImmutableMap.of("minecraft:play_one_minute", "minecraft:play_time"))
		);
		Schema schema142 = dataFixerBuilder.addSchema(2717, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema142, "Rename azalea_leaves_flowers", createRenamer(ImmutableMap.of("minecraft:azalea_leaves_flowers", "minecraft:flowering_azalea_leaves"))
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema142, "Rename azalea_leaves_flowers items", createRenamer(ImmutableMap.of("minecraft:azalea_leaves_flowers", "minecraft:flowering_azalea_leaves"))
			)
		);
		Schema schema143 = dataFixerBuilder.addSchema(2825, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new AddFlagIfNotPresentFix(schema143, References.WORLD_GEN_SETTINGS, "has_increased_height_already", false));
		Schema schema144 = dataFixerBuilder.addSchema(2831, V2831::new);
		dataFixerBuilder.addFixer(new SpawnerDataFix(schema144));
		Schema schema145 = dataFixerBuilder.addSchema(2832, V2832::new);
		dataFixerBuilder.addFixer(new WorldGenSettingsHeightAndBiomeFix(schema145));
		dataFixerBuilder.addFixer(new ChunkHeightAndBiomeFix(schema145));
		Schema schema146 = dataFixerBuilder.addSchema(2833, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new WorldGenSettingsDisallowOldCustomWorldsFix(schema146));
		Schema schema147 = dataFixerBuilder.addSchema(2838, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new NamespacedTypeRenameFix(schema147, "Caves and Cliffs biome renames", References.BIOME, createRenamer(CavesAndCliffsRenames.RENAMES))
		);
		Schema schema148 = dataFixerBuilder.addSchema(2841, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkProtoTickListFix(schema148));
		Schema schema149 = dataFixerBuilder.addSchema(2842, V2842::new);
		dataFixerBuilder.addFixer(new ChunkRenamesFix(schema149));
		Schema schema150 = dataFixerBuilder.addSchema(2843, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OverreachingTickFix(schema150));
		dataFixerBuilder.addFixer(
			new NamespacedTypeRenameFix(schema150, "Remove Deep Warm Ocean", References.BIOME, createRenamer("minecraft:deep_warm_ocean", "minecraft:warm_ocean"))
		);
		Schema schema151 = dataFixerBuilder.addSchema(2846, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new AdvancementsRenameFix(
				schema151,
				false,
				"Rename some C&C part 2 advancements",
				createRenamer(
					ImmutableMap.of(
						"minecraft:husbandry/play_jukebox_in_meadows",
						"minecraft:adventure/play_jukebox_in_meadows",
						"minecraft:adventure/caves_and_cliff",
						"minecraft:adventure/fall_from_world_height",
						"minecraft:adventure/ride_strider_in_overworld_lava",
						"minecraft:nether/ride_strider_in_overworld_lava"
					)
				)
			)
		);
		Schema schema152 = dataFixerBuilder.addSchema(2852, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new WorldGenSettingsDisallowOldCustomWorldsFix(schema152));
		Schema schema153 = dataFixerBuilder.addSchema(2967, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new StructureSettingsFlattenFix(schema153));
		Schema schema154 = dataFixerBuilder.addSchema(2970, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new StructuresBecomeConfiguredFix(schema154));
		Schema schema155 = dataFixerBuilder.addSchema(3076, V3076::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema155, "Added Sculk Catalyst", References.BLOCK_ENTITY));
		Schema schema156 = dataFixerBuilder.addSchema(3077, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkDeleteIgnoredLightDataFix(schema156));
		Schema schema157 = dataFixerBuilder.addSchema(3078, V3078::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema157, "Added Frog", References.ENTITY));
		dataFixerBuilder.addFixer(new AddNewChoices(schema157, "Added Tadpole", References.ENTITY));
		dataFixerBuilder.addFixer(new AddNewChoices(schema157, "Added Sculk Shrieker", References.BLOCK_ENTITY));
		Schema schema158 = dataFixerBuilder.addSchema(3081, V3081::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema158, "Added Warden", References.ENTITY));
		Schema schema159 = dataFixerBuilder.addSchema(3082, V3082::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema159, "Added Chest Boat", References.ENTITY));
		Schema schema160 = dataFixerBuilder.addSchema(3083, V3083::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema160, "Added Allay", References.ENTITY));
		Schema schema161 = dataFixerBuilder.addSchema(3084, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new NamespacedTypeRenameFix(
				schema161,
				"game_event_renames_3084",
				References.GAME_EVENT_NAME,
				createRenamer(
					ImmutableMap.<String, String>builder()
						.put("minecraft:block_press", "minecraft:block_activate")
						.put("minecraft:block_switch", "minecraft:block_activate")
						.put("minecraft:block_unpress", "minecraft:block_deactivate")
						.put("minecraft:block_unswitch", "minecraft:block_deactivate")
						.put("minecraft:drinking_finish", "minecraft:drink")
						.put("minecraft:elytra_free_fall", "minecraft:elytra_glide")
						.put("minecraft:entity_damaged", "minecraft:entity_damage")
						.put("minecraft:entity_dying", "minecraft:entity_die")
						.put("minecraft:entity_killed", "minecraft:entity_die")
						.put("minecraft:mob_interact", "minecraft:entity_interact")
						.put("minecraft:ravager_roar", "minecraft:entity_roar")
						.put("minecraft:ring_bell", "minecraft:block_change")
						.put("minecraft:shulker_close", "minecraft:container_close")
						.put("minecraft:shulker_open", "minecraft:container_open")
						.put("minecraft:wolf_shaking", "minecraft:entity_shake")
						.build()
				)
			)
		);
		Schema schema162 = dataFixerBuilder.addSchema(3086, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new EntityVariantFix(
				schema162, "Change cat variant type", References.ENTITY, "minecraft:cat", "CatType", Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
					int2ObjectOpenHashMap.defaultReturnValue("minecraft:tabby");
					int2ObjectOpenHashMap.put(0, "minecraft:tabby");
					int2ObjectOpenHashMap.put(1, "minecraft:black");
					int2ObjectOpenHashMap.put(2, "minecraft:red");
					int2ObjectOpenHashMap.put(3, "minecraft:siamese");
					int2ObjectOpenHashMap.put(4, "minecraft:british");
					int2ObjectOpenHashMap.put(5, "minecraft:calico");
					int2ObjectOpenHashMap.put(6, "minecraft:persian");
					int2ObjectOpenHashMap.put(7, "minecraft:ragdoll");
					int2ObjectOpenHashMap.put(8, "minecraft:white");
					int2ObjectOpenHashMap.put(9, "minecraft:jellie");
					int2ObjectOpenHashMap.put(10, "minecraft:all_black");
				})::get
			)
		);
		ImmutableMap<String, String> immutableMap4 = ImmutableMap.<String, String>builder()
			.put("textures/entity/cat/tabby.png", "minecraft:tabby")
			.put("textures/entity/cat/black.png", "minecraft:black")
			.put("textures/entity/cat/red.png", "minecraft:red")
			.put("textures/entity/cat/siamese.png", "minecraft:siamese")
			.put("textures/entity/cat/british_shorthair.png", "minecraft:british")
			.put("textures/entity/cat/calico.png", "minecraft:calico")
			.put("textures/entity/cat/persian.png", "minecraft:persian")
			.put("textures/entity/cat/ragdoll.png", "minecraft:ragdoll")
			.put("textures/entity/cat/white.png", "minecraft:white")
			.put("textures/entity/cat/jellie.png", "minecraft:jellie")
			.put("textures/entity/cat/all_black.png", "minecraft:all_black")
			.build();
		dataFixerBuilder.addFixer(
			new CriteriaRenameFix(
				schema162, "Migrate cat variant advancement", "minecraft:husbandry/complete_catalogue", string -> immutableMap4.getOrDefault(string, string)
			)
		);
		Schema schema163 = dataFixerBuilder.addSchema(3087, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new EntityVariantFix(
				schema163, "Change frog variant type", References.ENTITY, "minecraft:frog", "Variant", Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
					int2ObjectOpenHashMap.put(0, "minecraft:temperate");
					int2ObjectOpenHashMap.put(1, "minecraft:warm");
					int2ObjectOpenHashMap.put(2, "minecraft:cold");
				})::get
			)
		);
		Schema schema164 = dataFixerBuilder.addSchema(3090, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityPaintingFieldsRenameFix(schema164));
		Schema schema165 = dataFixerBuilder.addSchema(3093, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityGoatMissingStateFix(schema165));
		Schema schema166 = dataFixerBuilder.addSchema(3094, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new GoatHornIdFix(schema166));
		Schema schema167 = dataFixerBuilder.addSchema(3097, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new FilteredBooksFix(schema167));
		dataFixerBuilder.addFixer(new FilteredSignsFix(schema167));
		Map<String, String> map = Map.of("minecraft:british", "minecraft:british_shorthair");
		dataFixerBuilder.addFixer(new VariantRenameFix(schema167, "Rename british shorthair", References.ENTITY, "minecraft:cat", map));
		dataFixerBuilder.addFixer(
			new CriteriaRenameFix(
				schema167,
				"Migrate cat variant advancement for british shorthair",
				"minecraft:husbandry/complete_catalogue",
				string -> (String)map.getOrDefault(string, string)
			)
		);
		dataFixerBuilder.addFixer(
			new PoiTypeRemoveFix(schema167, "Remove unpopulated villager PoI types", Set.of("minecraft:unemployed", "minecraft:nitwit")::contains)
		);
		Schema schema168 = dataFixerBuilder.addSchema(3108, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlendingDataRemoveFromNetherEndFix(schema168));
		Schema schema169 = dataFixerBuilder.addSchema(3201, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsProgrammerArtFix(schema169));
		Schema schema170 = dataFixerBuilder.addSchema(3202, V3202::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema170, "Added Hanging Sign", References.BLOCK_ENTITY));
		Schema schema171 = dataFixerBuilder.addSchema(3203, V3203::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema171, "Added Camel", References.ENTITY));
		Schema schema172 = dataFixerBuilder.addSchema(3204, V3204::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema172, "Added Chiseled Bookshelf", References.BLOCK_ENTITY));
		Schema schema173 = dataFixerBuilder.addSchema(3209, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemStackSpawnEggFix(schema173, false, "minecraft:pig_spawn_egg"));
		Schema schema174 = dataFixerBuilder.addSchema(3214, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsAmbientOcclusionFix(schema174));
		Schema schema175 = dataFixerBuilder.addSchema(3319, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsAccessibilityOnboardFix(schema175));
		Schema schema176 = dataFixerBuilder.addSchema(3322, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EffectDurationFix(schema176));
		Schema schema177 = dataFixerBuilder.addSchema(3325, V3325::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema177, "Added displays", References.ENTITY));
		Schema schema178 = dataFixerBuilder.addSchema(3326, V3326::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema178, "Added Sniffer", References.ENTITY));
		Schema schema179 = dataFixerBuilder.addSchema(3327, V3327::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema179, "Archaeology", References.BLOCK_ENTITY));
		Schema schema180 = dataFixerBuilder.addSchema(3328, V3328::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema180, "Added interaction", References.ENTITY));
		Schema schema181 = dataFixerBuilder.addSchema(3438, V3438::new);
		dataFixerBuilder.addFixer(
			BlockEntityRenameFix.create(schema181, "Rename Suspicious Sand to Brushable Block", createRenamer("minecraft:suspicious_sand", "minecraft:brushable_block"))
		);
		dataFixerBuilder.addFixer(new EntityBrushableBlockFieldsRenameFix(schema181));
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema181,
				"Pottery shard renaming",
				createRenamer(
					ImmutableMap.of(
						"minecraft:pottery_shard_archer",
						"minecraft:archer_pottery_shard",
						"minecraft:pottery_shard_prize",
						"minecraft:prize_pottery_shard",
						"minecraft:pottery_shard_arms_up",
						"minecraft:arms_up_pottery_shard",
						"minecraft:pottery_shard_skull",
						"minecraft:skull_pottery_shard"
					)
				)
			)
		);
		dataFixerBuilder.addFixer(new AddNewChoices(schema181, "Added calibrated sculk sensor", References.BLOCK_ENTITY));
		Schema schema182 = dataFixerBuilder.addSchema(3439, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlockEntitySignDoubleSidedEditableTextFix(schema182, "Updated sign text format for Signs", "minecraft:sign"));
		dataFixerBuilder.addFixer(new BlockEntitySignDoubleSidedEditableTextFix(schema182, "Updated sign text format for Hanging Signs", "minecraft:hanging_sign"));
		Schema schema183 = dataFixerBuilder.addSchema(3440, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new NamespacedTypeRenameFix(
				schema183,
				"Replace experimental 1.20 overworld",
				References.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
				createRenamer("minecraft:overworld_update_1_20", "minecraft:overworld")
			)
		);
		dataFixerBuilder.addFixer(new FeatureFlagRemoveFix(schema183, "Remove 1.20 feature toggle", Set.of("minecraft:update_1_20")));
		Schema schema184 = dataFixerBuilder.addSchema(3441, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlendingDataFix(schema184));
		Schema schema185 = dataFixerBuilder.addSchema(3447, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema185,
				"Pottery shard item renaming to Pottery sherd",
				createRenamer(
					(Map<String, String>)Stream.of(
							"minecraft:angler_pottery_shard",
							"minecraft:archer_pottery_shard",
							"minecraft:arms_up_pottery_shard",
							"minecraft:blade_pottery_shard",
							"minecraft:brewer_pottery_shard",
							"minecraft:burn_pottery_shard",
							"minecraft:danger_pottery_shard",
							"minecraft:explorer_pottery_shard",
							"minecraft:friend_pottery_shard",
							"minecraft:heart_pottery_shard",
							"minecraft:heartbreak_pottery_shard",
							"minecraft:howl_pottery_shard",
							"minecraft:miner_pottery_shard",
							"minecraft:mourner_pottery_shard",
							"minecraft:plenty_pottery_shard",
							"minecraft:prize_pottery_shard",
							"minecraft:sheaf_pottery_shard",
							"minecraft:shelter_pottery_shard",
							"minecraft:skull_pottery_shard",
							"minecraft:snort_pottery_shard"
						)
						.collect(Collectors.toMap(Function.identity(), string -> string.replace("_pottery_shard", "_pottery_sherd")))
				)
			)
		);
		Schema schema186 = dataFixerBuilder.addSchema(3448, V3448::new);
		dataFixerBuilder.addFixer(new DecoratedPotFieldRenameFix(schema186));
		Schema schema187 = dataFixerBuilder.addSchema(3450, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			new RemapChunkStatusFix(
				schema187,
				"Remove liquid_carvers and heightmap chunk statuses",
				createRenamer(Map.of("minecraft:liquid_carvers", "minecraft:carvers", "minecraft:heightmaps", "minecraft:spawn"))
			)
		);
		Schema schema188 = dataFixerBuilder.addSchema(3451, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkDeleteLightFix(schema188));
		Schema schema189 = dataFixerBuilder.addSchema(3459, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new LegacyDragonFightFix(schema189));
		Schema schema190 = dataFixerBuilder.addSchema(3564, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new DropInvalidSignDataFix(schema190, "Drop invalid sign datafix data", "minecraft:sign"));
		dataFixerBuilder.addFixer(new DropInvalidSignDataFix(schema190, "Drop invalid hanging sign datafix data", "minecraft:hanging_sign"));
		Schema schema191 = dataFixerBuilder.addSchema(3565, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new RandomSequenceSettingsFix(schema191));
		Schema schema192 = dataFixerBuilder.addSchema(3566, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ScoreboardDisplaySlotFix(schema192));
		Schema schema193 = dataFixerBuilder.addSchema(3568, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new MobEffectIdFix(schema193));
		Schema schema194 = dataFixerBuilder.addSchema(3682, V3682::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema194, "Added Crafter", References.BLOCK_ENTITY));
		Schema schema195 = dataFixerBuilder.addSchema(3683, V3683::new);
		dataFixerBuilder.addFixer(new PrimedTntBlockStateFixer(schema195));
		Schema schema196 = dataFixerBuilder.addSchema(3685, V3685::new);
		dataFixerBuilder.addFixer(new FixProjectileStoredItem(schema196));
		Schema schema197 = dataFixerBuilder.addSchema(3689, V3689::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema197, "Added Breeze", References.ENTITY));
		dataFixerBuilder.addFixer(new AddNewChoices(schema197, "Added Trial Spawner", References.BLOCK_ENTITY));
	}

	private static UnaryOperator<String> createRenamer(Map<String, String> map) {
		return string -> (String)map.getOrDefault(string, string);
	}

	private static UnaryOperator<String> createRenamer(String string, String string2) {
		return string3 -> Objects.equals(string3, string) ? string2 : string3;
	}
}
