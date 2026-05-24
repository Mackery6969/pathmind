package com.pathmind.nodes;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class NodePlayerStateSensorEvaluator {
    private static final long FALLING_SENSOR_RETENTION_MS = 1000L;
    private static final double FALLING_SENSOR_MIN_CLEARANCE = 0.6D;

    private final Node owner;

    NodePlayerStateSensorEvaluator(Node owner) {
        this.owner = owner;
    }

    boolean evaluateFalling() {
        double distance = Math.max(0.0, owner.getDoubleParameter("Distance", 2.0));
        return isFalling(distance);
    }

    boolean isSwimming() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && client.player.isSwimming();
    }

    boolean isInLava() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && client.player.isInLava();
    }

    boolean isUnderwater() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.player != null && client.player.isUnderWater();
    }

    Optional<Double> getDistanceFromGround() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) {
            return Optional.empty();
        }

        AABB box = client.player.getBoundingBox();
        double bottomY = box.minY;
        double bottomLimit = client.level.getMinBuildHeight() - 1.0;
        double inset = 1.0E-3;
        Vec3[] samplePoints = new Vec3[] {
            new Vec3((box.minX + box.maxX) * 0.5, bottomY + 0.01, (box.minZ + box.maxZ) * 0.5),
            new Vec3(box.minX + inset, bottomY + 0.01, box.minZ + inset),
            new Vec3(box.minX + inset, bottomY + 0.01, box.maxZ - inset),
            new Vec3(box.maxX - inset, bottomY + 0.01, box.minZ + inset),
            new Vec3(box.maxX - inset, bottomY + 0.01, box.maxZ - inset)
        };

        Double nearestDistance = null;
        for (Vec3 start : samplePoints) {
            HitResult hit = client.level.clip(new ClipContext(
                start,
                new Vec3(start.x, bottomLimit, start.z),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                client.player
            ));
            if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
                continue;
            }
            double distance = Math.max(0.0, bottomY - blockHit.getLocation().y);
            if (nearestDistance == null || distance < nearestDistance) {
                nearestDistance = distance;
            }
        }

        if (nearestDistance == null) {
            return Optional.empty();
        }
        if (nearestDistance < 1.0E-3) {
            nearestDistance = 0.0;
        }
        return Optional.of(nearestDistance);
    }

    static boolean isFallingState(
        boolean onGround,
        boolean swimming,
        boolean submergedInWater,
        boolean climbing,
        boolean flying,
        double downwardVelocity,
        double fallDistance,
        double peakY,
        double currentY,
        double groundClearance,
        double requiredDistance,
        long nowMs,
        long lastDetectedAtMs
    ) {
        if (onGround || swimming || submergedInWater || climbing || flying) {
            return false;
        }
        if (lastDetectedAtMs != Long.MIN_VALUE && nowMs - lastDetectedAtMs <= FALLING_SENSOR_RETENTION_MS) {
            return true;
        }
        if (downwardVelocity >= -1.0E-3) {
            return false;
        }
        if (groundClearance >= FALLING_SENSOR_MIN_CLEARANCE
            && (fallDistance > 1.0E-3 || peakY - currentY > 1.0E-3)) {
            return true;
        }
        if (fallDistance >= requiredDistance) {
            return true;
        }
        return peakY - currentY >= requiredDistance;
    }

    boolean isFalling(double distance) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return false;
        }
        NodeRuntimeState runtimeState = owner.runtimeState();
        long now = System.currentTimeMillis();
        double currentY = client.player.getY();
        if (client.player.onGround()) {
            runtimeState.fallingPeakY = currentY;
            runtimeState.fallingPeakInitialized = true;
            runtimeState.lastFallingDetectedAtMs = Long.MIN_VALUE;
            return false;
        }
        if (client.player.isSwimming()
            || client.player.isUnderWater()
            || client.player.onClimbable()
            || client.player.getAbilities().flying) {
            runtimeState.fallingPeakY = currentY;
            runtimeState.fallingPeakInitialized = false;
            runtimeState.lastFallingDetectedAtMs = Long.MIN_VALUE;
            return false;
        }

        if (!runtimeState.fallingPeakInitialized) {
            runtimeState.fallingPeakY = currentY;
            runtimeState.fallingPeakInitialized = true;
        } else if (currentY > runtimeState.fallingPeakY) {
            runtimeState.fallingPeakY = currentY;
        }
        double groundClearance = getDistanceFromGround().orElse(Double.POSITIVE_INFINITY);

        boolean falling = isFallingState(
            false,
            false,
            false,
            false,
            false,
            client.player.getDeltaMovement().y,
            client.player.fallDistance,
            runtimeState.fallingPeakY,
            currentY,
            groundClearance,
            distance,
            now,
            runtimeState.lastFallingDetectedAtMs
        );
        if (falling) {
            runtimeState.lastFallingDetectedAtMs = now;
        }
        return falling;
    }
}
