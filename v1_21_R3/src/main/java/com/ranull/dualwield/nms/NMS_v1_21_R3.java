package com.ranull.dualwield.nms;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class NMS_v1_21_R3 implements NMS {
    @Override
    public void handAnimation(Player player, org.bukkit.inventory.EquipmentSlot equipmentSlot) {
        switch (equipmentSlot) {
            case HAND: {
                player.swingMainHand();
            }

            case OFF_HAND: {
                player.swingOffHand();
            }
        }
    }

    @Override
    public void blockBreakAnimation(Player player, org.bukkit.block.Block block, int animationID, int stage) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl serverGamePacketListener = serverPlayer.connection;
        BlockPos blockPosition = new BlockPos(block.getX(), block.getY(), block.getZ());

        serverGamePacketListener.send(new ClientboundBlockDestructionPacket(animationID, blockPosition, stage));
    }

    @Override
    public void blockCrackParticle(org.bukkit.block.Block block) {
        block.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, block.getLocation().add(0.5, 0, 0.5), // TODO change
                10, block.getBlockData());
    }

    @Override
    public float getToolStrength(org.bukkit.block.Block block, org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = serverLevel.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();

            return craftItemStack.getDestroySpeed(nmsBlock.defaultBlockState());
        }

        return 1;
    }

    @Override
    public double getAttackDamage(org.bukkit.inventory.ItemStack itemStack) {
        return getItemStackAttribute(itemStack, Attributes.ATTACK_DAMAGE.value()); // TODO change
    }

    @Override
    public double getAttackSpeed(org.bukkit.inventory.ItemStack itemStack) {
        return getItemStackAttribute(itemStack, Attributes.ATTACK_SPEED.value()); // TODO change
    }

    private double getItemStackAttribute(org.bukkit.inventory.ItemStack itemStack, Attribute attribute) {
        if (itemStack.getAmount() != 0) {
            ItemStack craftItemStack = CraftItemStack.asNMSCopy(itemStack);
            Multimap<Attribute, AttributeModifier> attributeMultimap = HashMultimap.create();

            craftItemStack.forEachModifier(EquipmentSlot.MAINHAND, (x, y) -> {
                attributeMultimap.put(x.value(), y);
            });

            AttributeModifier attributeModifier = Iterables.getFirst(attributeMultimap.get(attribute), null);

            return attributeModifier != null ? attributeModifier.amount() : 0;
        }

        return 0;
    }

    @Override
    public Sound getHitSound(org.bukkit.block.Block block) {
        try {
            ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
            Block nmsBlock = serverLevel.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();
            SoundType soundType = nmsBlock.defaultBlockState().getSoundType();

            return Sound.valueOf(soundType.getHitSound().location().getPath().toUpperCase()
                    .replace(".", "_"));
        } catch (IllegalArgumentException ignored) {
        }

        return Sound.BLOCK_STONE_HIT;
    }

    @Override
    public float getBlockHardness(org.bukkit.block.Block block) {
        ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
        Block nmsBlock = serverLevel.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();

        return nmsBlock.defaultBlockState().getDestroySpeed(null, null);
    }

    @Override
    public boolean breakBlock(Player player, org.bukkit.block.Block block) {
        return player.breakBlock(block);
    }

    @Override
    public void setModifier(Player player, double damage, double speed, UUID uuid) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance damageAttributeInstance = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speedAttributeInstance = serverPlayer.getAttribute(Attributes.ATTACK_SPEED);

        if (damageAttributeInstance != null) {
            damageAttributeInstance.addTransientModifier(new AttributeModifier(ResourceLocation.parse(Attributes.ATTACK_DAMAGE.getRegisteredName()), damage, AttributeModifier.Operation.ADD_VALUE));
        }

        if (speedAttributeInstance != null) {
            speedAttributeInstance.addTransientModifier(new AttributeModifier(ResourceLocation.parse(Attributes.ATTACK_SPEED.getRegisteredName()), speed, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void removeModifier(Player player, UUID uuid) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance damageAttributeInstance = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speedAttributeInstance = serverPlayer.getAttribute(Attributes.ATTACK_SPEED);

        if (damageAttributeInstance != null) {
            damageAttributeInstance.removeModifier(new AttributeModifier(ResourceLocation.parse(Attributes.ATTACK_DAMAGE.getRegisteredName()), 0, AttributeModifier.Operation.ADD_VALUE));
        }

        if (speedAttributeInstance != null) {
            speedAttributeInstance.removeModifier(new AttributeModifier(ResourceLocation.parse(Attributes.ATTACK_SPEED.getRegisteredName()), 0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void attack(Player player, org.bukkit.entity.Entity entity) {
        (((CraftPlayer) player).getHandle()).attack(((CraftEntity) entity).getHandle());
    }
}
