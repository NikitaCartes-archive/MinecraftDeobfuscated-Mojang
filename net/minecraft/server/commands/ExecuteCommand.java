/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;

public class ExecuteCommand {
    private static final int MAX_TEST_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("commands.execute.blocks.toobig", object, object2));
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(object -> Component.translatable("commands.execute.conditional.fail_count", object));
    private static final BinaryOperator<ResultConsumer<CommandSourceStack>> CALLBACK_CHAINER = (resultConsumer, resultConsumer2) -> (commandContext, bl, i) -> {
        resultConsumer.onCommandComplete(commandContext, bl, i);
        resultConsumer2.onCommandComplete(commandContext, bl, i);
    };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (commandContext, suggestionsBuilder) -> {
        PredicateManager predicateManager = ((CommandSourceStack)commandContext.getSource()).getServer().getPredicateManager();
        return SharedSuggestionProvider.suggestResource(predicateManager.getKeys(), suggestionsBuilder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("execute").requires(commandSourceStack -> commandSourceStack.hasPermission(2)));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("execute").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("run").redirect(commandDispatcher.getRoot()))).then(ExecuteCommand.addConditionals(literalCommandNode, Commands.literal("if"), true, commandBuildContext))).then(ExecuteCommand.addConditionals(literalCommandNode, Commands.literal("unless"), false, commandBuildContext))).then(Commands.literal("as").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
            ArrayList<CommandSourceStack> list = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
                list.add(((CommandSourceStack)commandContext.getSource()).withEntity(entity));
            }
            return list;
        })))).then(Commands.literal("at").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
            ArrayList<CommandSourceStack> list = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
                list.add(((CommandSourceStack)commandContext.getSource()).withLevel((ServerLevel)entity.level).withPosition(entity.position()).withRotation(entity.getRotationVector()));
            }
            return list;
        })))).then(((LiteralArgumentBuilder)Commands.literal("store").then(ExecuteCommand.wrapStores(literalCommandNode, Commands.literal("result"), true))).then(ExecuteCommand.wrapStores(literalCommandNode, Commands.literal("success"), false)))).then(((LiteralArgumentBuilder)Commands.literal("positioned").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("pos", Vec3Argument.vec3()).redirect(literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withPosition(Vec3Argument.getVec3(commandContext, "pos")).withAnchor(EntityAnchorArgument.Anchor.FEET)))).then(Commands.literal("as").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
            ArrayList<CommandSourceStack> list = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
                list.add(((CommandSourceStack)commandContext.getSource()).withPosition(entity.position()));
            }
            return list;
        }))))).then(((LiteralArgumentBuilder)Commands.literal("rotated").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("rot", RotationArgument.rotation()).redirect(literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withRotation(RotationArgument.getRotation(commandContext, "rot").getRotation((CommandSourceStack)commandContext.getSource()))))).then(Commands.literal("as").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
            ArrayList<CommandSourceStack> list = Lists.newArrayList();
            for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
                list.add(((CommandSourceStack)commandContext.getSource()).withRotation(entity.getRotationVector()));
            }
            return list;
        }))))).then(((LiteralArgumentBuilder)Commands.literal("facing").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("anchor", EntityAnchorArgument.anchor()).fork(literalCommandNode, commandContext -> {
            ArrayList<CommandSourceStack> list = Lists.newArrayList();
            EntityAnchorArgument.Anchor anchor = EntityAnchorArgument.getAnchor(commandContext, "anchor");
            for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
                list.add(((CommandSourceStack)commandContext.getSource()).facing(entity, anchor));
            }
            return list;
        }))))).then(Commands.argument("pos", Vec3Argument.vec3()).redirect(literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).facing(Vec3Argument.getVec3(commandContext, "pos")))))).then(Commands.literal("align").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("axes", SwizzleArgument.swizzle()).redirect(literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withPosition(((CommandSourceStack)commandContext.getSource()).getPosition().align(SwizzleArgument.getSwizzle(commandContext, "axes"))))))).then(Commands.literal("anchored").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("anchor", EntityAnchorArgument.anchor()).redirect(literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withAnchor(EntityAnchorArgument.getAnchor(commandContext, "anchor")))))).then(Commands.literal("in").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("dimension", DimensionArgument.dimension()).redirect(literalCommandNode, commandContext -> ((CommandSourceStack)commandContext.getSource()).withLevel(DimensionArgument.getDimension(commandContext, "dimension"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(LiteralCommandNode<CommandSourceStack> literalCommandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, boolean bl) {
        literalArgumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("score").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeValue((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getObjective(commandContext, "objective"), bl)))));
        literalArgumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("bossbar").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("id", ResourceLocationArgument.id()).suggests(BossBarCommands.SUGGEST_BOSS_BAR).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("value").redirect(literalCommandNode, commandContext -> ExecuteCommand.storeValue((CommandSourceStack)commandContext.getSource(), BossBarCommands.getBossBar(commandContext), true, bl)))).then(Commands.literal("max").redirect(literalCommandNode, commandContext -> ExecuteCommand.storeValue((CommandSourceStack)commandContext.getSource(), BossBarCommands.getBossBar(commandContext), false, bl)))));
        for (DataCommands.DataProvider dataProvider : DataCommands.TARGET_PROVIDERS) {
            dataProvider.wrap(literalArgumentBuilder, argumentBuilder -> argumentBuilder.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("int").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), i -> IntTag.valueOf((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))), bl))))).then(Commands.literal("float").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), i -> FloatTag.valueOf((float)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))), bl))))).then(Commands.literal("short").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), i -> ShortTag.valueOf((short)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))), bl))))).then(Commands.literal("long").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), i -> LongTag.valueOf((long)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))), bl))))).then(Commands.literal("double").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), i -> DoubleTag.valueOf((double)i * DoubleArgumentType.getDouble(commandContext, "scale")), bl))))).then(Commands.literal("byte").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalCommandNode, commandContext -> ExecuteCommand.storeData((CommandSourceStack)commandContext.getSource(), dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"), i -> ByteTag.valueOf((byte)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))), bl))))));
        }
        return literalArgumentBuilder;
    }

    private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, boolean bl) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        return commandSourceStack.withCallback((commandContext, bl2, i) -> {
            for (String string : collection) {
                Score score = scoreboard.getOrCreatePlayerScore(string, objective);
                int j = bl ? i : (bl2 ? 1 : 0);
                score.setScore(j);
            }
        }, CALLBACK_CHAINER);
    }

    private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, boolean bl, boolean bl2) {
        return commandSourceStack.withCallback((commandContext, bl3, i) -> {
            int j;
            int n = bl2 ? i : (j = bl3 ? 1 : 0);
            if (bl) {
                customBossEvent.setValue(j);
            } else {
                customBossEvent.setMax(j);
            }
        }, CALLBACK_CHAINER);
    }

    private static CommandSourceStack storeData(CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath, IntFunction<Tag> intFunction, boolean bl) {
        return commandSourceStack.withCallback((commandContext, bl2, i) -> {
            try {
                CompoundTag compoundTag = dataAccessor.getData();
                int j = bl ? i : (bl2 ? 1 : 0);
                nbtPath.set(compoundTag, (Tag)intFunction.apply(j));
                dataAccessor.setData(compoundTag);
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }, CALLBACK_CHAINER);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(CommandNode<CommandSourceStack> commandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, boolean bl, CommandBuildContext commandBuildContext) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("block").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("pos", BlockPosArgument.blockPos()).then(ExecuteCommand.addConditional(commandNode, Commands.argument("block", BlockPredicateArgument.blockPredicate(commandBuildContext)), bl, commandContext -> BlockPredicateArgument.getBlockPredicate(commandContext, "block").test(new BlockInWorld(((CommandSourceStack)commandContext.getSource()).getLevel(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), true))))))).then(Commands.literal("biome").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("pos", BlockPosArgument.blockPos()).then(ExecuteCommand.addConditional(commandNode, Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME)), bl, commandContext -> ResourceOrTagArgument.getResourceOrTag(commandContext, "biome", Registries.BIOME).test(((CommandSourceStack)commandContext.getSource()).getLevel().getBiome(BlockPosArgument.getLoadedBlockPos(commandContext, "pos")))))))).then(Commands.literal("score").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targetObjective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("=").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, Integer::equals)))))).then(Commands.literal("<").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (Integer integer, Integer integer2) -> integer < integer2)))))).then(Commands.literal("<=").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (Integer integer, Integer integer2) -> integer <= integer2)))))).then(Commands.literal(">").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (Integer integer, Integer integer2) -> integer > integer2)))))).then(Commands.literal(">=").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ExecuteCommand.addConditional(commandNode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, (Integer integer, Integer integer2) -> integer >= integer2)))))).then(Commands.literal("matches").then(ExecuteCommand.addConditional(commandNode, Commands.argument("range", RangeArgument.intRange()), bl, commandContext -> ExecuteCommand.checkScore((CommandContext<CommandSourceStack>)commandContext, RangeArgument.Ints.getRange(commandContext, "range"))))))))).then(Commands.literal("blocks").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("start", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("end", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos()).then(ExecuteCommand.addIfBlocksConditional(commandNode, Commands.literal("all"), bl, false))).then(ExecuteCommand.addIfBlocksConditional(commandNode, Commands.literal("masked"), bl, true))))))).then(Commands.literal("entity").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("entities", EntityArgument.entities()).fork(commandNode, commandContext -> ExecuteCommand.expect(commandContext, bl, !EntityArgument.getOptionalEntities(commandContext, "entities").isEmpty()))).executes(ExecuteCommand.createNumericConditionalHandler(bl, commandContext -> EntityArgument.getOptionalEntities(commandContext, "entities").size()))))).then(Commands.literal("predicate").then(ExecuteCommand.addConditional(commandNode, Commands.argument("predicate", ResourceLocationArgument.id()).suggests(SUGGEST_PREDICATE), bl, commandContext -> ExecuteCommand.checkCustomPredicate((CommandSourceStack)commandContext.getSource(), ResourceLocationArgument.getPredicate(commandContext, "predicate")))));
        for (DataCommands.DataProvider dataProvider : DataCommands.SOURCE_PROVIDERS) {
            literalArgumentBuilder.then(dataProvider.wrap(Commands.literal("data"), argumentBuilder -> argumentBuilder.then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).fork(commandNode, commandContext -> ExecuteCommand.expect(commandContext, bl, ExecuteCommand.checkMatchingData(dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path")) > 0))).executes(ExecuteCommand.createNumericConditionalHandler(bl, commandContext -> ExecuteCommand.checkMatchingData(dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path")))))));
        }
        return literalArgumentBuilder;
    }

    private static Command<CommandSourceStack> createNumericConditionalHandler(boolean bl, CommandNumericPredicate commandNumericPredicate) {
        if (bl) {
            return commandContext -> {
                int i = commandNumericPredicate.test(commandContext);
                if (i > 0) {
                    ((CommandSourceStack)commandContext.getSource()).sendSuccess(Component.translatable("commands.execute.conditional.pass_count", i), false);
                    return i;
                }
                throw ERROR_CONDITIONAL_FAILED.create();
            };
        }
        return commandContext -> {
            int i = commandNumericPredicate.test(commandContext);
            if (i == 0) {
                ((CommandSourceStack)commandContext.getSource()).sendSuccess(Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED_COUNT.create(i);
        };
    }

    private static int checkMatchingData(DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
        return nbtPath.countMatching(dataAccessor.getData());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> commandContext, BiPredicate<Integer, Integer> biPredicate) throws CommandSyntaxException {
        String string = ScoreHolderArgument.getName(commandContext, "target");
        Objective objective = ObjectiveArgument.getObjective(commandContext, "targetObjective");
        String string2 = ScoreHolderArgument.getName(commandContext, "source");
        Objective objective2 = ObjectiveArgument.getObjective(commandContext, "sourceObjective");
        ServerScoreboard scoreboard = commandContext.getSource().getServer().getScoreboard();
        if (!scoreboard.hasPlayerScore(string, objective) || !scoreboard.hasPlayerScore(string2, objective2)) {
            return false;
        }
        Score score = scoreboard.getOrCreatePlayerScore(string, objective);
        Score score2 = scoreboard.getOrCreatePlayerScore(string2, objective2);
        return biPredicate.test(score.getScore(), score2.getScore());
    }

    private static boolean checkScore(CommandContext<CommandSourceStack> commandContext, MinMaxBounds.Ints ints) throws CommandSyntaxException {
        String string = ScoreHolderArgument.getName(commandContext, "target");
        Objective objective = ObjectiveArgument.getObjective(commandContext, "targetObjective");
        ServerScoreboard scoreboard = commandContext.getSource().getServer().getScoreboard();
        if (!scoreboard.hasPlayerScore(string, objective)) {
            return false;
        }
        return ints.matches(scoreboard.getOrCreatePlayerScore(string, objective).getScore());
    }

    private static boolean checkCustomPredicate(CommandSourceStack commandSourceStack, LootItemCondition lootItemCondition) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        LootContext.Builder builder = new LootContext.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity());
        return lootItemCondition.test(builder.create(LootContextParamSets.COMMAND));
    }

    private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> commandContext, boolean bl, boolean bl2) {
        if (bl2 == bl) {
            return Collections.singleton(commandContext.getSource());
        }
        return Collections.emptyList();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(CommandNode<CommandSourceStack> commandNode, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, boolean bl, CommandPredicate commandPredicate) {
        return ((ArgumentBuilder)argumentBuilder.fork(commandNode, commandContext -> ExecuteCommand.expect(commandContext, bl, commandPredicate.test(commandContext)))).executes(commandContext -> {
            if (bl == commandPredicate.test(commandContext)) {
                ((CommandSourceStack)commandContext.getSource()).sendSuccess(Component.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw ERROR_CONDITIONAL_FAILED.create();
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(CommandNode<CommandSourceStack> commandNode, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, boolean bl, boolean bl2) {
        return ((ArgumentBuilder)argumentBuilder.fork(commandNode, commandContext -> ExecuteCommand.expect(commandContext, bl, ExecuteCommand.checkRegions(commandContext, bl2).isPresent()))).executes(bl ? commandContext -> ExecuteCommand.checkIfRegions(commandContext, bl2) : commandContext -> ExecuteCommand.checkUnlessRegions(commandContext, bl2));
    }

    private static int checkIfRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.checkRegions(commandContext, bl);
        if (optionalInt.isPresent()) {
            commandContext.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
            return optionalInt.getAsInt();
        }
        throw ERROR_CONDITIONAL_FAILED.create();
    }

    private static int checkUnlessRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.checkRegions(commandContext, bl);
        if (optionalInt.isPresent()) {
            throw ERROR_CONDITIONAL_FAILED_COUNT.create(optionalInt.getAsInt());
        }
        commandContext.getSource().sendSuccess(Component.translatable("commands.execute.conditional.pass"), false);
        return 1;
    }

    private static OptionalInt checkRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
        return ExecuteCommand.checkRegions(commandContext.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(commandContext, "start"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), bl);
    }

    private static OptionalInt checkRegions(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, boolean bl) throws CommandSyntaxException {
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
        BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos3.offset(boundingBox.getLength()));
        BlockPos blockPos4 = new BlockPos(boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ());
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > 32768) {
            throw ERROR_AREA_TOO_LARGE.create(32768, i);
        }
        int j = 0;
        for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); ++k) {
            for (int l = boundingBox.minY(); l <= boundingBox.maxY(); ++l) {
                for (int m = boundingBox.minX(); m <= boundingBox.maxX(); ++m) {
                    BlockPos blockPos5 = new BlockPos(m, l, k);
                    BlockPos blockPos6 = blockPos5.offset(blockPos4);
                    BlockState blockState = serverLevel.getBlockState(blockPos5);
                    if (bl && blockState.is(Blocks.AIR)) continue;
                    if (blockState != serverLevel.getBlockState(blockPos6)) {
                        return OptionalInt.empty();
                    }
                    BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos5);
                    BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos6);
                    if (blockEntity != null) {
                        CompoundTag compoundTag2;
                        if (blockEntity2 == null) {
                            return OptionalInt.empty();
                        }
                        if (blockEntity2.getType() != blockEntity.getType()) {
                            return OptionalInt.empty();
                        }
                        CompoundTag compoundTag = blockEntity.saveWithoutMetadata();
                        if (!compoundTag.equals(compoundTag2 = blockEntity2.saveWithoutMetadata())) {
                            return OptionalInt.empty();
                        }
                    }
                    ++j;
                }
            }
        }
        return OptionalInt.of(j);
    }

    @FunctionalInterface
    static interface CommandPredicate {
        public boolean test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface CommandNumericPredicate {
        public int test(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}

