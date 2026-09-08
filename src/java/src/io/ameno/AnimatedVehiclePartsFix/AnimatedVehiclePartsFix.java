package io.ameno.AVPF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import me.zed_0xff.zombie_buddy.Patch;
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
            if (animId == null) continue;

            if (!isPartPresent(part)) continue;

            if (forceRecreate) {
                debugDump(part); // <-- TEMPORAIRE : uniquement au moment ou ca casse
            }

            VehicleScript.Model model = resolveDisplayModel(part);
            if (model == null) continue;

            syncModelEntries(vehicle, part, model, forceRecreate);
            vehicle.playPartAnim(part, animId);
        }
    }

    public static void debugDump(VehiclePart part) {
        System.out.println("[AVPF-DEBUG] ===== part.getId()=" + part.getId() + " =====");

        VehicleScript.Part scriptPart = part.getScriptPart();
        if (scriptPart != null && scriptPart.models != null) {
            for (Object m : scriptPart.models) {
                VehicleScript.Model model = (VehicleScript.Model) m;
                System.out.println("[AVPF-DEBUG]   scriptPart.models entry -> id=" + model.getId() + " file=" + model.getFile());
            }
        } else {
            System.out.println("[AVPF-DEBUG]   scriptPart ou scriptPart.models est null");
        }

        InventoryItem item = part.getInventoryItem();
        if (item == null) {
            System.out.println("[AVPF-DEBUG]   part.getInventoryItem() == null");
            return;
        }
        System.out.println("[AVPF-DEBUG]   item installe -> fullType=" + item.getFullType() + " displayName=" + item.getDisplayName());

        Item scriptItem = item.getScriptItem();
        if (scriptItem == null) {
            System.out.println("[AVPF-DEBUG]   item.getScriptItem() == null");
            return;
        }

        ArrayList<VehiclePartModel> vpms = scriptItem.getVehiclePartModels();
        if (vpms == null || vpms.isEmpty()) {
            System.out.println("[AVPF-DEBUG]   getVehiclePartModels() est null ou vide");
            return;
        }
        for (VehiclePartModel vpm : vpms) {
            System.out.println("[AVPF-DEBUG]   vehiclePartModel -> partId=" + vpm.partId + " partModelId=" + vpm.partModelId + " modelId=" + vpm.modelId);
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

    public static VehicleScript.Model resolveDisplayModel(VehiclePart part) {
        VehicleScript.Part scriptPart = part.getScriptPart();
        if (scriptPart == null || scriptPart.models == null || scriptPart.models.isEmpty()) return null;

        InventoryItem item = part.getInventoryItem();
        if (item != null) {
            Item scriptItem = item.getScriptItem();
            ArrayList<VehiclePartModel> vehiclePartModels = scriptItem != null ? scriptItem.getVehiclePartModels() : null;
            if (vehiclePartModels != null) {
                for (VehiclePartModel vpm : vehiclePartModels) {
                    if (!vpm.partId.equalsIgnoreCase(part.getId())) continue;
                    for (Object m : scriptPart.models) {
                        VehicleScript.Model model = (VehicleScript.Model) m;
                        if (vpm.partModelId.equalsIgnoreCase(model.getId())) {
                            return model;
                        }
                    }
                }
            }
        }

        for (Object m : scriptPart.models) {
            VehicleScript.Model model = (VehicleScript.Model) m;
            if ("Default".equals(model.id)) return model;
        }
        return (VehicleScript.Model) scriptPart.models.get(0);
    }

    public static void syncModelEntries(BaseVehicle vehicle, VehiclePart part, VehicleScript.Model model, boolean forceRecreate) {
        int matchCount = 0;
        boolean keptOne = false;

        Iterator<BaseVehicle.ModelInfo> it = vehicle.models.iterator();
        while (it.hasNext()) {
            BaseVehicle.ModelInfo info = it.next();
            if (info.part == null || !info.part.getId().equals(part.getId())) continue;

            matchCount++;
            boolean shouldRemove = forceRecreate || (matchCount > 1 && keptOne);
            if (shouldRemove) {
                if (info.animPlayer != null) {
                    info.animPlayer = null;
                }
                it.remove();
            } else {
                keptOne = true;
            }
        }

        if (forceRecreate || matchCount == 0) {
            vehicle.setModelVisible(part, model, true);
        }
    }
}