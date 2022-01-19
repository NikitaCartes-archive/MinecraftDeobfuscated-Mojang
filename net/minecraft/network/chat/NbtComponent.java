/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class NbtComponent
extends BaseComponent
implements ContextAwareComponent {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final boolean interpreting;
    protected final Optional<Component> separator;
    protected final String nbtPathPattern;
    @Nullable
    protected final NbtPathArgument.NbtPath compiledNbtPath;

    @Nullable
    private static NbtPathArgument.NbtPath compileNbtPath(String string) {
        try {
            return new NbtPathArgument().parse(new StringReader(string));
        } catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    public NbtComponent(String string, boolean bl, Optional<Component> optional) {
        this(string, NbtComponent.compileNbtPath(string), bl, optional);
    }

    protected NbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, Optional<Component> optional) {
        this.nbtPathPattern = string;
        this.compiledNbtPath = nbtPath;
        this.interpreting = bl;
        this.separator = optional;
    }

    protected abstract Stream<CompoundTag> getData(CommandSourceStack var1) throws CommandSyntaxException;

    public String getNbtPath() {
        return this.nbtPathPattern;
    }

    public boolean isInterpreting() {
        return this.interpreting;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandSourceStack == null || this.compiledNbtPath == null) {
            return new TextComponent("");
        }
        Stream<String> stream = this.getData(commandSourceStack).flatMap(compoundTag -> {
            try {
                return this.compiledNbtPath.get((Tag)compoundTag).stream();
            } catch (CommandSyntaxException commandSyntaxException) {
                return Stream.empty();
            }
        }).map(Tag::getAsString);
        if (this.interpreting) {
            Component component = DataFixUtils.orElse(ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR);
            return stream.flatMap(string -> {
                try {
                    MutableComponent mutableComponent = Component.Serializer.fromJson(string);
                    return Stream.of(ComponentUtils.updateForEntity(commandSourceStack, mutableComponent, entity, i));
                } catch (Exception exception) {
                    LOGGER.warn("Failed to parse component: {}", string, (Object)exception);
                    return Stream.of(new MutableComponent[0]);
                }
            }).reduce((mutableComponent, mutableComponent2) -> mutableComponent.append(component).append((Component)mutableComponent2)).orElseGet(() -> new TextComponent(""));
        }
        return ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i).map(mutableComponent -> stream.map(string -> new TextComponent((String)string)).reduce((mutableComponent2, mutableComponent3) -> mutableComponent2.append((Component)mutableComponent).append((Component)mutableComponent3)).orElseGet(() -> new TextComponent(""))).orElseGet(() -> new TextComponent(stream.collect(Collectors.joining(", "))));
    }

    public static class StorageNbtComponent
    extends NbtComponent {
        private final ResourceLocation id;

        public StorageNbtComponent(String string, boolean bl, ResourceLocation resourceLocation, Optional<Component> optional) {
            super(string, bl, optional);
            this.id = resourceLocation;
        }

        public StorageNbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, ResourceLocation resourceLocation, Optional<Component> optional) {
            super(string, nbtPath, bl, optional);
            this.id = resourceLocation;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public StorageNbtComponent plainCopy() {
            return new StorageNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.id, this.separator);
        }

        @Override
        protected Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
            CompoundTag compoundTag = commandSourceStack.getServer().getCommandStorage().get(this.id);
            return Stream.of(compoundTag);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof StorageNbtComponent) {
                StorageNbtComponent storageNbtComponent = (StorageNbtComponent)object;
                return Objects.equals(this.id, storageNbtComponent.id) && Objects.equals(this.nbtPathPattern, storageNbtComponent.nbtPathPattern) && super.equals(object);
            }
            return false;
        }

        @Override
        public String toString() {
            return "StorageNbtComponent{id='" + this.id + "'path='" + this.nbtPathPattern + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
        }

        @Override
        public /* synthetic */ BaseComponent plainCopy() {
            return this.plainCopy();
        }

        @Override
        public /* synthetic */ MutableComponent plainCopy() {
            return this.plainCopy();
        }
    }

    public static class BlockNbtComponent
    extends NbtComponent {
        private final String posPattern;
        @Nullable
        private final Coordinates compiledPos;

        public BlockNbtComponent(String string, boolean bl, String string2, Optional<Component> optional) {
            super(string, bl, optional);
            this.posPattern = string2;
            this.compiledPos = this.compilePos(this.posPattern);
        }

        @Nullable
        private Coordinates compilePos(String string) {
            try {
                return BlockPosArgument.blockPos().parse(new StringReader(string));
            } catch (CommandSyntaxException commandSyntaxException) {
                return null;
            }
        }

        private BlockNbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, String string2, @Nullable Coordinates coordinates, Optional<Component> optional) {
            super(string, nbtPath, bl, optional);
            this.posPattern = string2;
            this.compiledPos = coordinates;
        }

        @Nullable
        public String getPos() {
            return this.posPattern;
        }

        @Override
        public BlockNbtComponent plainCopy() {
            return new BlockNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.posPattern, this.compiledPos, this.separator);
        }

        @Override
        protected Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
            BlockEntity blockEntity;
            BlockPos blockPos;
            ServerLevel serverLevel;
            if (this.compiledPos != null && (serverLevel = commandSourceStack.getLevel()).isLoaded(blockPos = this.compiledPos.getBlockPos(commandSourceStack)) && (blockEntity = serverLevel.getBlockEntity(blockPos)) != null) {
                return Stream.of(blockEntity.saveWithFullMetadata());
            }
            return Stream.empty();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof BlockNbtComponent) {
                BlockNbtComponent blockNbtComponent = (BlockNbtComponent)object;
                return Objects.equals(this.posPattern, blockNbtComponent.posPattern) && Objects.equals(this.nbtPathPattern, blockNbtComponent.nbtPathPattern) && super.equals(object);
            }
            return false;
        }

        @Override
        public String toString() {
            return "BlockPosArgument{pos='" + this.posPattern + "'path='" + this.nbtPathPattern + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
        }

        @Override
        public /* synthetic */ BaseComponent plainCopy() {
            return this.plainCopy();
        }

        @Override
        public /* synthetic */ MutableComponent plainCopy() {
            return this.plainCopy();
        }
    }

    public static class EntityNbtComponent
    extends NbtComponent {
        private final String selectorPattern;
        @Nullable
        private final EntitySelector compiledSelector;

        public EntityNbtComponent(String string, boolean bl, String string2, Optional<Component> optional) {
            super(string, bl, optional);
            this.selectorPattern = string2;
            this.compiledSelector = EntityNbtComponent.compileSelector(string2);
        }

        @Nullable
        private static EntitySelector compileSelector(String string) {
            try {
                EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
                return entitySelectorParser.parse();
            } catch (CommandSyntaxException commandSyntaxException) {
                return null;
            }
        }

        private EntityNbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, String string2, @Nullable EntitySelector entitySelector, Optional<Component> optional) {
            super(string, nbtPath, bl, optional);
            this.selectorPattern = string2;
            this.compiledSelector = entitySelector;
        }

        public String getSelector() {
            return this.selectorPattern;
        }

        @Override
        public EntityNbtComponent plainCopy() {
            return new EntityNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.selectorPattern, this.compiledSelector, this.separator);
        }

        @Override
        protected Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            if (this.compiledSelector != null) {
                List<? extends Entity> list = this.compiledSelector.findEntities(commandSourceStack);
                return list.stream().map(NbtPredicate::getEntityTagToCompare);
            }
            return Stream.empty();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof EntityNbtComponent) {
                EntityNbtComponent entityNbtComponent = (EntityNbtComponent)object;
                return Objects.equals(this.selectorPattern, entityNbtComponent.selectorPattern) && Objects.equals(this.nbtPathPattern, entityNbtComponent.nbtPathPattern) && super.equals(object);
            }
            return false;
        }

        @Override
        public String toString() {
            return "EntityNbtComponent{selector='" + this.selectorPattern + "'path='" + this.nbtPathPattern + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
        }

        @Override
        public /* synthetic */ BaseComponent plainCopy() {
            return this.plainCopy();
        }

        @Override
        public /* synthetic */ MutableComponent plainCopy() {
            return this.plainCopy();
        }
    }
}

