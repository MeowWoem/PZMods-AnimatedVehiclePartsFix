package io.ameno.AVPF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import me.zed_0xff.zombie_buddy.Patch;
import se.krka.kahlua.vm.KahluaTable;
import zombie.inventory.InventoryItem;
import zombie.scripting.objects.Item;
import zombie.scripting.objects.VehiclePartModel;
import zombie.scripting.objects.VehicleScript;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.BaseVehicle.ModelInfo;
import zombie.vehicles.VehiclePart;

import java.lang.reflect.Field;

public class AnimatedVehiclePartsFix {

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
                VehiclePart current = vehicle.getPartById(id);
                if (current != null && current != info.part) {
                    info.part = current;
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