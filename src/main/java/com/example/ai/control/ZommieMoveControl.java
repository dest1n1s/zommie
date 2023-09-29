package com.example.ai.control;

import javax.annotation.Nullable;

import com.example.ZommieZombieEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ZommieMoveControl extends ZommieControl {
    private MoveControlType type = MoveControlType.WAIT;
    private @Nullable Vec3d direction = null;
    private double speed;

    public ZommieMoveControl(ZommieZombieEntity mob, double speed) {
        super(mob);
        this.speed = speed;
        mob.getNavigation().setCanSwim(true);
    }

    @Override
    public void tick() {
        switch (type) {
            case WAIT:
                break;
            case DIRECTION:
                tickDirection();
                break;
            case LOCATION:
            case TARGET:
                if (mob.getNavigation().isIdle()) {
                    type = MoveControlType.WAIT;
                }
                break;
        }
    }

    private void tickDirection() {
        var direction = this.direction;
        if (direction == null) {
            return;
        }
        Vec3d to = mob.getPos().add(direction.multiply(0.9));
        mob.getMoveControl().moveTo(to.getX(), to.getY(), to.getZ(), speed);
    }

    public void moveAlongDirection(Vec3d direction) {
        this.direction = direction;
        this.type = MoveControlType.DIRECTION;
    }

    public boolean moveToLocation(Vec3d location) {
        this.type = MoveControlType.LOCATION;
        return mob.getNavigation().startMovingTo(location.getX(), location.getY(), location.getZ(), speed);
    }

    public boolean moveToTarget() {
        this.type = MoveControlType.TARGET;
        return mob.getTarget() != null && mob.getNavigation().startMovingTo(mob.getTarget(), speed);
    }

    public void stop() {
        this.type = MoveControlType.WAIT;
        mob.getNavigation().stop();
    }

    private enum MoveControlType {
        WAIT, DIRECTION, LOCATION, TARGET
    }
}
