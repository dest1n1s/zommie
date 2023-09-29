package com.example.ai;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.example.ZommieZombieEntity;
import com.example.ai.control.ZommieMoveControl;
import com.example.proto.Action;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

public class ActionControlledGoal extends Goal {
    private final ZommieZombieEntity mob;
    private int noActionTimeout = 0;
    private ZommieMoveControl moveControl;

    public ActionControlledGoal(ZommieZombieEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Goal.Control.LOOK, Goal.Control.TARGET));
        this.moveControl = new ZommieMoveControl(mob, 1);
    }

    @Override
    public boolean canStart() {
        return noActionTimeout > 0;
    }

    public void executeAction(Action action) {
        noActionTimeout = 20;
        switch (action.getType()) {
            case MOVE:
                executeMoveAction(action.getParams());
                break;
            case ATTACK:
                executeAttackAction(action.getParams());
                break;
            case STOP:
                executeIdleAction(action.getParams());
                break;
            case LOOK:
                executeLookAction(action.getParams());
                break;
            case TARGET:
                executeTargetAction(action.getParams());
                break;
            default:
                break;
        }
    }

    private boolean executeMoveAction(com.google.protobuf.Struct actionParams) {
        if (actionParams.containsFields("direction")) {
            Vec3d direction;
            try {
                direction = convertProtobufValueToVec3d(actionParams.getFieldsOrThrow("direction"));
            } catch (Exception e) {
                return false;
            }
            moveControl.setDirection(direction);
            return true;
        } else if (actionParams.containsFields("location")) {
            Vec3d location;
            try {
                location = convertProtobufValueToVec3d(actionParams.getFieldsOrThrow("location"));
            } catch (Exception e) {
                return false;
            }
            return moveControl.setLocation(location);
        } else if (actionParams.containsFields("target")) {
            return moveControl.setTarget();
        }
        return false;
    }

    private static Vec3d convertProtobufValueToVec3d(com.google.protobuf.Value value) {
        assert value.getKindCase() == com.google.protobuf.Value.KindCase.STRUCT_VALUE;
        var struct = value.getStructValue();
        return new Vec3d(struct.getFieldsOrThrow("x").getNumberValue(), struct.getFieldsOrThrow("y").getNumberValue(),
                struct.getFieldsOrThrow("z").getNumberValue());
    }
}
