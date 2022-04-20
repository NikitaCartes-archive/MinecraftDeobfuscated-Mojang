package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

public interface ComponentContents {
	ComponentContents EMPTY = new ComponentContents() {
		public String toString() {
			return "empty";
		}
	};

	default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
		return Optional.empty();
	}

	default <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
		return Optional.empty();
	}

	default MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		return MutableComponent.create(this);
	}
}
