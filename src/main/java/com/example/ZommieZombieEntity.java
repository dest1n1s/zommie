package com.example;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ZommieZombieEntity extends ZombieEntity {

    public ZommieZombieEntity(EntityType<? extends ZommieZombieEntity> entityType, World world) {
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
            var playerIterator = PlayerLookup.tracking((ServerWorld) world, this.getBlockPos()).iterator();
            if (!playerIterator.hasNext()) {
                return;
            }
            ServerPlayNetworking.send(playerIterator.next(), Zommie.RENDER_ZOMBIE_VIEW_PACKET_ID, buf);
        }
    }
}
