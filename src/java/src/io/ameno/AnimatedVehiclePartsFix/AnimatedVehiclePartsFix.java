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
import zombie.vehicles.VehiclePart;

public class AnimatedVehiclePartsFix {

    public static final Set<BaseVehicle> pendingVehicles = new HashSet<>();

    @Patch(className = "zombie.vehicles.BaseVehicle", methodName = "addToWorld")
    public static class PatchAddToWorld {
        @Patch.OnEnter
        public static void enter(@Patch.This BaseVehicle vehicle) {
            if (vehicle != null) {
                pendingVehicles.add(vehicle);
            }
        }

        @Patch.OnExit
        public static void exit(@Patch.This BaseVehicle vehicle) {
            if (vehicle != null && vehicle.getScript() != null) {
                syncParts(vehicle, false);
            }
        }
    }

    @Patch(className = "zombie.vehicles.BaseVehicle", methodName = "postupdate")
    public static class PatchPostUpdate {
        @Patch.OnEnter
        public static void enter(@Patch.This BaseVehicle vehicle) {
            if (vehicle != null && vehicle.getScript() != null && pendingVehicles.remove(vehicle)) {
                syncParts(vehicle, vehicle.isEngineRunning());
            }
        }
    }

    public static void syncParts(BaseVehicle vehicle, boolean forceRecreate) {
        if (vehicle == null || vehicle.getParts() == null) return;

        for (int i = 0; i < vehicle.getPartCount(); i++) {
            VehiclePart part = vehicle.getPartByIndex(i);
            if (part == null) continue;

            String animId = resolveAnimId(part);
            boolean isTuningPart = resolveTuning2ModelId(part) != null;

            if (animId == null && !(forceRecreate && isTuningPart)) continue;

            if (!isPartPresent(part)) continue;

            List<VehicleScript.Model> models = resolveDisplayModels(part);
            if (models.isEmpty()) continue;

            syncModelEntries(vehicle, part, models, forceRecreate);

            if (animId != null) {
                vehicle.playPartAnim(part, animId);
            }
        }
    }

    public static String resolveAnimId(VehiclePart part) {
        if (part.getDoor() != null) {
            return part.getDoor().isOpen() ? "Opened" : "Closed";
        }
        if (part.getWindow() != null) {
            return part.getWindow().isOpen() ? "Opened" : "Closed";
        }
        if (part.hasModData() && part.getModData().rawget("open") != null) {
            return (Boolean) part.getModData().rawget("open") ? "Opened" : "Closed";
        }
        return null;
    }

    public static boolean isPartPresent(VehiclePart part) {
        List<String> itemType = part.getItemType();
        boolean requiresItem = itemType != null && !itemType.isEmpty();
        return !requiresItem || part.getInventoryItem() != null;
    }

    public static List<VehicleScript.Model> resolveDisplayModels(VehiclePart part) {
        VehicleScript.Part scriptPart = part.getScriptPart();
        if (scriptPart == null || scriptPart.models == null || scriptPart.models.isEmpty()) {
            return List.of();
        }

        String tuning2ModelId = resolveTuning2ModelId(part);
        if (tuning2ModelId != null) {
            VehicleScript.Model primary = findModelById(scriptPart, tuning2ModelId);
            if (primary != null) {
                List<VehicleScript.Model> result = new ArrayList<>();
                result.add(primary);
                boolean rusted = tuning2ModelId.contains("Rusted");
                VehicleScript.Model anchor = findModelById(scriptPart, rusted ? "anchorRusted" : "anchorNormal");
                if (anchor != null) {
                    result.add(anchor);
                }
                return result;
            }
        }

        if ("EngineDoor".equals(part.getId())) {
            BaseVehicle vehicle = part.getVehicle();
            VehiclePart scoop = vehicle != null ? vehicle.getPartById("ATA2AirScoop") : null;
            if (scoop != null && scoop.getInventoryItem() != null) {
                VehicleScript.Model scooped = findModelById(scriptPart, "Scooped");
                if (scooped != null) {
                    return List.of(scooped);
                }
            }
        }

        InventoryItem item = part.getInventoryItem();
        if (item != null) {
            Item scriptItem = item.getScriptItem();
            ArrayList<VehiclePartModel> vehiclePartModels = scriptItem != null ? scriptItem.getVehiclePartModels() : null;
            if (vehiclePartModels != null) {
                for (VehiclePartModel vpm : vehiclePartModels) {
                    if (!vpm.partId.equalsIgnoreCase(part.getId())) continue;
                    VehicleScript.Model match = findModelById(scriptPart, vpm.partModelId);
                    if (match != null) {
                        return List.of(match);
                    }
                }
            }
        }

        VehicleScript.Model fallback = findModelById(scriptPart, "Default");
        if (fallback == null) {
            fallback = (VehicleScript.Model) scriptPart.models.get(0);
        }
        return List.of(fallback);
    }

    public static String resolveTuning2ModelId(VehiclePart part) {
        if (!part.hasModData()) return null;
        Object tuning2Obj = part.getModData().rawget("tuning2");
        if (!(tuning2Obj instanceof KahluaTable tuning2Table)) return null;
        Object modelObj = tuning2Table.rawget("model");
        return modelObj instanceof String ? (String) modelObj : null;
    }

    public static VehicleScript.Model findModelById(VehicleScript.Part scriptPart, String id) {
        if (id == null) return null;
        for (Object m : scriptPart.models) {
            VehicleScript.Model model = (VehicleScript.Model) m;
            if (id.equalsIgnoreCase(model.getId())) {
                return model;
            }
        }
        return null;
    }

    public static void syncModelEntries(BaseVehicle vehicle, VehiclePart part, List<VehicleScript.Model> wantedModels, boolean forceRecreate) {
        List<VehicleScript.Model> stillMissing = new ArrayList<>(wantedModels);

        Iterator<BaseVehicle.ModelInfo> it = vehicle.models.iterator();
        while (it.hasNext()) {
            BaseVehicle.ModelInfo info = it.next();
            if (info.part == null || !info.part.getId().equals(part.getId())) continue;

            boolean isWanted = !forceRecreate && stillMissing.remove(info.scriptModel);
            if (!isWanted) {
                if (info.animPlayer != null) {
                    info.animPlayer = null;
                }
                it.remove();
            }
        }

        List<VehicleScript.Model> toAdd = forceRecreate ? wantedModels : stillMissing;
        for (VehicleScript.Model model : toAdd) {
            vehicle.setModelVisible(part, model, true);
        }
    }
}