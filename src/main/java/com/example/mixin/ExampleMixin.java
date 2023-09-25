package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.OverrideFramebuffer;

@Mixin(MinecraftClient.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;", cancellable = true)
	private void injectGetFrameBuffer(CallbackInfoReturnable<Framebuffer> info) {
		Framebuffer overrideFrameBuffer = OverrideFramebuffer.framebuffer;
		if (overrideFrameBuffer != null) {
			info.setReturnValue(overrideFrameBuffer);
		}
	}
}