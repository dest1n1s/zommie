package com.example.ai.control;

import com.example.ZommieZombieEntity;

import net.minecraft.registry.tag.FluidTags;

public class ZommieJumpControl extends ZommieControl {
    private boolean jumping = false;

    public ZommieJumpControl(ZommieZombieEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (!jumping) {
            return;
        }
        if (this.mob.isTouchingWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getSwimHeight()
                || this.mob.isInLava()) {
            if (this.mob.getRandom().nextFloat() < 0.8f) {
                this.mob.getJumpControl().setActive();
            }
        } else if (this.mob.isClimbing()) {
            this.mob.getJumpControl().setActive();
        } else if (this.mob.isOnGround()) {
            this.mob.getJumpControl().setActive();
        }
    }    

    public void startJumping() {
        jumping = true;
    }

    public void stopJumping() {
        jumping = false;
    }
}
