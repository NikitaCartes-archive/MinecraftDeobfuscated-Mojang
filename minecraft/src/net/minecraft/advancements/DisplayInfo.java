package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
	public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemStack.CODEC.fieldOf("icon").forGetter(DisplayInfo::getIcon),
					ComponentSerialization.CODEC.fieldOf("title").forGetter(DisplayInfo::getTitle),
					ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::getDescription),
					ResourceLocation.CODEC.optionalFieldOf("background").forGetter(DisplayInfo::getBackground),
					AdvancementType.CODEC.optionalFieldOf("frame", AdvancementType.TASK).forGetter(DisplayInfo::getType),
					Codec.BOOL.optionalFieldOf("show_toast", Boolean.valueOf(true)).forGetter(DisplayInfo::shouldShowToast),
					Codec.BOOL.optionalFieldOf("announce_to_chat", Boolean.valueOf(true)).forGetter(DisplayInfo::shouldAnnounceChat),
					Codec.BOOL.optionalFieldOf("hidden", Boolean.valueOf(false)).forGetter(DisplayInfo::isHidden)
				)
				.apply(instance, DisplayInfo::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(
		DisplayInfo::serializeToNetwork, DisplayInfo::fromNetwork
	);
	private final Component title;
	private final Component description;
	private final ItemStack icon;
	private final Optional<ResourceLocation> background;
	private final AdvancementType type;
	private final boolean showToast;
	private final boolean announceChat;
	private final boolean hidden;
	private float x;
	private float y;

	public DisplayInfo(
		ItemStack itemStack,
		Component component,
		Component component2,
		Optional<ResourceLocation> optional,
		AdvancementType advancementType,
		boolean bl,
		boolean bl2,
		boolean bl3
	) {
		this.title = component;
		this.description = component2;
		this.icon = itemStack;
		this.background = optional;
		this.type = advancementType;
		this.showToast = bl;
		this.announceChat = bl2;
		this.hidden = bl3;
	}

	public void setLocation(float f, float g) {
		this.x = f;
		this.y = g;
	}

	public Component getTitle() {
		return this.title;
	}

	public Component getDescription() {
		return this.description;
	}

	public ItemStack getIcon() {
		return this.icon;
	}

	public Optional<ResourceLocation> getBackground() {
		return this.background;
	}

	public AdvancementType getType() {
		return this.type;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public boolean shouldShowToast() {
		return this.showToast;
	}

	public boolean shouldAnnounceChat() {
		return this.announceChat;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	private void serializeToNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.title);
		ComponentSerialization.TRUSTED_STREAM_CODEC.encode(registryFriendlyByteBuf, this.description);
		ItemStack.STREAM_CODEC.encode(registryFriendlyByteBuf, this.icon);
		registryFriendlyByteBuf.writeEnum(this.type);
		int i = 0;
		if (this.background.isPresent()) {
			i |= 1;
		}

		if (this.showToast) {
			i |= 2;
		}

		if (this.hidden) {
			i |= 4;
		}

		registryFriendlyByteBuf.writeInt(i);
		this.background.ifPresent(registryFriendlyByteBuf::writeResourceLocation);
		registryFriendlyByteBuf.writeFloat(this.x);
		registryFriendlyByteBuf.writeFloat(this.y);
	}

	private static DisplayInfo fromNetwork(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		Component component = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
		Component component2 = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(registryFriendlyByteBuf);
		ItemStack itemStack = ItemStack.STREAM_CODEC.decode(registryFriendlyByteBuf);
		AdvancementType advancementType = registryFriendlyByteBuf.readEnum(AdvancementType.class);
		int i = registryFriendlyByteBuf.readInt();
		Optional<ResourceLocation> optional = (i & 1) != 0 ? Optional.of(registryFriendlyByteBuf.readResourceLocation()) : Optional.empty();
		boolean bl = (i & 2) != 0;
		boolean bl2 = (i & 4) != 0;
		DisplayInfo displayInfo = new DisplayInfo(itemStack, component, component2, optional, advancementType, bl, false, bl2);
		displayInfo.setLocation(registryFriendlyByteBuf.readFloat(), registryFriendlyByteBuf.readFloat());
		return displayInfo;
	}
}
