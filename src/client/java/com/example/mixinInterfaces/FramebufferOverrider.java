package com.example.mixinInterfaces;

import javax.annotation.Nullable;

import net.minecraft.client.gl.Framebuffer;

public interface FramebufferOverrider {
    public void setOverrideFramebuffer(@Nullable Framebuffer framebuffer);

    public @Nullable Framebuffer getOverrideFramebuffer();
}
