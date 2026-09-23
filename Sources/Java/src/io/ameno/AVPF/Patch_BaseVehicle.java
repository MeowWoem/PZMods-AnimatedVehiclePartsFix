package io.ameno.AVPF;

import java.util.List;
import me.zed_0xff.zombie_buddy.Patch;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.BaseVehicle.ModelInfo;
import zombie.vehicles.VehiclePart;

import java.lang.reflect.Field;

public class Patch_BaseVehicle {

    public static final Field LIGHTS_FIELD = resolveLightsField();

    public static Field resolveLightsField() {
        try {
            Field f = BaseVehicle.class.getDeclaredField("lights");
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    @Patch(className = "zombie.vehicles.BaseVehicle", methodName = "adoptParts")
    public static class PatchAdoptParts {
        @Patch.OnExit
        public static void exit(@Patch.This BaseVehicle vehicle) {
            readoptModelParts(vehicle);
            readoptLightsList(vehicle);
        }
    }

    public static void readoptModelParts(BaseVehicle vehicle) {
        if (vehicle == null || vehicle.models == null) return;
        try {
            for (Object o : vehicle.models) {
                ModelInfo info = (ModelInfo) o;
                if (info == null || info.part == null) continue;
                String id = info.part.getId();
                if (id == null) continue;
                VehiclePart part = vehicle.getPartById(id);
                if (part != null && part != info.part) {
                    info.part = part;
                }
            }
        } catch (Exception e) { }
    }

    public static void readoptLightsList(BaseVehicle vehicle) {
        if (LIGHTS_FIELD == null || vehicle == null) return;
        try {
            @SuppressWarnings("unchecked")
            List<VehiclePart> lights = (List<VehiclePart>) LIGHTS_FIELD.get(vehicle);
            lights.clear();
            for (int i = 0; i < vehicle.getPartCount(); i++) {
                VehiclePart part = vehicle.getPartByIndex(i);
                if (part != null && part.getLight() != null) {
                    lights.add(part);
                }
            }
        } catch (Exception e) { }
    }

}