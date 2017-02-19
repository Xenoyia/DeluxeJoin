package me.rojo8399.deluxejoin;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import me.rojo8399.placeholderapi.PlaceholderService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = DeluxeJoin.PLUGIN_ID, name = DeluxeJoin.PLUGIN_NAME, version = DeluxeJoin.PLUGIN_VERSION, authors = { "rojo8399" }, dependencies = {@Dependency(id = "placeholderapi", optional = true)})
public class DeluxeJoin {

	public static final String PLUGIN_ID = "deluxejoin";
	public static final String PLUGIN_NAME = "DeluxeJoin";
	public static final String PLUGIN_VERSION = "1.0";

	public static DeluxeJoin instance;
	public PluginContainer plugin;
	public CommentedConfigurationNode config;
	
	public PlaceholderService placeholderService = null;

	@Inject
	Logger logger;
	@Inject
	Game game;

	@Inject
	@ConfigDir(sharedRoot = false)
	public Path configDir;

	@Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent event) {
		instance = this;
		plugin = game.getPluginManager().getPlugin(PLUGIN_ID).get();
		if (!Files.exists(configDir)) {
			configDir.toFile().mkdir();
		}
		config = Util.loadFile("deluxejoin.conf");
	}
	
	@Listener
	public void onGamePostInitializationEvent(GamePostInitializationEvent event) {
		if (game.getServiceManager().provide(PlaceholderService.class).isPresent()) {
			logger.info("PlaceholderAPI found! Using advanced placeholders.");
			placeholderService = game.getServiceManager().provide(PlaceholderService.class).get();
		} else {
			logger.info("PlaceholderAPI not found! Placeholders will not be used.");
		}
	}
	
	@Listener
	public void onReload(GameReloadEvent event) {
		config = Util.loadFile("deluxejoin.conf");
	}
	
	@Listener
	public void onClientJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		try {
			TextTemplate configMessage = config.getNode("join").getValue(TypeToken.of(TextTemplate.class));
			if (!placeholderService.equals(null)) {
				String serialized = TextSerializers.JSON.serialize(Text.of(configMessage));
				Text message = TextSerializers.JSON.deserialize(placeholderService.replacePlaceholdersLegacy(player, serialized, "%", "%"));
				event.setMessage(message);
			} else {
				Text message = Text.of(configMessage);
				event.setMessage(message);
			}
		} catch (ObjectMappingException ex) {
			logger.error("Exception using join message in config: " + ex);
		}
	}
	
	@Listener
	public void onClientLeave(ClientConnectionEvent.Disconnect event) {
		Player player = event.getTargetEntity();
		try {
			TextTemplate configMessage = config.getNode("leave").getValue(TypeToken.of(TextTemplate.class));
			if (!placeholderService.equals(null)) {
				String serialized = TextSerializers.JSON.serialize(Text.of(configMessage));
				Text message = TextSerializers.JSON.deserialize(placeholderService.replacePlaceholdersLegacy(player, serialized, "%", "%"));
				event.setMessage(message);
			} else {
				Text message = Text.of(configMessage);
				event.setMessage(message);
			}
		} catch (ObjectMappingException ex) {
			logger.error("Exception using leave message in config: " + ex);
		}
	}
	
	public static DeluxeJoin getInstance() {
		return instance;
	}

	public Path getConfigDir() {
		return configDir;
	}
	
	public Logger getLogger() {
		return logger;
	}

}
