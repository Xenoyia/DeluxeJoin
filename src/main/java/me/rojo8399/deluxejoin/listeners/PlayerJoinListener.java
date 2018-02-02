package me.rojo8399.deluxejoin.listeners;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import me.rojo8399.deluxejoin.DeluxeJoinPlugin;

public class PlayerJoinListener {

	DeluxeJoinPlugin plugin;

	public PlayerJoinListener(DeluxeJoinPlugin plugin) {
		this.plugin = plugin;
	}

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
		if (plugin.getPlaceholderService().isPresent())
			event.setMessage(plugin.getPlaceholderService().get().replacePlaceholders(TextSerializers.JSON.deserialize(
					"[{\"text\":\"Hello %player% !\",\"color\":\"aqua\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"hi\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"I lovu \",\"color\":\"dark_blue\"}]}},\"insertion\":\"Insertion thingy\"}]"),
					player, null));
		else
			event.setMessage(Text.of("Sup " + player.getName()));
	}

}
