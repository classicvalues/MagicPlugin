package com.elmakers.mine.bukkit.utility.platform.base.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.entity.EntityExtraData;
import com.elmakers.mine.bukkit.entity.SpawnedEntityExtraData;
import com.elmakers.mine.bukkit.utility.ConfigUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;

public class EntityFallingBlockData extends EntityExtraData {
    @Nullable
    private MaterialAndData material;
    private Boolean dropItem;
    private Boolean hurtEntities;

    public EntityFallingBlockData(ConfigurationSection configuration, MageController controller) {
        String materialKey = configuration.getString("material");
        if (materialKey != null && !materialKey.isEmpty()) {
            material = controller.createMaterialAndData(materialKey);
        }
        dropItem = ConfigUtils.getOptionalBoolean(configuration, "drop_items");
        hurtEntities = ConfigUtils.getOptionalBoolean(configuration, "hurt_entities");
    }

    public EntityFallingBlockData(Entity entity, MageController controller) {
        CompatibilityUtils compatibilityUtils = getPlatform().getCompatibilityUtils();
        if (entity instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock)entity;
            Material material = compatibilityUtils.getMaterial(fallingBlock);
            String blockData = compatibilityUtils.getBlockData(fallingBlock);
            if (blockData != null) {
                this.material = controller.createMaterialAndData(material, blockData);
            } else {
                byte data = compatibilityUtils.getLegacyBlockData(fallingBlock);
                this.material = controller.createMaterialAndData(material, data);
            }
            dropItem = fallingBlock.getDropItem();
            hurtEntities = fallingBlock.canHurtEntities();
        }
    }

    @Override
    public void apply(Entity entity) {
        if (entity instanceof  FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock)entity;
            if (dropItem != null) fallingBlock.setDropItem(dropItem);
            if (hurtEntities != null) fallingBlock.setHurtEntities(hurtEntities);
        }
    }

    @Override
    public void setMaterialAndData(@Nonnull MaterialAndData material) {
        this.material = material;
    }

    @Nullable
    @Override
    public MaterialAndData getMaterialAndData() {
        return material;
    }

    public byte getMaterialData() {
        if (material == null) {
            return 0;
        }

        Byte data = material.getBlockData();
        return data == null ? 0 : data;
    }

    @Nullable
    public Material getMaterial() {
        return material == null ? null : material.getMaterial();
    }

    @Nullable
    public String getBlockData() {
        return material == null ? null : material.getModernBlockData();
    }

    @Override
    @SuppressWarnings("deprecation")
    public SpawnedEntityExtraData spawn(Location location) {
        Material material = getMaterial();
        byte data = getMaterialData();
        if (material == null) {
            material = Material.DIRT;
        }
        Entity newEntity = location.getWorld().spawnFallingBlock(location, material, data);
        return new SpawnedEntityExtraData(newEntity, true);
    }
}
