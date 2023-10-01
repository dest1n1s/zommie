package com.example.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.mixinInterfaces.FramebufferOverrider;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements FramebufferOverrider {
	private @Nullable Framebuffer overrideFramebuffer;

	@Override
	public void setOverrideFramebuffer(@Nullable Framebuffer overrideFramebuffer) {
		this.overrideFramebuffer = overrideFramebuffer;
	}

	@Override
	public @Nullable Framebuffer getOverrideFramebuffer() {
		return overrideFramebuffer;
	}

	@Inject(at = @At("HEAD"), method = "getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;", cancellable = true)
	private void injectGetFrameBuffer(CallbackInfoReturnable<Framebuffer> info) {
		if (this.getOverrideFramebuffer() != null) {
			info.setReturnValue(this.getOverrideFramebuffer());
		}
	}
}