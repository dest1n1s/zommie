package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.profiler.ProfileResult;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor
    ProfileResult getTickProfilerResult();

    @Invoker("drawProfilerResults")
    public void invokeDrawProfilerResults(DrawContext context, ProfileResult profileResult);

    @Accessor("framebuffer")
    public void setFramebuffer(Framebuffer framebuffer);
}