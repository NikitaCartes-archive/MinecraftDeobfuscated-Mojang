package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PlaceCommand {
	private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.feature.failed"));
	private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
	private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(
		Component.translatable("commands.place.structure.failed")
	);
	private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.place.template.invalid", object)
	);
	private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType(
		Component.translatable("commands.place.template.failed")
	);
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = (commandContext, suggestionsBuilder) -> {
		StructureTemplateManager structureTemplateManager = commandContext.getSource().getLevel().getStructureManager();
		return SharedSuggestionProvider.suggestResource(structureTemplateManager.listTemplates(), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("place")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("feature")
						.then(
							Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE))
								.executes(
									commandContext -> placeFeature(
											commandContext.getSource(),
											ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"),
											BlockPos.containing(commandContext.getSource().getPosition())
										)
								)
								.then(
									Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(
											commandContext -> placeFeature(
													commandContext.getSource(),
													ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"),
													BlockPosArgument.getLoadedBlockPos(commandContext, "pos")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("jigsaw")
						.then(
							Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL))
								.then(
									Commands.argument("target", ResourceLocationArgument.id())
										.then(
											Commands.argument("max_depth", IntegerArgumentType.integer(1, 7))
												.executes(
													commandContext -> placeJigsaw(
															commandContext.getSource(),
															ResourceKeyArgument.getStructureTemplatePool(commandContext, "pool"),
															ResourceLocationArgument.getId(commandContext, "target"),
															IntegerArgumentType.getInteger(commandContext, "max_depth"),
															BlockPos.containing(commandContext.getSource().getPosition())
														)
												)
												.then(
													Commands.argument("position", BlockPosArgument.blockPos())
														.executes(
															commandContext -> placeJigsaw(
																	commandContext.getSource(),
																	ResourceKeyArgument.getStructureTemplatePool(commandContext, "pool"),
																	ResourceLocationArgument.getId(commandContext, "target"),
																	IntegerArgumentType.getInteger(commandContext, "max_depth"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "position")
																)
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("structure")
						.then(
							Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE))
								.executes(
									commandContext -> placeStructure(
											commandContext.getSource(),
											ResourceKeyArgument.getStructure(commandContext, "structure"),
											BlockPos.containing(commandContext.getSource().getPosition())
										)
								)
								.then(
									Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(
											commandContext -> placeStructure(
													commandContext.getSource(),
													ResourceKeyArgument.getStructure(commandContext, "structure"),
													BlockPosArgument.getLoadedBlockPos(commandContext, "pos")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("template")
						.then(
							Commands.argument("template", ResourceLocationArgument.id())
								.suggests(SUGGEST_TEMPLATES)
								.executes(
									commandContext -> placeTemplate(
											commandContext.getSource(),
											ResourceLocationArgument.getId(commandContext, "template"),
											BlockPos.containing(commandContext.getSource().getPosition()),
											Rotation.NONE,
											Mirror.NONE,
											1.0F,
											0
										)
								)
								.then(
									Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(
											commandContext -> placeTemplate(
													commandContext.getSource(),
													ResourceLocationArgument.getId(commandContext, "template"),
													BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
													Rotation.NONE,
													Mirror.NONE,
													1.0F,
													0
												)
										)
										.then(
											Commands.argument("rotation", TemplateRotationArgument.templateRotation())
												.executes(
													commandContext -> placeTemplate(
															commandContext.getSource(),
															ResourceLocationArgument.getId(commandContext, "template"),
															BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
															TemplateRotationArgument.getRotation(commandContext, "rotation"),
															Mirror.NONE,
															1.0F,
															0
														)
												)
												.then(
													Commands.argument("mirror", TemplateMirrorArgument.templateMirror())
														.executes(
															commandContext -> placeTemplate(
																	commandContext.getSource(),
																	ResourceLocationArgument.getId(commandContext, "template"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																	TemplateRotationArgument.getRotation(commandContext, "rotation"),
																	TemplateMirrorArgument.getMirror(commandContext, "mirror"),
																	1.0F,
																	0
																)
														)
														.then(
															Commands.argument("integrity", FloatArgumentType.floatArg(0.0F, 1.0F))
																.executes(
																	commandContext -> placeTemplate(
																			commandContext.getSource(),
																			ResourceLocationArgument.getId(commandContext, "template"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																			TemplateRotationArgument.getRotation(commandContext, "rotation"),
																			TemplateMirrorArgument.getMirror(commandContext, "mirror"),
																			FloatArgumentType.getFloat(commandContext, "integrity"),
																			0
																		)
																)
																.then(
																	Commands.argument("seed", IntegerArgumentType.integer())
																		.executes(
																			commandContext -> placeTemplate(
																					commandContext.getSource(),
																					ResourceLocationArgument.getId(commandContext, "template"),
																					BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
																					TemplateRotationArgument.getRotation(commandContext, "rotation"),
																					TemplateMirrorArgument.getMirror(commandContext, "mirror"),
																					FloatArgumentType.getFloat(commandContext, "integrity"),
																					IntegerArgumentType.getInteger(commandContext, "seed")
																				)
																		)
																)
														)
												)
										)
								)
						)
				)
		);
	}

	public static int placeFeature(CommandSourceStack commandSourceStack, Holder.Reference<ConfiguredFeature<?, ?>> reference, BlockPos blockPos) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ConfiguredFeature<?, ?> configuredFeature = reference.value();
		ChunkPos chunkPos = new ChunkPos(blockPos);
		checkLoaded(serverLevel, new ChunkPos(chunkPos.x - 1, chunkPos.z - 1), new ChunkPos(chunkPos.x + 1, chunkPos.z + 1));
		if (!configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos)) {
			throw ERROR_FEATURE_FAILED.create();
		} else {
			String string = reference.key().location().toString();
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.place.feature.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true
			);
			return 1;
		}
	}

	public static int placeJigsaw(
		CommandSourceStack commandSourceStack, Holder<StructureTemplatePool> holder, ResourceLocation resourceLocation, int i, BlockPos blockPos
	) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		if (!JigsawPlacement.generateJigsaw(serverLevel, holder, resourceLocation, i, blockPos, false)) {
			throw ERROR_JIGSAW_FAILED.create();
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.place.jigsaw.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			return 1;
		}
	}

	public static int placeStructure(CommandSourceStack commandSourceStack, Holder.Reference<Structure> reference, BlockPos blockPos) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		Structure structure = reference.value();
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		StructureStart structureStart = structure.generate(
			commandSourceStack.registryAccess(),
			chunkGenerator,
			chunkGenerator.getBiomeSource(),
			serverLevel.getChunkSource().randomState(),
			serverLevel.getStructureManager(),
			serverLevel.getSeed(),
			new ChunkPos(blockPos),
			0,
			serverLevel,
			holder -> true
		);
		if (!structureStart.isValid()) {
			throw ERROR_STRUCTURE_FAILED.create();
		} else {
			BoundingBox boundingBox = structureStart.getBoundingBox();
			ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
			ChunkPos chunkPos2 = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
			checkLoaded(serverLevel, chunkPos, chunkPos2);
			ChunkPos.rangeClosed(chunkPos, chunkPos2)
				.forEach(
					chunkPosx -> structureStart.placeInChunk(
							serverLevel,
							serverLevel.structureManager(),
							chunkGenerator,
							serverLevel.getRandom(),
							new BoundingBox(
								chunkPosx.getMinBlockX(),
								serverLevel.getMinBuildHeight(),
								chunkPosx.getMinBlockZ(),
								chunkPosx.getMaxBlockX(),
								serverLevel.getMaxBuildHeight(),
								chunkPosx.getMaxBlockZ()
							),
							chunkPosx
						)
				);
			String string = reference.key().location().toString();
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.place.structure.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true
			);
			return 1;
		}
	}

	public static int placeTemplate(
		CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, Mirror mirror, float f, int i
	) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();

		Optional<StructureTemplate> optional;
		try {
			optional = structureTemplateManager.get(resourceLocation);
		} catch (ResourceLocationException var13) {
			throw ERROR_TEMPLATE_INVALID.create(resourceLocation);
		}

		if (optional.isEmpty()) {
			throw ERROR_TEMPLATE_INVALID.create(resourceLocation);
		} else {
			StructureTemplate structureTemplate = (StructureTemplate)optional.get();
			checkLoaded(serverLevel, new ChunkPos(blockPos), new ChunkPos(blockPos.offset(structureTemplate.getSize())));
			StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation);
			if (f < 1.0F) {
				structurePlaceSettings.clearProcessors().addProcessor(new BlockRotProcessor(f)).setRandom(StructureBlockEntity.createRandom((long)i));
			}

			boolean bl = structureTemplate.placeInWorld(serverLevel, blockPos, blockPos, structurePlaceSettings, StructureBlockEntity.createRandom((long)i), 2);
			if (!bl) {
				throw ERROR_TEMPLATE_FAILED.create();
			} else {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.place.template.success", resourceLocation, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true
				);
				return 1;
			}
		}
	}

	private static void checkLoaded(ServerLevel serverLevel, ChunkPos chunkPos, ChunkPos chunkPos2) throws CommandSyntaxException {
		if (ChunkPos.rangeClosed(chunkPos, chunkPos2).filter(chunkPosx -> !serverLevel.isLoaded(chunkPosx.getWorldPosition())).findAny().isPresent()) {
			throw BlockPosArgument.ERROR_NOT_LOADED.create();
		}
	}
}
