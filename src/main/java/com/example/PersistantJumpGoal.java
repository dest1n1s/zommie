package com.example;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.Vec3d;

public class PersistantJumpGoal extends Goal {

    private final ExampleZombie mob;

    private int count;

    private final Vec3d[] directions = new Vec3d[] { new Vec3d(1, 0, 0), new Vec3d(0, 0, 1), new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1) };

    private Vec3d to;

    public PersistantJumpGoal(ExampleZombie mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP));
        this.count = 0;
        this.to = mob.getPos();
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

    private void printPath(Path path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.getLength(); i++) {
            sb.append(String.format("(%d, %d, %d)", path.getNodePos(i).getX(), path.getNodePos(i).getY(),
                    path.getNodePos(i).getZ()));
            if (i != path.getLength() - 1) {
                sb.append(" -> ");
            }
        }
        System.out.println(sb.toString());
    }

    @Override
    public void start() {
        // this.to = this.mob.getPos().add(directions[count = (count + 1) %
        // 4].multiply(4));
        // Path path = this.mob.getNavigation().findPathTo(to.getX(), to.getY(),
        // to.getZ(), 1);
        // if (path == null) {
        // return;
        // }
        // this.printPath(path);
        // this.mob.getNavigation().startMovingAlong(path, 1);
        // this.mob.getMoveControl().moveTo(to.getX(), to.getY(), to.getZ(), 1);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.to = this.mob.getPos().add(directions[0].multiply(0.8));
        this.mob.getMoveControl().moveTo(to.getX(), to.getY(), to.getZ(), 1);
        if (this.mob.getRandom().nextFloat() < 0.8f) {
            this.mob.getJumpControl().setActive();
        }
    }
}
