package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NbtComponent extends BaseComponent implements ContextAwareComponent {
	private static final Logger LOGGER = LogManager.getLogger();
	protected final boolean interpreting;
	protected final Optional<Component> separator;
	protected final String nbtPathPattern;
	@Nullable
	protected final NbtPathArgument.NbtPath compiledNbtPath;

	@Nullable
	private static NbtPathArgument.NbtPath compileNbtPath(String string) {
		try {
			return new NbtPathArgument().parse(new StringReader(string));
		} catch (CommandSyntaxException var2) {
			return null;
		}
	}

	public NbtComponent(String string, boolean bl, Optional<Component> optional) {
		this(string, compileNbtPath(string), bl, optional);
	}

	protected NbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, Optional<Component> optional) {
		this.nbtPathPattern = string;
		this.compiledNbtPath = nbtPath;
		this.interpreting = bl;
		this.separator = optional;
	}

	protected abstract Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException;

	public String getNbtPath() {
		return this.nbtPathPattern;
	}

	public boolean isInterpreting() {
		return this.interpreting;
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack != null && this.compiledNbtPath != null) {
			Stream<String> stream = this.getData(commandSourceStack).flatMap(compoundTag -> {
				try {
					return this.compiledNbtPath.get(compoundTag).stream();
				} catch (CommandSyntaxException var3) {
					return Stream.empty();
				}
			}).map(Tag::getAsString);
			if (this.interpreting) {
				Component component = DataFixUtils.orElse(
					ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR
				);
				return (MutableComponent)stream.flatMap(string -> {
					try {
						MutableComponent mutableComponent = Component.Serializer.fromJson(string);
						return Stream.of(ComponentUtils.updateForEntity(commandSourceStack, mutableComponent, entity, i));
					} catch (Exception var5x) {
						LOGGER.warn("Failed to parse component: {}", string, var5x);
						return Stream.of();
					}
				}).reduce((mutableComponent, mutableComponent2) -> mutableComponent.append(component).append(mutableComponent2)).orElseGet(() -> new TextComponent(""));
			} else {
				return (MutableComponent)ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i)
					.map(
						mutableComponent -> (MutableComponent)stream.map(string -> new TextComponent(string))
								.reduce((mutableComponent2, mutableComponent3) -> mutableComponent2.append(mutableComponent).append(mutableComponent3))
								.orElseGet(() -> new TextComponent(""))
					)
					.orElseGet(() -> new TextComponent((String)stream.collect(Collectors.joining(", "))));
			}
		} else {
			return new TextComponent("");
		}
	}

	public static class BlockNbtComponent extends NbtComponent {
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
			} catch (CommandSyntaxException var3) {
				return null;
			}
		}

		private BlockNbtComponent(
			String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, String string2, @Nullable Coordinates coordinates, Optional<Component> optional
		) {
			super(string, nbtPath, bl, optional);
			this.posPattern = string2;
			this.compiledPos = coordinates;
		}

		@Nullable
		public String getPos() {
			return this.posPattern;
		}

		public NbtComponent.BlockNbtComponent plainCopy() {
			return new NbtComponent.BlockNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.posPattern, this.compiledPos, this.separator);
		}

		@Override
		protected Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
			if (this.compiledPos != null) {
				ServerLevel serverLevel = commandSourceStack.getLevel();
				BlockPos blockPos = this.compiledPos.getBlockPos(commandSourceStack);
				if (serverLevel.isLoaded(blockPos)) {
					BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
					if (blockEntity != null) {
						return Stream.of(blockEntity.save(new CompoundTag()));
					}
				}
			}

			return Stream.empty();
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				return !(object instanceof NbtComponent.BlockNbtComponent blockNbtComponent)
					? false
					: Objects.equals(this.posPattern, blockNbtComponent.posPattern)
						&& Objects.equals(this.nbtPathPattern, blockNbtComponent.nbtPathPattern)
						&& super.equals(object);
			}
		}

		@Override
		public String toString() {
			return "BlockPosArgument{pos='" + this.posPattern + "'path='" + this.nbtPathPattern + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
		}
	}

	public static class EntityNbtComponent extends NbtComponent {
		private final String selectorPattern;
		@Nullable
		private final EntitySelector compiledSelector;

		public EntityNbtComponent(String string, boolean bl, String string2, Optional<Component> optional) {
			super(string, bl, optional);
			this.selectorPattern = string2;
			this.compiledSelector = compileSelector(string2);
		}

		@Nullable
		private static EntitySelector compileSelector(String string) {
			try {
				EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
				return entitySelectorParser.parse();
			} catch (CommandSyntaxException var2) {
				return null;
			}
		}

		private EntityNbtComponent(
			String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, String string2, @Nullable EntitySelector entitySelector, Optional<Component> optional
		) {
			super(string, nbtPath, bl, optional);
			this.selectorPattern = string2;
			this.compiledSelector = entitySelector;
		}

		public String getSelector() {
			return this.selectorPattern;
		}

		public NbtComponent.EntityNbtComponent plainCopy() {
			return new NbtComponent.EntityNbtComponent(
				this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.selectorPattern, this.compiledSelector, this.separator
			);
		}

		@Override
		protected Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			if (this.compiledSelector != null) {
				List<? extends Entity> list = this.compiledSelector.findEntities(commandSourceStack);
				return list.stream().map(NbtPredicate::getEntityTagToCompare);
			} else {
				return Stream.empty();
			}
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				return !(object instanceof NbtComponent.EntityNbtComponent entityNbtComponent)
					? false
					: Objects.equals(this.selectorPattern, entityNbtComponent.selectorPattern)
						&& Objects.equals(this.nbtPathPattern, entityNbtComponent.nbtPathPattern)
						&& super.equals(object);
			}
		}

		@Override
		public String toString() {
			return "EntityNbtComponent{selector='"
				+ this.selectorPattern
				+ "'path='"
				+ this.nbtPathPattern
				+ "', siblings="
				+ this.siblings
				+ ", style="
				+ this.getStyle()
				+ "}";
		}
	}

	public static class StorageNbtComponent extends NbtComponent {
		private final ResourceLocation id;

		public StorageNbtComponent(String string, boolean bl, ResourceLocation resourceLocation, Optional<Component> optional) {
			super(string, bl, optional);
			this.id = resourceLocation;
		}

		public StorageNbtComponent(
			String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, ResourceLocation resourceLocation, Optional<Component> optional
		) {
			super(string, nbtPath, bl, optional);
			this.id = resourceLocation;
		}

		public ResourceLocation getId() {
			return this.id;
		}

		public NbtComponent.StorageNbtComponent plainCopy() {
			return new NbtComponent.StorageNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.id, this.separator);
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
			} else {
				return !(object instanceof NbtComponent.StorageNbtComponent storageNbtComponent)
					? false
					: Objects.equals(this.id, storageNbtComponent.id) && Objects.equals(this.nbtPathPattern, storageNbtComponent.nbtPathPattern) && super.equals(object);
			}
		}

		@Override
		public String toString() {
			return "StorageNbtComponent{id='" + this.id + "'path='" + this.nbtPathPattern + "', siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
		}
	}
}
