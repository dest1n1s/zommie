package com.example.mixin.client;

import java.util.SortedMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;

@Mixin(BufferBuilderStorage.class)
public interface BufferBuilderStorageAccessor {
    @Accessor
    SortedMap<RenderLayer, BufferBuilder> getEntityBuilders();
}
