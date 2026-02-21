package dev.imb11.skinshuffle.mixin;

import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(com.mojang.authlib.GameProfile.class)
public interface GameProfileAccessor {
	@Accessor
	PropertyMap getProperties();

	@Mutable
	@Accessor
	void setProperties(PropertyMap properties);
}
