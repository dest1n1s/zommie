package com.example.mixin.client;

import java.nio.ByteBuffer;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.systems.VertexSorter;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor("buffer")
    ByteBuffer getBuffer();

    @Accessor("builtBufferCount")
    int getBuiltBufferCount();

    @Accessor("builtBufferCount")
    void setBuiltBufferCount(int count);

    @Accessor("batchOffset")
    int getBatchOffset();

    @Accessor("batchOffset")
    void setBatchOffset(int offset);

    @Accessor("elementOffset")
    int getElementOffset();

    @Accessor("elementOffset")
    void setElementOffset(int offset);

    @Accessor("vertexCount")
    int getVertexCount();

    @Accessor("vertexCount")
    void setVertexCount(int count);

    @Accessor("currentElement")
    VertexFormatElement getCurrentElement();

    @Accessor("currentElement")
    void setCurrentElement(VertexFormatElement element);

    @Accessor("currentElementId")
    int getCurrentElementId();

    @Accessor("currentElementId")
    void setCurrentElementId(int id);

    @Accessor("format")
    VertexFormat getFormat();

    @Invoker("setFormat")
    void invokeSetFormat(VertexFormat format);

    @Accessor("drawMode")
    VertexFormat.DrawMode getDrawMode();

    @Accessor("drawMode")
    void setDrawMode(VertexFormat.DrawMode drawMode);

    @Accessor("canSkipElementChecks")
    boolean isCanSkipElementChecks();

    @Accessor("canSkipElementChecks")
    void setCanSkipElementChecks(boolean canSkip);

    @Accessor("hasOverlay")
    boolean hasOverlay();

    @Accessor("hasOverlay")
    void setHasOverlay(boolean hasOverlay);

    @Accessor("building")
    boolean isBuilding();

    @Accessor("building")
    void setBuilding(boolean building);

    @Accessor("sortingPrimitiveCenters")
    Vector3f[] getSortingPrimitiveCenters();

    @Accessor("sortingPrimitiveCenters")
    void setSortingPrimitiveCenters(Vector3f[] centers);

    @Accessor("sorter")
    VertexSorter getSorter();

    @Accessor("sorter")
    void setSorter(VertexSorter sorter);

    @Accessor("hasNoVertexBuffer")
    boolean hasNoVertexBuffer();

    @Accessor("hasNoVertexBuffer")
    void setHasNoVertexBuffer(boolean hasNoVertexBuffer);
}