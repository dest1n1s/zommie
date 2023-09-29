package com.example.ai;

import java.util.EnumSet;

import com.example.ZommieZombieEntity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec3d;

public class PersistantJumpGoal extends Goal {

    private final ZommieZombieEntity mob;

    private final Vec3d[] directions = new Vec3d[] { new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1) };

    private Vec3d to;

    public PersistantJumpGoal(ZommieZombieEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP));
        this.to = mob.getPos();
        mob.getNavigation().setCanSwim(true);
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public boolean shouldContinue() {
        // return this.mob.getMoveControl().isMoving() && !this.mob.hasPassengers();
        return true;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.to = this.mob.getPos().add(directions[0].multiply(0.8));
        this.mob.getMoveControl().moveTo(to.getX(), to.getY(), to.getZ(), 1);
        if (this.mob.isTouchingWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getSwimHeight()
                || this.mob.isInLava()) {
            if (this.mob.getRandom().nextFloat() < 0.8f) {
                this.mob.getJumpControl().setActive();
            }
        }
    }
}
