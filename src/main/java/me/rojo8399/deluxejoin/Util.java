package me.rojo8399.deluxejoin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.spongepowered.api.Sponge;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class Util {
	
	private static DeluxeJoin instance = DeluxeJoin.getInstance();
	
	/**
	 * Loads a HOCON file and if it does not exist it is looked for in the JAR
	 * 
	 * @param fileLoc
	 * @return
	 */
	public static CommentedConfigurationNode loadFile(String fileLoc) {

		Path configFile = Paths.get(instance.getConfigDir() + File.separator + fileLoc);
		ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
		CommentedConfigurationNode configNode = null;

		if (!Files.exists(configFile)) {
			try {
				instance.getLogger().info("No " + fileLoc + " found. Creating it...");
				if (Sponge.getAssetManager().getAsset(instance, fileLoc).isPresent()) {
					instance.getLogger().info("Using default found in jar file.");
					Sponge.getAssetManager().getAsset(instance, fileLoc).get().copyToFile(configFile);
					configNode = configLoader.load();
				} else {
					Files.createFile(configFile);
					configNode = configLoader.load();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				configNode = configLoader.load();
			} catch (IOException e) {
				instance.getLogger().info(fileLoc + " failed to load. Invalid! " + e);
			}
		}
		
		return configNode;
	}

	/**
	 * Saves a HOCON file
	 * 
	 * @param file
	 * @param fileLocation
	 */
	public static void saveFile(CommentedConfigurationNode node, String fileLocation) {
		
		Path configFile = Paths.get(instance.getConfigDir() + File.separator + fileLocation);
		ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
		
		try {
			configLoader.save(node);
			instance.getLogger().debug("Saved " + fileLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
