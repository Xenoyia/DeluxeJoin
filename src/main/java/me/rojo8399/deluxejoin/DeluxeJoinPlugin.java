package me.rojo8399.deluxejoin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.rojo8399.deluxejoin.commands.VersionCommand;
import me.rojo8399.deluxejoin.config.Config;
import me.rojo8399.deluxejoin.config.Messages;
import me.rojo8399.deluxejoin.listeners.PlayerJoinListener;
import me.rojo8399.placeholderapi.PlaceholderService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, dependencies = {
		@Dependency(id = "placeholderapi", optional = true, version = "[4.4,)") })
public class DeluxeJoinPlugin {

	private static DeluxeJoinPlugin instance;

	public static DeluxeJoinPlugin getInstance() {
		return instance;
	}

	private Config config;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Inject
	private Logger logger;
	@Inject
	private Game game;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	private ConfigurationLoader<CommentedConfigurationNode> msgloader;
	private ConfigurationNode msgRoot;
	private Messages msgs;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path path;
	@Inject
	private PluginContainer plugin;

	private ConfigurationNode root;

	private Optional<PlaceholderService> placeholderService;

	public Logger getLogger() {
		return logger;
	}

	public Path getConfigDirPath() {
		return configDir;
	}

	public Optional<PlaceholderService> getPlaceholderService() {
		return placeholderService;
	}

	private ConfigurationNode loadDefault() throws IOException {
		return HoconConfigurationLoader.builder()
				.setURL(game.getAssetManager().getAsset(this, "main.conf").get().getUrl()).build()
				.load(loader.getDefaultOptions());
	}

	private void mapDefault() throws IOException, ObjectMappingException {
		try {
			config = (root = loadDefault()).getValue(Config.type);
		} catch (IOException | ObjectMappingException ex) {
			logger.error("Could not load the embedded default config! Disabling plugin.");
			game.getEventManager().unregisterPluginListeners(this);
			throw ex;
		}
	}

	@Listener
	public void onPreInit(GamePreInitializationEvent preinitializationEvent)
			throws IOException, ObjectMappingException {
		instance = this;
		plugin = game.getPluginManager().getPlugin(PluginInfo.ID).get();
		Asset conf = game.getAssetManager().getAsset(this, "config.conf").get();
		// Load configuration
		if (!Files.exists(path)) {
			try {
				conf.copyToFile(path);
				logger.info("Coppied the config file to: " + path);
			} catch (IOException ex) {
				logger.error("Could not copy the config file!");
				try {
					throw ex;
				} finally {
					mapDefault();
				}
			}

		}
		try {
			root = loader.load();
		} catch (IOException ex) {
			logger.error("Could not load the config file!");
			try {
				throw ex;
			} finally {
				mapDefault();
			}
		}
		try {
			config = root.getValue(Config.type, new Config());
		} catch (ObjectMappingException ex) {
			logger.error("Invalid config file!");
			try {
				throw ex;
			} finally {
				mapDefault();
			}
		}
		if (config == null) {
			config = new Config();
			this.root.setValue(Config.type, config);
			try {
				this.loader.save(root);
			} catch (Exception e) {
			}
		}
		File msgFile = new File(configDir.toFile(), "messages.conf");
		msgloader = HoconConfigurationLoader.builder().setFile(msgFile).build();
		try {
			msgs = (msgRoot = msgloader.load()).getValue(Messages.type);
			if (msgs == null) {
				msgs = new Messages();
				msgRoot.setValue(Messages.type, msgs);
				msgloader.save(msgRoot);
			}
		} catch (ObjectMappingException ex) {
			logger.error("Invalid messages file!");
			try {
				throw ex;
			} finally {
				msgs = new Messages();
				msgRoot.setValue(Messages.type, msgs);
				msgloader.save(msgRoot);
			}
		}
		Messages.init(msgs);
	}

	@Listener
	public void onInit(GameInitializationEvent event) {
		logger.info(String.format("%s is now entering the init phase.", PluginInfo.NAME));
		// Register Listeners and Commands

		Sponge.getEventManager().registerListeners(this, new PlayerJoinListener(instance));

		String p = PluginInfo.ID;
		CommandSpec versionCmd = CommandSpec.builder().permission(p + ".admin").executor(new VersionCommand()).build();

		// /deluxejoin - /djoin
		CommandSpec baseCmd = CommandSpec.builder().executor(new CommandExecutor() {
			// send plugin name + version
			@Override
			public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
				src.sendMessage(Text.of(TextColors.GREEN, PluginInfo.NAME, TextColors.GRAY, " ",
						Messages.get().misc.version.t(), " ", TextColors.AQUA, PluginInfo.VERSION, TextColors.GRAY,
						"."));
				return CommandResult.success();
			}
		}).child(versionCmd, "version", "v").build();
		game.getCommandManager().register(plugin, baseCmd, "deluxejoin", "djoin");
	}

}
