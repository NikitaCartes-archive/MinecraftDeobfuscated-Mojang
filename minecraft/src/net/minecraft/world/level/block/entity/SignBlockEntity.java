package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SignBlockEntity extends BlockEntity {
	public static final int LINES = 4;
	private static final String[] RAW_TEXT_FIELD_NAMES = new String[]{"Text1", "Text2", "Text3", "Text4"};
	private static final String[] FILTERED_TEXT_FIELD_NAMES = new String[]{"FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4"};
	private final Component[] messages = new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
	private final Component[] filteredMessages = new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
	private boolean isEditable = true;
	@Nullable
	private UUID playerWhoMayEdit;
	@Nullable
	private FormattedCharSequence[] renderMessages;
	private boolean renderMessagedFiltered;
	private DyeColor color = DyeColor.BLACK;
	private boolean hasGlowingText;

	public SignBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SIGN, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);

		for (int i = 0; i < 4; i++) {
			Component component = this.messages[i];
			String string = Component.Serializer.toJson(component);
			compoundTag.putString(RAW_TEXT_FIELD_NAMES[i], string);
			Component component2 = this.filteredMessages[i];
			if (!component2.equals(component)) {
				compoundTag.putString(FILTERED_TEXT_FIELD_NAMES[i], Component.Serializer.toJson(component2));
			}
		}

		compoundTag.putString("Color", this.color.getName());
		compoundTag.putBoolean("GlowingText", this.hasGlowingText);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		this.isEditable = false;
		super.load(compoundTag);
		this.color = DyeColor.byName(compoundTag.getString("Color"), DyeColor.BLACK);

		for (int i = 0; i < 4; i++) {
			String string = compoundTag.getString(RAW_TEXT_FIELD_NAMES[i]);
			Component component = this.loadLine(string);
			this.messages[i] = component;
			String string2 = FILTERED_TEXT_FIELD_NAMES[i];
			if (compoundTag.contains(string2, 8)) {
				this.filteredMessages[i] = this.loadLine(compoundTag.getString(string2));
			} else {
				this.filteredMessages[i] = component;
			}
		}

		this.renderMessages = null;
		this.hasGlowingText = compoundTag.getBoolean("GlowingText");
	}

	private Component loadLine(String string) {
		Component component = this.deserializeTextSafe(string);
		if (this.level instanceof ServerLevel) {
			try {
				return ComponentUtils.updateForEntity(this.createCommandSourceStack(null), component, null, 0);
			} catch (CommandSyntaxException var4) {
			}
		}

		return component;
	}

	private Component deserializeTextSafe(String string) {
		try {
			Component component = Component.Serializer.fromJson(string);
			if (component != null) {
				return component;
			}
		} catch (Exception var3) {
		}

		return CommonComponents.EMPTY;
	}

	public Component getMessage(int i, boolean bl) {
		return this.getMessages(bl)[i];
	}

	public void setMessage(int i, Component component) {
		this.setMessage(i, component, component);
	}

	public void setMessage(int i, Component component, Component component2) {
		this.messages[i] = component;
		this.filteredMessages[i] = component2;
		this.renderMessages = null;
	}

	public FormattedCharSequence[] getRenderMessages(boolean bl, Function<Component, FormattedCharSequence> function) {
		if (this.renderMessages == null || this.renderMessagedFiltered != bl) {
			this.renderMessagedFiltered = bl;
			this.renderMessages = new FormattedCharSequence[4];

			for (int i = 0; i < 4; i++) {
				this.renderMessages[i] = (FormattedCharSequence)function.apply(this.getMessage(i, bl));
			}
		}

		return this.renderMessages;
	}

	private Component[] getMessages(boolean bl) {
		return bl ? this.filteredMessages : this.messages;
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	public boolean isEditable() {
		return this.isEditable;
	}

	public void setEditable(boolean bl) {
		this.isEditable = bl;
		if (!bl) {
			this.playerWhoMayEdit = null;
		}
	}

	public void setAllowedPlayerEditor(UUID uUID) {
		this.playerWhoMayEdit = uUID;
	}

	@Nullable
	public UUID getPlayerWhoMayEdit() {
		return this.playerWhoMayEdit;
	}

	public boolean executeClickCommands(ServerPlayer serverPlayer) {
		for (Component component : this.getMessages(serverPlayer.isTextFilteringEnabled())) {
			Style style = component.getStyle();
			ClickEvent clickEvent = style.getClickEvent();
			if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
				serverPlayer.getServer().getCommands().performCommand(this.createCommandSourceStack(serverPlayer), clickEvent.getValue());
			}
		}

		return true;
	}

	public CommandSourceStack createCommandSourceStack(@Nullable ServerPlayer serverPlayer) {
		String string = serverPlayer == null ? "Sign" : serverPlayer.getName().getString();
		Component component = (Component)(serverPlayer == null ? Component.literal("Sign") : serverPlayer.getDisplayName());
		return new CommandSourceStack(
			CommandSource.NULL, Vec3.atCenterOf(this.worldPosition), Vec2.ZERO, (ServerLevel)this.level, 2, string, component, this.level.getServer(), serverPlayer
		);
	}

	public DyeColor getColor() {
		return this.color;
	}

	public boolean setColor(DyeColor dyeColor) {
		if (dyeColor != this.getColor()) {
			this.color = dyeColor;
			this.markUpdated();
			return true;
		} else {
			return false;
		}
	}

	public boolean hasGlowingText() {
		return this.hasGlowingText;
	}

	public boolean setHasGlowingText(boolean bl) {
		if (this.hasGlowingText != bl) {
			this.hasGlowingText = bl;
			this.markUpdated();
			return true;
		} else {
			return false;
		}
	}

	private void markUpdated() {
		this.setChanged();
		this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
	}
}
