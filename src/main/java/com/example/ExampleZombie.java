package com.example;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

public class ExampleZombie extends ZombieEntity {

    public ExampleZombie(EntityType<? extends ExampleZombie> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new PersistantJumpGoal(this));
    }
}
