package net.minecraft.util.datafix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.function.BiFunction;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.AdvancementsFix;
import net.minecraft.util.datafix.fixes.BedBlockEntityInjecter;
import net.minecraft.util.datafix.fixes.BedItemColorFix;
import net.minecraft.util.datafix.fixes.BeehivePoiRenameFix;
import net.minecraft.util.datafix.fixes.BiomeFix;
import net.minecraft.util.datafix.fixes.BlockEntityBannerColorFix;
import net.minecraft.util.datafix.fixes.BlockEntityBlockStateFix;
import net.minecraft.util.datafix.fixes.BlockEntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.BlockEntityIdFix;
import net.minecraft.util.datafix.fixes.BlockEntityJukeboxFix;
import net.minecraft.util.datafix.fixes.BlockEntityKeepPacked;
import net.minecraft.util.datafix.fixes.BlockEntityShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix;
import net.minecraft.util.datafix.fixes.BlockNameFlatteningFix;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.fixes.BlockStateStructureTemplateFix;
import net.minecraft.util.datafix.fixes.CatTypeFix;
import net.minecraft.util.datafix.fixes.ChunkBiomeFix;
import net.minecraft.util.datafix.fixes.ChunkLightRemoveFix;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix;
import net.minecraft.util.datafix.fixes.ChunkStatusFix2;
import net.minecraft.util.datafix.fixes.ChunkStructuresTemplateRenameFix;
import net.minecraft.util.datafix.fixes.ChunkToProtochunkFix;
import net.minecraft.util.datafix.fixes.ColorlessShulkerEntityFix;
import net.minecraft.util.datafix.fixes.DyeItemRenameFix;
import net.minecraft.util.datafix.fixes.EntityArmorStandSilentFix;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.EntityCatSplitFix;
import net.minecraft.util.datafix.fixes.EntityCodSalmonFix;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.EntityElderGuardianSplitFix;
import net.minecraft.util.datafix.fixes.EntityEquipmentToArmorAndHandFix;
import net.minecraft.util.datafix.fixes.EntityHealthFix;
import net.minecraft.util.datafix.fixes.EntityHorseSaddleFix;
import net.minecraft.util.datafix.fixes.EntityHorseSplitFix;
import net.minecraft.util.datafix.fixes.EntityIdFix;
import net.minecraft.util.datafix.fixes.EntityItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityMinecartIdentifiersFix;
import net.minecraft.util.datafix.fixes.EntityPaintingItemFrameDirectionFix;
import net.minecraft.util.datafix.fixes.EntityPaintingMotiveFix;
import net.minecraft.util.datafix.fixes.EntityPufferfishRenameFix;
import net.minecraft.util.datafix.fixes.EntityRavagerRenameFix;
import net.minecraft.util.datafix.fixes.EntityRedundantChanceTagsFix;
import net.minecraft.util.datafix.fixes.EntityRidingToPassengersFix;
import net.minecraft.util.datafix.fixes.EntityShulkerColorFix;
import net.minecraft.util.datafix.fixes.EntitySkeletonSplitFix;
import net.minecraft.util.datafix.fixes.EntityStringUuidFix;
import net.minecraft.util.datafix.fixes.EntityTheRenameningFix;
import net.minecraft.util.datafix.fixes.EntityTippedArrowFix;
import net.minecraft.util.datafix.fixes.EntityWolfColorFix;
import net.minecraft.util.datafix.fixes.EntityZombieSplitFix;
import net.minecraft.util.datafix.fixes.EntityZombieVillagerTypeFix;
import net.minecraft.util.datafix.fixes.ForcePoiRebuild;
import net.minecraft.util.datafix.fixes.HeightmapRenamingFix;
import net.minecraft.util.datafix.fixes.IglooMetadataRemovalFix;
import net.minecraft.util.datafix.fixes.ItemBannerColorFix;
import net.minecraft.util.datafix.fixes.ItemCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.ItemLoreFix;
import net.minecraft.util.datafix.fixes.ItemPotionFix;
import net.minecraft.util.datafix.fixes.ItemRenameFix;
import net.minecraft.util.datafix.fixes.ItemShulkerBoxColorFix;
import net.minecraft.util.datafix.fixes.ItemSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackEnchantmentNamesFix;
import net.minecraft.util.datafix.fixes.ItemStackMapIdFix;
import net.minecraft.util.datafix.fixes.ItemStackSpawnEggFix;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.util.datafix.fixes.ItemWaterPotionFix;
import net.minecraft.util.datafix.fixes.ItemWrittenBookPagesStrictJsonFix;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.util.datafix.fixes.LevelDataGeneratorOptionsFix;
import net.minecraft.util.datafix.fixes.LevelFlatGeneratorInfoFix;
import net.minecraft.util.datafix.fixes.MapIdFix;
import net.minecraft.util.datafix.fixes.MobSpawnerEntityIdentifiersFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.NewVillageFix;
import net.minecraft.util.datafix.fixes.ObjectiveDisplayNameFix;
import net.minecraft.util.datafix.fixes.ObjectiveRenderTypeFix;
import net.minecraft.util.datafix.fixes.OptionsAddTextBackgroundFix;
import net.minecraft.util.datafix.fixes.OptionsForceVBOFix;
import net.minecraft.util.datafix.fixes.OptionsKeyLwjgl3Fix;
import net.minecraft.util.datafix.fixes.OptionsKeyTranslationFix;
import net.minecraft.util.datafix.fixes.OptionsLowerCaseLanguageFix;
import net.minecraft.util.datafix.fixes.RecipesFix;
import net.minecraft.util.datafix.fixes.RecipesRenameningFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.RenamedCoralFansFix;
import net.minecraft.util.datafix.fixes.RenamedCoralFix;
import net.minecraft.util.datafix.fixes.ReorganizePoi;
import net.minecraft.util.datafix.fixes.SavedDataVillageCropFix;
import net.minecraft.util.datafix.fixes.StatsCounterFix;
import net.minecraft.util.datafix.fixes.StructureReferenceCountFix;
import net.minecraft.util.datafix.fixes.SwimStatsRenameFix;
import net.minecraft.util.datafix.fixes.TeamDisplayNameFix;
import net.minecraft.util.datafix.fixes.TrappedChestBlockEntityFix;
import net.minecraft.util.datafix.fixes.VillagerDataFix;
import net.minecraft.util.datafix.fixes.VillagerRebuildLevelAndXpFix;
import net.minecraft.util.datafix.fixes.VillagerTradeFix;
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
import net.minecraft.util.datafix.schemas.V1451_7;
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
	private static final DataFixer DATA_FIXER = createFixerUpper();

	private static DataFixer createFixerUpper() {
		DataFixerBuilder dataFixerBuilder = new DataFixerBuilder(SharedConstants.getCurrentVersion().getWorldVersion());
		addFixers(dataFixerBuilder);
		return dataFixerBuilder.build(Util.backgroundExecutor());
	}

	public static DataFixer getDataFixer() {
		return DATA_FIXER;
	}

	private static void addFixers(DataFixerBuilder dataFixerBuilder) {
		Schema schema = dataFixerBuilder.addSchema(99, V99::new);
		Schema schema2 = dataFixerBuilder.addSchema(100, V100::new);
		dataFixerBuilder.addFixer(new EntityEquipmentToArmorAndHandFix(schema2, true));
		Schema schema3 = dataFixerBuilder.addSchema(101, SAME);
		dataFixerBuilder.addFixer(new BlockEntitySignTextStrictJsonFix(schema3, false));
		Schema schema4 = dataFixerBuilder.addSchema(102, V102::new);
		dataFixerBuilder.addFixer(new ItemIdFix(schema4, true));
		dataFixerBuilder.addFixer(new ItemPotionFix(schema4, false));
		Schema schema5 = dataFixerBuilder.addSchema(105, SAME);
		dataFixerBuilder.addFixer(new ItemSpawnEggFix(schema5, true));
		Schema schema6 = dataFixerBuilder.addSchema(106, V106::new);
		dataFixerBuilder.addFixer(new MobSpawnerEntityIdentifiersFix(schema6, true));
		Schema schema7 = dataFixerBuilder.addSchema(107, V107::new);
		dataFixerBuilder.addFixer(new EntityMinecartIdentifiersFix(schema7, true));
		Schema schema8 = dataFixerBuilder.addSchema(108, SAME);
		dataFixerBuilder.addFixer(new EntityStringUuidFix(schema8, true));
		Schema schema9 = dataFixerBuilder.addSchema(109, SAME);
		dataFixerBuilder.addFixer(new EntityHealthFix(schema9, true));
		Schema schema10 = dataFixerBuilder.addSchema(110, SAME);
		dataFixerBuilder.addFixer(new EntityHorseSaddleFix(schema10, true));
		Schema schema11 = dataFixerBuilder.addSchema(111, SAME);
		dataFixerBuilder.addFixer(new EntityPaintingItemFrameDirectionFix(schema11, true));
		Schema schema12 = dataFixerBuilder.addSchema(113, SAME);
		dataFixerBuilder.addFixer(new EntityRedundantChanceTagsFix(schema12, true));
		Schema schema13 = dataFixerBuilder.addSchema(135, V135::new);
		dataFixerBuilder.addFixer(new EntityRidingToPassengersFix(schema13, true));
		Schema schema14 = dataFixerBuilder.addSchema(143, V143::new);
		dataFixerBuilder.addFixer(new EntityTippedArrowFix(schema14, true));
		Schema schema15 = dataFixerBuilder.addSchema(147, SAME);
		dataFixerBuilder.addFixer(new EntityArmorStandSilentFix(schema15, true));
		Schema schema16 = dataFixerBuilder.addSchema(165, SAME);
		dataFixerBuilder.addFixer(new ItemWrittenBookPagesStrictJsonFix(schema16, true));
		Schema schema17 = dataFixerBuilder.addSchema(501, V501::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema17, "Add 1.10 entities fix", References.ENTITY));
		Schema schema18 = dataFixerBuilder.addSchema(502, SAME);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema18,
				"cooked_fished item renamer",
				string -> Objects.equals(NamespacedSchema.ensureNamespaced(string), "minecraft:cooked_fished") ? "minecraft:cooked_fish" : string
			)
		);
		dataFixerBuilder.addFixer(new EntityZombieVillagerTypeFix(schema18, false));
		Schema schema19 = dataFixerBuilder.addSchema(505, SAME);
		dataFixerBuilder.addFixer(new OptionsForceVBOFix(schema19, false));
		Schema schema20 = dataFixerBuilder.addSchema(700, V700::new);
		dataFixerBuilder.addFixer(new EntityElderGuardianSplitFix(schema20, true));
		Schema schema21 = dataFixerBuilder.addSchema(701, V701::new);
		dataFixerBuilder.addFixer(new EntitySkeletonSplitFix(schema21, true));
		Schema schema22 = dataFixerBuilder.addSchema(702, V702::new);
		dataFixerBuilder.addFixer(new EntityZombieSplitFix(schema22, true));
		Schema schema23 = dataFixerBuilder.addSchema(703, V703::new);
		dataFixerBuilder.addFixer(new EntityHorseSplitFix(schema23, true));
		Schema schema24 = dataFixerBuilder.addSchema(704, V704::new);
		dataFixerBuilder.addFixer(new BlockEntityIdFix(schema24, true));
		Schema schema25 = dataFixerBuilder.addSchema(705, V705::new);
		dataFixerBuilder.addFixer(new EntityIdFix(schema25, true));
		Schema schema26 = dataFixerBuilder.addSchema(804, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemBannerColorFix(schema26, true));
		Schema schema27 = dataFixerBuilder.addSchema(806, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemWaterPotionFix(schema27, false));
		Schema schema28 = dataFixerBuilder.addSchema(808, V808::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema28, "added shulker box", References.BLOCK_ENTITY));
		Schema schema29 = dataFixerBuilder.addSchema(808, 1, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityShulkerColorFix(schema29, false));
		Schema schema30 = dataFixerBuilder.addSchema(813, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemShulkerBoxColorFix(schema30, false));
		dataFixerBuilder.addFixer(new BlockEntityShulkerBoxColorFix(schema30, false));
		Schema schema31 = dataFixerBuilder.addSchema(816, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsLowerCaseLanguageFix(schema31, false));
		Schema schema32 = dataFixerBuilder.addSchema(820, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema32, "totem item renamer", string -> Objects.equals(string, "minecraft:totem") ? "minecraft:totem_of_undying" : string)
		);
		Schema schema33 = dataFixerBuilder.addSchema(1022, V1022::new);
		dataFixerBuilder.addFixer(new WriteAndReadFix(schema33, "added shoulder entities to players", References.PLAYER));
		Schema schema34 = dataFixerBuilder.addSchema(1125, V1125::new);
		dataFixerBuilder.addFixer(new BedBlockEntityInjecter(schema34, true));
		dataFixerBuilder.addFixer(new BedItemColorFix(schema34, false));
		Schema schema35 = dataFixerBuilder.addSchema(1344, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsKeyLwjgl3Fix(schema35, false));
		Schema schema36 = dataFixerBuilder.addSchema(1446, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsKeyTranslationFix(schema36, false));
		Schema schema37 = dataFixerBuilder.addSchema(1450, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlockStateStructureTemplateFix(schema37, false));
		Schema schema38 = dataFixerBuilder.addSchema(1451, V1451::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema38, "AddTrappedChestFix", References.BLOCK_ENTITY));
		Schema schema39 = dataFixerBuilder.addSchema(1451, 1, V1451_1::new);
		dataFixerBuilder.addFixer(new ChunkPalettedStorageFix(schema39, true));
		Schema schema40 = dataFixerBuilder.addSchema(1451, 2, V1451_2::new);
		dataFixerBuilder.addFixer(new BlockEntityBlockStateFix(schema40, true));
		Schema schema41 = dataFixerBuilder.addSchema(1451, 3, V1451_3::new);
		dataFixerBuilder.addFixer(new EntityBlockStateFix(schema41, true));
		dataFixerBuilder.addFixer(new ItemStackMapIdFix(schema41, false));
		Schema schema42 = dataFixerBuilder.addSchema(1451, 4, V1451_4::new);
		dataFixerBuilder.addFixer(new BlockNameFlatteningFix(schema42, true));
		dataFixerBuilder.addFixer(new ItemStackTheFlatteningFix(schema42, false));
		Schema schema43 = dataFixerBuilder.addSchema(1451, 5, V1451_5::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema43, "RemoveNoteBlockFlowerPotFix", References.BLOCK_ENTITY));
		dataFixerBuilder.addFixer(new ItemStackSpawnEggFix(schema43, false));
		dataFixerBuilder.addFixer(new EntityWolfColorFix(schema43, false));
		dataFixerBuilder.addFixer(new BlockEntityBannerColorFix(schema43, false));
		dataFixerBuilder.addFixer(new LevelFlatGeneratorInfoFix(schema43, false));
		Schema schema44 = dataFixerBuilder.addSchema(1451, 6, V1451_6::new);
		dataFixerBuilder.addFixer(new StatsCounterFix(schema44, true));
		dataFixerBuilder.addFixer(new BlockEntityJukeboxFix(schema44, false));
		Schema schema45 = dataFixerBuilder.addSchema(1451, 7, V1451_7::new);
		dataFixerBuilder.addFixer(new SavedDataVillageCropFix(schema45, true));
		Schema schema46 = dataFixerBuilder.addSchema(1451, 7, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerTradeFix(schema46, false));
		Schema schema47 = dataFixerBuilder.addSchema(1456, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityItemFrameDirectionFix(schema47, false));
		Schema schema48 = dataFixerBuilder.addSchema(1458, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new EntityCustomNameToComponentFix(schema48, false));
		dataFixerBuilder.addFixer(new ItemCustomNameToComponentFix(schema48, false));
		dataFixerBuilder.addFixer(new BlockEntityCustomNameToComponentFix(schema48, false));
		Schema schema49 = dataFixerBuilder.addSchema(1460, V1460::new);
		dataFixerBuilder.addFixer(new EntityPaintingMotiveFix(schema49, false));
		Schema schema50 = dataFixerBuilder.addSchema(1466, V1466::new);
		dataFixerBuilder.addFixer(new ChunkToProtochunkFix(schema50, true));
		Schema schema51 = dataFixerBuilder.addSchema(1470, V1470::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema51, "Add 1.13 entities fix", References.ENTITY));
		Schema schema52 = dataFixerBuilder.addSchema(1474, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ColorlessShulkerEntityFix(schema52, false));
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema52,
				"Colorless shulker block fixer",
				string -> Objects.equals(NamespacedSchema.ensureNamespaced(string), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : string
			)
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema52,
				"Colorless shulker item fixer",
				string -> Objects.equals(NamespacedSchema.ensureNamespaced(string), "minecraft:purple_shulker_box") ? "minecraft:shulker_box" : string
			)
		);
		Schema schema53 = dataFixerBuilder.addSchema(1475, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema53,
				"Flowing fixer",
				string -> ImmutableMap.of("minecraft:flowing_water", "minecraft:water", "minecraft:flowing_lava", "minecraft:lava").getOrDefault(string, string)
			)
		);
		Schema schema54 = dataFixerBuilder.addSchema(1480, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(BlockRenameFix.create(schema54, "Rename coral blocks", string -> (String)RenamedCoralFix.RENAMED_IDS.getOrDefault(string, string)));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema54, "Rename coral items", string -> (String)RenamedCoralFix.RENAMED_IDS.getOrDefault(string, string)));
		Schema schema55 = dataFixerBuilder.addSchema(1481, V1481::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema55, "Add conduit", References.BLOCK_ENTITY));
		Schema schema56 = dataFixerBuilder.addSchema(1483, V1483::new);
		dataFixerBuilder.addFixer(new EntityPufferfishRenameFix(schema56, true));
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema56, "Rename pufferfish egg item", string -> (String)EntityPufferfishRenameFix.RENAMED_IDS.getOrDefault(string, string))
		);
		Schema schema57 = dataFixerBuilder.addSchema(1484, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema57,
				"Rename seagrass items",
				string -> ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass").getOrDefault(string, string)
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema57,
				"Rename seagrass blocks",
				string -> ImmutableMap.of("minecraft:sea_grass", "minecraft:seagrass", "minecraft:tall_sea_grass", "minecraft:tall_seagrass").getOrDefault(string, string)
			)
		);
		dataFixerBuilder.addFixer(new HeightmapRenamingFix(schema57, false));
		Schema schema58 = dataFixerBuilder.addSchema(1486, V1486::new);
		dataFixerBuilder.addFixer(new EntityCodSalmonFix(schema58, true));
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema58, "Rename cod/salmon egg items", string -> (String)EntityCodSalmonFix.RENAMED_EGG_IDS.getOrDefault(string, string))
		);
		Schema schema59 = dataFixerBuilder.addSchema(1487, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema59,
				"Rename prismarine_brick(s)_* blocks",
				string -> ImmutableMap.of(
							"minecraft:prismarine_bricks_slab", "minecraft:prismarine_brick_slab", "minecraft:prismarine_bricks_stairs", "minecraft:prismarine_brick_stairs"
						)
						.getOrDefault(string, string)
			)
		);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema59,
				"Rename prismarine_brick(s)_* items",
				string -> ImmutableMap.of(
							"minecraft:prismarine_bricks_slab", "minecraft:prismarine_brick_slab", "minecraft:prismarine_bricks_stairs", "minecraft:prismarine_brick_stairs"
						)
						.getOrDefault(string, string)
			)
		);
		Schema schema60 = dataFixerBuilder.addSchema(1488, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema60,
				"Rename kelp/kelptop",
				string -> ImmutableMap.of("minecraft:kelp_top", "minecraft:kelp", "minecraft:kelp", "minecraft:kelp_plant").getOrDefault(string, string)
			)
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema60, "Rename kelptop", string -> Objects.equals(string, "minecraft:kelp_top") ? "minecraft:kelp" : string)
		);
		dataFixerBuilder.addFixer(
			new NamedEntityFix(schema60, false, "Command block block entity custom name fix", References.BLOCK_ENTITY, "minecraft:command_block") {
				@Override
				protected Typed<?> fix(Typed<?> typed) {
					return typed.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName);
				}
			}
		);
		dataFixerBuilder.addFixer(
			new NamedEntityFix(schema60, false, "Command block minecart custom name fix", References.ENTITY, "minecraft:commandblock_minecart") {
				@Override
				protected Typed<?> fix(Typed<?> typed) {
					return typed.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName);
				}
			}
		);
		dataFixerBuilder.addFixer(new IglooMetadataRemovalFix(schema60, false));
		Schema schema61 = dataFixerBuilder.addSchema(1490, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(schema61, "Rename melon_block", string -> Objects.equals(string, "minecraft:melon_block") ? "minecraft:melon" : string)
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema61,
				"Rename melon_block/melon/speckled_melon",
				string -> ImmutableMap.of(
							"minecraft:melon_block", "minecraft:melon", "minecraft:melon", "minecraft:melon_slice", "minecraft:speckled_melon", "minecraft:glistering_melon_slice"
						)
						.getOrDefault(string, string)
			)
		);
		Schema schema62 = dataFixerBuilder.addSchema(1492, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkStructuresTemplateRenameFix(schema62, false));
		Schema schema63 = dataFixerBuilder.addSchema(1494, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemStackEnchantmentNamesFix(schema63, false));
		Schema schema64 = dataFixerBuilder.addSchema(1496, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new LeavesFix(schema64, false));
		Schema schema65 = dataFixerBuilder.addSchema(1500, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BlockEntityKeepPacked(schema65, false));
		Schema schema66 = dataFixerBuilder.addSchema(1501, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new AdvancementsFix(schema66, false));
		Schema schema67 = dataFixerBuilder.addSchema(1502, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new RecipesFix(schema67, false));
		Schema schema68 = dataFixerBuilder.addSchema(1506, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new LevelDataGeneratorOptionsFix(schema68, false));
		Schema schema69 = dataFixerBuilder.addSchema(1508, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new BiomeFix(schema69, false));
		Schema schema70 = dataFixerBuilder.addSchema(1510, V1510::new);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(schema70, "Block renamening fix", string -> (String)EntityTheRenameningFix.RENAMED_BLOCKS.getOrDefault(string, string))
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema70, "Item renamening fix", string -> (String)EntityTheRenameningFix.RENAMED_ITEMS.getOrDefault(string, string))
		);
		dataFixerBuilder.addFixer(new RecipesRenameningFix(schema70, false));
		dataFixerBuilder.addFixer(new EntityTheRenameningFix(schema70, true));
		dataFixerBuilder.addFixer(new SwimStatsRenameFix(schema70, false));
		Schema schema71 = dataFixerBuilder.addSchema(1514, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ObjectiveDisplayNameFix(schema71, false));
		dataFixerBuilder.addFixer(new TeamDisplayNameFix(schema71, false));
		dataFixerBuilder.addFixer(new ObjectiveRenderTypeFix(schema71, false));
		Schema schema72 = dataFixerBuilder.addSchema(1515, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(schema72, "Rename coral fan blocks", string -> (String)RenamedCoralFansFix.RENAMED_IDS.getOrDefault(string, string))
		);
		Schema schema73 = dataFixerBuilder.addSchema(1624, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new TrappedChestBlockEntityFix(schema73, false));
		Schema schema74 = dataFixerBuilder.addSchema(1800, V1800::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema74, "Added 1.14 mobs fix", References.ENTITY));
		dataFixerBuilder.addFixer(ItemRenameFix.create(schema74, "Rename dye items", string -> (String)DyeItemRenameFix.RENAMED_IDS.getOrDefault(string, string)));
		Schema schema75 = dataFixerBuilder.addSchema(1801, V1801::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema75, "Added Illager Beast", References.ENTITY));
		Schema schema76 = dataFixerBuilder.addSchema(1802, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema76,
				"Rename sign blocks & stone slabs",
				string -> ImmutableMap.of(
							"minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign", "minecraft:wall_sign", "minecraft:oak_wall_sign"
						)
						.getOrDefault(string, string)
			)
		);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(
				schema76,
				"Rename sign item & stone slabs",
				string -> ImmutableMap.of("minecraft:stone_slab", "minecraft:smooth_stone_slab", "minecraft:sign", "minecraft:oak_sign").getOrDefault(string, string)
			)
		);
		Schema schema77 = dataFixerBuilder.addSchema(1803, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ItemLoreFix(schema77, false));
		Schema schema78 = dataFixerBuilder.addSchema(1904, V1904::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema78, "Added Cats", References.ENTITY));
		dataFixerBuilder.addFixer(new EntityCatSplitFix(schema78, false));
		Schema schema79 = dataFixerBuilder.addSchema(1905, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkStatusFix(schema79, false));
		Schema schema80 = dataFixerBuilder.addSchema(1906, V1906::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema80, "Add POI Blocks", References.BLOCK_ENTITY));
		Schema schema81 = dataFixerBuilder.addSchema(1909, V1909::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema81, "Add jigsaw", References.BLOCK_ENTITY));
		Schema schema82 = dataFixerBuilder.addSchema(1911, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkStatusFix2(schema82, false));
		Schema schema83 = dataFixerBuilder.addSchema(1917, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new CatTypeFix(schema83, false));
		Schema schema84 = dataFixerBuilder.addSchema(1918, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerDataFix(schema84, "minecraft:villager"));
		dataFixerBuilder.addFixer(new VillagerDataFix(schema84, "minecraft:zombie_villager"));
		Schema schema85 = dataFixerBuilder.addSchema(1920, V1920::new);
		dataFixerBuilder.addFixer(new NewVillageFix(schema85, false));
		dataFixerBuilder.addFixer(new AddNewChoices(schema85, "Add campfire", References.BLOCK_ENTITY));
		Schema schema86 = dataFixerBuilder.addSchema(1925, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new MapIdFix(schema86, false));
		Schema schema87 = dataFixerBuilder.addSchema(1928, V1928::new);
		dataFixerBuilder.addFixer(new EntityRavagerRenameFix(schema87, true));
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema87, "Rename ravager egg item", string -> (String)EntityRavagerRenameFix.RENAMED_IDS.getOrDefault(string, string))
		);
		Schema schema88 = dataFixerBuilder.addSchema(1929, V1929::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema88, "Add Wandering Trader and Trader Llama", References.ENTITY));
		Schema schema89 = dataFixerBuilder.addSchema(1931, V1931::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema89, "Added Fox", References.ENTITY));
		Schema schema90 = dataFixerBuilder.addSchema(1936, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OptionsAddTextBackgroundFix(schema90, false));
		Schema schema91 = dataFixerBuilder.addSchema(1946, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ReorganizePoi(schema91, false));
		Schema schema92 = dataFixerBuilder.addSchema(1948, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OminousBannerRenameFix(schema92, false));
		Schema schema93 = dataFixerBuilder.addSchema(1953, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new OminousBannerBlockEntityRenameFix(schema93, false));
		Schema schema94 = dataFixerBuilder.addSchema(1955, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new VillagerRebuildLevelAndXpFix(schema94, false));
		dataFixerBuilder.addFixer(new ZombieVillagerRebuildXpFix(schema94, false));
		Schema schema95 = dataFixerBuilder.addSchema(1961, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkLightRemoveFix(schema95, false));
		Schema schema96 = dataFixerBuilder.addSchema(2100, V2100::new);
		dataFixerBuilder.addFixer(new AddNewChoices(schema96, "Added Bee and Bee Stinger", References.ENTITY));
		dataFixerBuilder.addFixer(new AddNewChoices(schema96, "Add beehive", References.BLOCK_ENTITY));
		Schema schema97 = dataFixerBuilder.addSchema(2202, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ChunkBiomeFix(schema97, false));
		Schema schema98 = dataFixerBuilder.addSchema(2209, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(
			ItemRenameFix.create(schema98, "Rename bee_hive item to beehive", string -> Objects.equals(string, "minecraft:bee_hive") ? "minecraft:beehive" : string)
		);
		dataFixerBuilder.addFixer(new BeehivePoiRenameFix(schema98));
		dataFixerBuilder.addFixer(
			BlockRenameFix.create(
				schema98, "Rename bee_hive block to beehive", string -> ImmutableMap.of("minecraft:bee_hive", "minecraft:beehive").getOrDefault(string, string)
			)
		);
		Schema schema99 = dataFixerBuilder.addSchema(2211, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new StructureReferenceCountFix(schema99, false));
		Schema schema100 = dataFixerBuilder.addSchema(2218, SAME_NAMESPACED);
		dataFixerBuilder.addFixer(new ForcePoiRebuild(schema100, false));
	}
}
