/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class PlaceCommand {
    private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.feature.failed"));
    private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.structure.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("place").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("feature").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("feature", ResourceKeyArgument.key(Registry.CONFIGURED_FEATURE_REGISTRY)).executes(commandContext -> PlaceCommand.placeFeature((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeFeature((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"))))))).then(Commands.literal("jigsaw").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("pool", ResourceKeyArgument.key(Registry.TEMPLATE_POOL_REGISTRY)).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("target", ResourceLocationArgument.id()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("max_depth", IntegerArgumentType.integer(1, 7)).executes(commandContext -> PlaceCommand.placeJigsaw((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructureTemplatePool(commandContext, "pool"), ResourceLocationArgument.getId(commandContext, "target"), IntegerArgumentType.getInteger(commandContext, "max_depth"), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("position", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeJigsaw((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructureTemplatePool(commandContext, "pool"), ResourceLocationArgument.getId(commandContext, "target"), IntegerArgumentType.getInteger(commandContext, "max_depth"), BlockPosArgument.getLoadedBlockPos(commandContext, "position"))))))))).then(Commands.literal("structure").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("structure", ResourceKeyArgument.key(Registry.STRUCTURE_REGISTRY)).executes(commandContext -> PlaceCommand.placeStructure((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructure(commandContext, "structure"), new BlockPos(((CommandSourceStack)commandContext.getSource()).getPosition())))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(commandContext -> PlaceCommand.placeStructure((CommandSourceStack)commandContext.getSource(), ResourceKeyArgument.getStructure(commandContext, "structure"), BlockPosArgument.getLoadedBlockPos(commandContext, "pos")))))));
    }

    public static int placeFeature(CommandSourceStack commandSourceStack, Holder<ConfiguredFeature<?, ?>> holder, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ConfiguredFeature<?, ?> configuredFeature = holder.value();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        PlaceCommand.checkLoaded(serverLevel, new ChunkPos(chunkPos.x - 1, chunkPos.z - 1), new ChunkPos(chunkPos.x + 1, chunkPos.z + 1));
        if (!configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos)) {
            throw ERROR_FEATURE_FAILED.create();
        }
        String string = holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("[unregistered]");
        commandSourceStack.sendSuccess(Component.translatable("commands.place.feature.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    public static int placeJigsaw(CommandSourceStack commandSourceStack, Holder<StructureTemplatePool> holder, ResourceLocation resourceLocation, int i, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        if (!JigsawPlacement.generateJigsaw(serverLevel, holder, resourceLocation, i, blockPos, false)) {
            throw ERROR_JIGSAW_FAILED.create();
        }
        commandSourceStack.sendSuccess(Component.translatable("commands.place.jigsaw.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    public static int placeStructure(CommandSourceStack commandSourceStack, Holder<Structure> holder2, BlockPos blockPos) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        Structure structure = holder2.value();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        StructureStart structureStart = structure.generate(commandSourceStack.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), serverLevel.getChunkSource().randomState(), serverLevel.getStructureManager(), serverLevel.getSeed(), new ChunkPos(blockPos), 0, serverLevel, holder -> true);
        if (!structureStart.isValid()) {
            throw ERROR_STRUCTURE_FAILED.create();
        }
        BoundingBox boundingBox = structureStart.getBoundingBox();
        ChunkPos chunkPos2 = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
        ChunkPos chunkPos22 = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
        PlaceCommand.checkLoaded(serverLevel, chunkPos2, chunkPos22);
        ChunkPos.rangeClosed(chunkPos2, chunkPos22).forEach(chunkPos -> structureStart.placeInChunk(serverLevel, serverLevel.structureManager(), chunkGenerator, serverLevel.getRandom(), new BoundingBox(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight(), chunkPos.getMaxBlockZ()), (ChunkPos)chunkPos));
        String string = holder2.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("[unregistered]");
        commandSourceStack.sendSuccess(Component.translatable("commands.place.structure.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    private static void checkLoaded(ServerLevel serverLevel, ChunkPos chunkPos2, ChunkPos chunkPos22) throws CommandSyntaxException {
        if (ChunkPos.rangeClosed(chunkPos2, chunkPos22).filter(chunkPos -> !serverLevel.isLoaded(chunkPos.getWorldPosition())).findAny().isPresent()) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
    }
}

