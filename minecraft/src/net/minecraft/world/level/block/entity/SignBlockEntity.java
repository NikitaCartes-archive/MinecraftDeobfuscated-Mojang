package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SignBlockEntity extends BlockEntity {
	public final Component[] messages = new Component[]{new TextComponent(""), new TextComponent(""), new TextComponent(""), new TextComponent("")};
	@Environment(EnvType.CLIENT)
	private boolean showCursor;
	private int selectedLine = -1;
	private int cursorPos = -1;
	private int selectionPos = -1;
	private boolean isEditable = true;
	private Player playerWhoMayEdit;
	private final String[] renderMessages = new String[4];
	private DyeColor color = DyeColor.BLACK;

	public SignBlockEntity() {
		super(BlockEntityType.SIGN);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);

		for (int i = 0; i < 4; i++) {
			String string = Component.Serializer.toJson(this.messages[i]);
			compoundTag.putString("Text" + (i + 1), string);
		}

		compoundTag.putString("Color", this.color.getName());
		return compoundTag;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		this.isEditable = false;
		super.load(compoundTag);
		this.color = DyeColor.byName(compoundTag.getString("Color"), DyeColor.BLACK);

		for (int i = 0; i < 4; i++) {
			String string = compoundTag.getString("Text" + (i + 1));
			Component component = Component.Serializer.fromJson(string.isEmpty() ? "\"\"" : string);
			if (this.level instanceof ServerLevel) {
				try {
					this.messages[i] = ComponentUtils.updateForEntity(this.createCommandSourceStack(null), component, null, 0);
				} catch (CommandSyntaxException var6) {
					this.messages[i] = component;
				}
			} else {
				this.messages[i] = component;
			}

			this.renderMessages[i] = null;
		}
	}

	@Environment(EnvType.CLIENT)
	public Component getMessage(int i) {
		return this.messages[i];
	}

	public void setMessage(int i, Component component) {
		this.messages[i] = component;
		this.renderMessages[i] = null;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public String getRenderMessage(int i, Function<Component, String> function) {
		if (this.renderMessages[i] == null && this.messages[i] != null) {
			this.renderMessages[i] = (String)function.apply(this.messages[i]);
		}

		return this.renderMessages[i];
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 9, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	public boolean isEditable() {
		return this.isEditable;
	}

	@Environment(EnvType.CLIENT)
	public void setEditable(boolean bl) {
		this.isEditable = bl;
		if (!bl) {
			this.playerWhoMayEdit = null;
		}
	}

	public void setAllowedPlayerEditor(Player player) {
		this.playerWhoMayEdit = player;
	}

	public Player getPlayerWhoMayEdit() {
		return this.playerWhoMayEdit;
	}

	public boolean executeClickCommands(Player player) {
		for (Component component : this.messages) {
			Style style = component == null ? null : component.getStyle();
			if (style != null && style.getClickEvent() != null) {
				ClickEvent clickEvent = style.getClickEvent();
				if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
					player.getServer().getCommands().performCommand(this.createCommandSourceStack((ServerPlayer)player), clickEvent.getValue());
				}
			}
		}

		return true;
	}

	public CommandSourceStack createCommandSourceStack(@Nullable ServerPlayer serverPlayer) {
		String string = serverPlayer == null ? "Sign" : serverPlayer.getName().getString();
		Component component = (Component)(serverPlayer == null ? new TextComponent("Sign") : serverPlayer.getDisplayName());
		return new CommandSourceStack(
			CommandSource.NULL,
			new Vec3((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5),
			Vec2.ZERO,
			(ServerLevel)this.level,
			2,
			string,
			component,
			this.level.getServer(),
			serverPlayer
		);
	}

	public DyeColor getColor() {
		return this.color;
	}

	public boolean setColor(DyeColor dyeColor) {
		if (dyeColor != this.getColor()) {
			this.color = dyeColor;
			this.setChanged();
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
			return true;
		} else {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public void setCursorInfo(int i, int j, int k, boolean bl) {
		this.selectedLine = i;
		this.cursorPos = j;
		this.selectionPos = k;
		this.showCursor = bl;
	}

	@Environment(EnvType.CLIENT)
	public void resetCursorInfo() {
		this.selectedLine = -1;
		this.cursorPos = -1;
		this.selectionPos = -1;
		this.showCursor = false;
	}

	@Environment(EnvType.CLIENT)
	public boolean isShowCursor() {
		return this.showCursor;
	}

	@Environment(EnvType.CLIENT)
	public int getSelectedLine() {
		return this.selectedLine;
	}

	@Environment(EnvType.CLIENT)
	public int getCursorPos() {
		return this.cursorPos;
	}

	@Environment(EnvType.CLIENT)
	public int getSelectionPos() {
		return this.selectionPos;
	}
}
