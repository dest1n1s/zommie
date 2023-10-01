package com.example.ai;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.example.ZommieZombieEntity;
import com.example.ai.control.ZommieAttackControl;
import com.example.ai.control.ZommieLookControl;
import com.example.ai.control.ZommieMoveControl;
import com.example.ai.control.ZommieTargetControl;
import com.example.proto.Action;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

public class ActionControlledGoal extends Goal {
    private final ZommieZombieEntity mob;
    private int noActionTimeout = 0;
    private ZommieMoveControl moveControl;
    private ZommieAttackControl attackControl;
    private ZommieLookControl lookControl;
    private ZommieTargetControl targetControl;

    public ActionControlledGoal(ZommieZombieEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Goal.Control.LOOK, Goal.Control.TARGET));
        this.moveControl = new ZommieMoveControl(mob, 1);
        this.attackControl = new ZommieAttackControl(mob);
        this.lookControl = new ZommieLookControl(mob);
        this.targetControl = new ZommieTargetControl(mob);
    }

    @Override
    public boolean canStart() {
        return noActionTimeout > 0;
    }

    @Override
    public boolean tick() {
        noActionTimeout = Math.max(noActionTimeout - 1, 0);

        moveControl.tick();
        attackControl.tick();
        lookControl.tick();
        targetControl.tick();
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
            case LOOK:
                executeLookAction(action.getParams());
                break;
            case TARGET:
                executeTargetAction(action.getParams());
                break;
            case JUMP:
                executeJumpAction(action.getParams());
                break;
            default:
                break;
        }
    }

    private boolean executeMoveAction(com.google.protobuf.Struct actionParams) {
        if (!actionParams.containsFields("type") || !actionParams.getFieldsOrThrow("type").hasStringValue()) {
            return false;
        }
        var type = actionParams.getFieldsOrThrow("type").getStringValue();
        if (type == "direction") {
            if (!actionParams.containsFields("direction")) {
                return false;
            }
            Vec3d direction;
            try {
                direction = convertProtobufValueToVec3d(actionParams.getFieldsOrThrow("direction"));
            } catch (Exception e) {
                return false;
            }
            moveControl.moveAlongDirection(direction);
            return true;
        } else if (type == "location") {
            if (!actionParams.containsFields("location")) {
                return false;
            }
            Vec3d location;
            try {
                location = convertProtobufValueToVec3d(actionParams.getFieldsOrThrow("location"));
            } catch (Exception e) {
                return false;
            }
            return moveControl.moveToLocation(location);
        } else if (type == "target") {
            return moveControl.moveToTarget();
        } else if (type == "stop") {
            moveControl.stop();
            return true;
        }
        return false;
    }

    private boolean executeAttackAction(com.google.protobuf.Struct actionParams) {
        if (!actionParams.containsFields("type") || !actionParams.getFieldsOrThrow("type").hasStringValue()) {
            return false;
        }
        var type = actionParams.getFieldsOrThrow("type").getStringValue();
        if (type == "start") {
            return attackControl.startAttacking();
        } else if (type == "stop") {
            attackControl.stopAttacking();
            return true;
        }
        return false;
    }

    private boolean executeLookAction(com.google.protobuf.Struct actionParams) {
        if (!actionParams.containsFields("type") || !actionParams.getFieldsOrThrow("type").hasStringValue()) {
            return false;
        }
        var type = actionParams.getFieldsOrThrow("type").getStringValue();
        if (type == "direction") {
            if (!actionParams.containsFields("direction")) {
                return false;
            }
            Vec3d direction;
            try {
                direction = convertProtobufValueToVec3d(actionParams.getFieldsOrThrow("direction"));
            } catch (Exception e) {
                return false;
            }
            lookControl.lookAtDirection(direction);
            return true;
        } else if (type == "location") {
            if (!actionParams.containsFields("location")) {
                return false;
            }
            Vec3d location;
            try {
                location = convertProtobufValueToVec3d(actionParams.getFieldsOrThrow("location"));
            } catch (Exception e) {
                return false;
            }
            lookControl.lookAtLocation(location);
            return true;
        } else if (type == "target") {
            return lookControl.lookAtTarget();
        } else if (type == "reset") {
            lookControl.reset();
            return true;
        }
        return false;
    }

    private boolean executeTargetAction(com.google.protobuf.Struct actionParams) {
        if (!actionParams.containsFields("type") || !actionParams.getFieldsOrThrow("type").hasStringValue()) {
            return false;
        }
        var type = actionParams.getFieldsOrThrow("type").getStringValue();
        if (type == "start") {
            if (!actionParams.containsFields("entity") || !actionParams.getFieldsOrThrow("entity").hasStructValue()){
                return false;
            }
            var entityParams = actionParams.getFieldsOrThrow("entity").getStructValue();
            if (!entityParams.containsFields("k") || !entityParams.getFieldsOrThrow("k").hasNumberValue()) {
                return false;
            }
            var k = (int) entityParams.getFieldsOrThrow("k").getNumberValue();
            String targetType = null;
            if (entityParams.containsFields("type") && entityParams.getFieldsOrThrow("type").hasStringValue()) {
                targetType = entityParams.getFieldsOrThrow("type").getStringValue();
            }
            return targetControl.startTargeting(k, targetType);
        } else if (type == "stop") {
            targetControl.stopTargeting();
            return true;
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
