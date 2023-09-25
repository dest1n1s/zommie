package com.example;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ExampleZombie extends ZombieEntity {

    public ExampleZombie(EntityType<? extends ExampleZombie> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new PersistantJumpGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        // Get tick count
        if (this.age % 20 == 0) {
            // Return if not server
            var world = this.getWorld();
            if (world.isClient) {
                return;
            }

            PacketByteBuf buf = PacketByteBufs.create();
            // Write entity id
            buf.writeInt(this.getId());

            // Send packet to the first player
            ServerPlayNetworking.send(PlayerLookup.tracking((ServerWorld) world, this.getChunkPos()).iterator().next(),
                    ExampleMod.RENDER_ZOMBIE_VIEW_PACKET_ID, buf);
        }
    }
}
