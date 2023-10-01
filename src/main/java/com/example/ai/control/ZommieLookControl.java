package com.example.ai.control;

import javax.annotation.Nullable;

import com.example.ZommieZombieEntity;

import net.minecraft.util.math.Vec3d;

public class ZommieLookControl extends ZommieControl {
    private LookControlType type = LookControlType.RESET;
    private @Nullable Vec3d direction = null;
    private @Nullable Vec3d location = null;

    public ZommieLookControl(ZommieZombieEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        switch (type) {
            case DIRECTION:
                tickDirection();
                break;
            case LOCATION:
                tickLocation();
                break;
            case TARGET:
                tickTarget();
                break;
            case RESET:
                tickReset();
                break;
            default:
                break;
        }
    }

    private void tickDirection() {
        var direction = this.direction;
        if (direction == null) {
            return;
        }
        Vec3d to = mob.getEyePos().add(direction.multiply(2));
        mob.getLookControl().lookAt(to);
    }

    private void tickLocation() {
        var location = this.location;
        if (location == null) {
            return;
        }
        mob.getLookControl().lookAt(location);
    }

    private void tickTarget() {
        var target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        mob.getLookControl().lookAt(target, 10, 10);
    }

    private void tickReset() {
        var direction = mob.getRotationVec(1.0f);
        Vec3d to = mob.getEyePos().add(direction.multiply(2));
        mob.getLookControl().lookAt(to);
    }

    public void lookAtDirection(Vec3d direction) {
        this.direction = direction;
        this.type = LookControlType.DIRECTION;
    }

    public void lookAtLocation(Vec3d location) {
        this.location = location;
        this.type = LookControlType.LOCATION;
    }

    public boolean lookAtTarget() {
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            return false;
        }
        this.type = LookControlType.TARGET;
        return true;
    }

    public void reset() {
        this.type = LookControlType.RESET;
    }
    
    private enum LookControlType {
        DIRECTION,
        LOCATION,
        TARGET,
        RESET
    }
}
