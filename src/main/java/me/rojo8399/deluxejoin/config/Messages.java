package me.rojo8399.deluxejoin.config;

import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;

import me.rojo8399.deluxejoin.PluginInfo;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Messages {

	@ConfigSerializable
	public static class Message {

		@Setting
		public String value;

		public Message() {

		}

		public Message(String s) {
			this.value = s;
		}

		public Text t(Object... args) {
			return Messages.t(value, args);
		}

		@Override
		public String toString() {
			return value;
		}

	}
	
	@ConfigSerializable
	public static class Misc {
		@Setting
		public Message version = of("&7version");
	}
	
	@ConfigSerializable
	public static class Startup {
		@Setting
		public Message welcome = of(
				String.format("&f%s version %s, running on Sponge API %s.", PluginInfo.NAME, PluginInfo.VERSION,
						Sponge.getPlatform().getContainer(Platform.Component.API).getVersion().orElse("unknown version")));
	}
	
	private static Messages inst;
	
	public static final TypeToken<Messages> type = TypeToken.of(Messages.class);
	
	public static Messages get() {
		return inst == null ? new Messages() : inst;
	}
	
	public static void init(Messages inst) {
		Messages.inst = inst;
	}

	private static Message of(String v) {
		return new Message(v);
	}

	public static Text t(String m, Object... args) {
		return TextSerializers.FORMATTING_CODE
				.deserialize((args == null || args.length == 0 ? m : String.format(m, args)));
	}
	
	@Setting
	public Misc misc = new Misc();

	@Setting
	public Startup startup = new Startup();

}
