package net.minecraft.network.chat;

import com.google.common.base.Joiner;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NbtComponent extends BaseComponent implements ContextAwareComponent {
	private static final Logger LOGGER = LogManager.getLogger();
	protected final boolean interpreting;
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

	public NbtComponent(String string, boolean bl) {
		this(string, compileNbtPath(string), bl);
	}

	protected NbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl) {
		this.nbtPathPattern = string;
		this.compiledNbtPath = nbtPath;
		this.interpreting = bl;
	}

	protected abstract Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException;

	@Override
	public String getContents() {
		return "";
	}

	public String getNbtPath() {
		return this.nbtPathPattern;
	}

	public boolean isInterpreting() {
		return this.interpreting;
	}

	@Override
	public Component resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack != null && this.compiledNbtPath != null) {
			Stream<String> stream = this.getData(commandSourceStack).flatMap(compoundTag -> {
				try {
					return this.compiledNbtPath.get(compoundTag).stream();
				} catch (CommandSyntaxException var3) {
					return Stream.empty();
				}
			}).map(Tag::getAsString);
			return (Component)(this.interpreting
				? (Component)stream.flatMap(string -> {
					try {
						Component component = Component.Serializer.fromJson(string);
						return Stream.of(ComponentUtils.updateForEntity(commandSourceStack, component, entity, i));
					} catch (Exception var5) {
						LOGGER.warn("Failed to parse component: " + string, (Throwable)var5);
						return Stream.of();
					}
				}).reduce((component, component2) -> component.append(", ").append(component2)).orElse(new TextComponent(""))
				: new TextComponent(Joiner.on(", ").join(stream.iterator())));
		} else {
			return new TextComponent("");
		}
	}

	public static class BlockNbtComponent extends NbtComponent {
		private final String posPattern;
		@Nullable
		private final Coordinates compiledPos;

		public BlockNbtComponent(String string, boolean bl, String string2) {
			super(string, bl);
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

		private BlockNbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, String string2, @Nullable Coordinates coordinates) {
			super(string, nbtPath, bl);
			this.posPattern = string2;
			this.compiledPos = coordinates;
		}

		@Nullable
		public String getPos() {
			return this.posPattern;
		}

		@Override
		public Component copy() {
			return new NbtComponent.BlockNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.posPattern, this.compiledPos);
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
			} else if (!(object instanceof NbtComponent.BlockNbtComponent)) {
				return false;
			} else {
				NbtComponent.BlockNbtComponent blockNbtComponent = (NbtComponent.BlockNbtComponent)object;
				return Objects.equals(this.posPattern, blockNbtComponent.posPattern)
					&& Objects.equals(this.nbtPathPattern, blockNbtComponent.nbtPathPattern)
					&& super.equals(object);
			}
		}

		@Override
		public String toString() {
			return "BlockPosArgument{pos='"
				+ this.posPattern
				+ '\''
				+ "path='"
				+ this.nbtPathPattern
				+ '\''
				+ ", siblings="
				+ this.siblings
				+ ", style="
				+ this.getStyle()
				+ '}';
		}
	}

	public static class EntityNbtComponent extends NbtComponent {
		private final String selectorPattern;
		@Nullable
		private final EntitySelector compiledSelector;

		public EntityNbtComponent(String string, boolean bl, String string2) {
			super(string, bl);
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

		private EntityNbtComponent(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, String string2, @Nullable EntitySelector entitySelector) {
			super(string, nbtPath, bl);
			this.selectorPattern = string2;
			this.compiledSelector = entitySelector;
		}

		public String getSelector() {
			return this.selectorPattern;
		}

		@Override
		public Component copy() {
			return new NbtComponent.EntityNbtComponent(this.nbtPathPattern, this.compiledNbtPath, this.interpreting, this.selectorPattern, this.compiledSelector);
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
			} else if (!(object instanceof NbtComponent.EntityNbtComponent)) {
				return false;
			} else {
				NbtComponent.EntityNbtComponent entityNbtComponent = (NbtComponent.EntityNbtComponent)object;
				return Objects.equals(this.selectorPattern, entityNbtComponent.selectorPattern)
					&& Objects.equals(this.nbtPathPattern, entityNbtComponent.nbtPathPattern)
					&& super.equals(object);
			}
		}

		@Override
		public String toString() {
			return "EntityNbtComponent{selector='"
				+ this.selectorPattern
				+ '\''
				+ "path='"
				+ this.nbtPathPattern
				+ '\''
				+ ", siblings="
				+ this.siblings
				+ ", style="
				+ this.getStyle()
				+ '}';
		}
	}
}
