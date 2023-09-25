package com.example.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;

@Mixin(BlockBufferBuilderStorage.class)
public interface BlockBufferBuilderStorageAccessor {
    @Accessor
    Map<RenderLayer, BufferBuilder> getBuilders();
}
