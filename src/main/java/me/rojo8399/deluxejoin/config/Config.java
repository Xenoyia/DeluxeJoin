package me.rojo8399.deluxejoin.config;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {
	public static final TypeToken<Config> type = TypeToken.of(Config.class);
	
	@Setting("config-version")
	public String version = "2";

}
