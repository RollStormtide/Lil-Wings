package dev.willowworks.lilwings.block;

import dev.willowworks.lilwings.entity.ButterflyEntity;
import dev.willowworks.lilwings.entity.jareffects.JarEffect;
import dev.willowworks.lilwings.registry.LilWingsBlocks;
import dev.willowworks.lilwings.registry.entity.Butterfly;
import dev.willowworks.lilwings.registry.entity.GraylingType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class ButterflyJarBlockEntity extends BlockEntity {

    private EntityType<? extends ButterflyEntity> entityType;
    private NbtCompound butterflyData;
    private JarEffect jarEffect;
    private GraylingType colorType;

    public ButterflyJarBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(LilWingsBlocks.BUTTERFLY_JAR_ENTITY.get(), pWorldPosition, pBlockState);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (entityType != null)
            tag.putString("entityId", entityType.getRegistryName().toString());

        if (butterflyData != null)
            tag.put("butterfly", butterflyData);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (tag.contains("entityId", NbtElement.STRING_TYPE)) {
            Identifier id = new Identifier(tag.getString("entityId"));

            if (Butterfly.BUTTERFLIES.containsKey(id)) {
                entityType = (EntityType<? extends ButterflyEntity>) ForgeRegistries.ENTITIES.getValue(id);

                Butterfly butterfly = Butterfly.BUTTERFLIES.get(entityType.getRegistryName());
                if (jarEffect == null && butterfly.jarEffect() != null)
                    jarEffect = butterfly.jarEffect().get();
            }
        }

        if (tag.contains("butterfly")) {
            this.butterflyData = tag.getCompound("butterfly");
        }
    }

    public static void tick(World level, BlockPos pos, BlockState state, ButterflyJarBlockEntity blockEntity) {
        if (blockEntity.getJarEffect() != null)
            blockEntity.getJarEffect().tickEffect(level, blockEntity);
    }

    public void setEntityType(EntityType<? extends ButterflyEntity> entityType) {
        if(entityType != null) {
            Butterfly butterfly = Butterfly.BUTTERFLIES.get(entityType.getRegistryName());
            this.entityType = entityType;
            if (butterfly.jarEffect() != null)
                this.jarEffect = butterfly.jarEffect().get();
            markDirty();
            world.updateListeners(getPos(), getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        } else {
            this.entityType = null;
            this.jarEffect = null;
        }
    }

    public void setButterflyData(NbtCompound tag) {
        this.butterflyData = tag;
        if (tag != null && tag.contains("colorType")) {
            colorType = GraylingType.valueOf(tag.getString("colorType"));
        }

        markDirty();
        world.updateListeners(getPos(), getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
    }

    public EntityType<? extends ButterflyEntity> getEntityType() {
        return entityType;
    }

    public NbtCompound getButterflyData() {
        return butterflyData;
    }

    public JarEffect getJarEffect() {
        return jarEffect;
    }

    public GraylingType getColorType() {
        return colorType;
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        writeNbt(tag);
        return tag;
    }
}
