package com.resourceful_refinement.content.manifold;

import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, order-independent manifold state. The canonical key is deliberately human-readable;
 * {@link #identityHash()} is its compact stable identifier for later delivery matching.
 */
public final class ManifoldAssemblyRecord {
    private static final int FORMAT_VERSION = 1;
    private static final String NBT_VERSION = "FormatVersion";
    private static final String NBT_ETCHED = "Etched";
    private static final String NBT_FILL = "Fill";
    private static final String NBT_TEMPERATURE = "Temperature";
    private static final String NBT_FACES = "Faces";
    private static final String NBT_INDENT = "Indent";
    private static final String NBT_STAMP_ITEM = "StampItem";
    private static final String NBT_STAMP_FILL = "StampFill";
    private static final String NBT_EMBEDDING = "Embedding";

    public static final ManifoldAssemblyRecord EMPTY = new ManifoldAssemblyRecord(false, null, null, Map.of());

    private final boolean etched;
    @Nullable private final ResourceLocation fillFluid;
    @Nullable private final ExtendedHeatCondition temperature;
    private final Map<Direction, FaceAssembly> faces;

    private ManifoldAssemblyRecord(boolean etched, @Nullable ResourceLocation fillFluid,
                                   @Nullable ExtendedHeatCondition temperature,
                                   Map<Direction, FaceAssembly> faces) {
        this.etched = etched;
        this.fillFluid = fillFluid;
        this.temperature = temperature;
        EnumMap<Direction, FaceAssembly> copiedFaces = new EnumMap<>(Direction.class);
        copiedFaces.putAll(faces);
        this.faces = Map.copyOf(copiedFaces);
    }

    public boolean etched() {
        return etched;
    }

    public @Nullable ResourceLocation fillFluid() {
        return fillFluid;
    }

    public @Nullable ExtendedHeatCondition temperature() {
        return temperature;
    }

    public FaceAssembly face(Direction face) {
        return faces.getOrDefault(face, FaceAssembly.EMPTY);
    }

    public Map<Direction, FaceAssembly> faces() {
        return faces;
    }

    public ManifoldAssemblyRecord withEtching() {
        return etched ? this : new ManifoldAssemblyRecord(true, fillFluid, temperature, faces);
    }

    public ManifoldAssemblyRecord withFill(ResourceLocation fluidId) {
        return Objects.equals(fillFluid, fluidId) ? this : new ManifoldAssemblyRecord(etched, fluidId, temperature, faces);
    }

    public ManifoldAssemblyRecord withTemperature(ExtendedHeatCondition heatCondition) {
        return temperature == heatCondition ? this : new ManifoldAssemblyRecord(etched, fillFluid, heatCondition, faces);
    }

    public ManifoldAssemblyRecord withIndent(Direction face) {
        return withFace(face, this.face(face).withIndent());
    }

    public ManifoldAssemblyRecord withStamp(Direction face, ResourceLocation stampItemId, ResourceLocation fillId) {
        return withFace(face, this.face(face).withStamp(stampItemId, fillId));
    }

    public ManifoldAssemblyRecord withEmbedding(Direction face, ResourceLocation itemId) {
        return withFace(face, this.face(face).withEmbedding(itemId));
    }

    private ManifoldAssemblyRecord withFace(Direction face, FaceAssembly nextFace) {
        if (nextFace.equals(this.face(face))) {
            return this;
        }
        EnumMap<Direction, FaceAssembly> nextFaces = new EnumMap<>(Direction.class);
        nextFaces.putAll(faces);
        nextFaces.put(face, nextFace);
        return new ManifoldAssemblyRecord(etched, fillFluid, temperature, nextFaces);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_VERSION, FORMAT_VERSION);
        tag.putBoolean(NBT_ETCHED, etched);
        if (fillFluid != null) {
            tag.putString(NBT_FILL, fillFluid.toString());
        }
        if (temperature != null) {
            tag.putString(NBT_TEMPERATURE, temperature.name());
        }

        CompoundTag faceTag = new CompoundTag();
        for (Direction direction : Direction.values()) {
            FaceAssembly assembly = faces.get(direction);
            if (assembly == null || assembly.equals(FaceAssembly.EMPTY)) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putBoolean(NBT_INDENT, assembly.indented());
            if (assembly.stamp() != null) {
                entry.putString(NBT_STAMP_ITEM, assembly.stamp().itemId().toString());
                entry.putString(NBT_STAMP_FILL, assembly.stamp().fillId().toString());
            }
            if (assembly.embeddedItem() != null) {
                entry.putString(NBT_EMBEDDING, assembly.embeddedItem().toString());
            }
            faceTag.put(direction.getSerializedName(), entry);
        }
        if (!faceTag.isEmpty()) {
            tag.put(NBT_FACES, faceTag);
        }
        return tag;
    }

    public static ManifoldAssemblyRecord load(CompoundTag tag) {
        if (tag.getInt(NBT_VERSION) > FORMAT_VERSION) {
            return EMPTY;
        }
        ResourceLocation fill = parseId(tag, NBT_FILL);
        ExtendedHeatCondition temperature = parseTemperature(tag.getString(NBT_TEMPERATURE));
        EnumMap<Direction, FaceAssembly> faces = new EnumMap<>(Direction.class);
        if (tag.contains(NBT_FACES)) {
            CompoundTag faceTag = tag.getCompound(NBT_FACES);
            for (Direction direction : Direction.values()) {
                String key = direction.getSerializedName();
                if (!faceTag.contains(key)) {
                    continue;
                }
                CompoundTag entry = faceTag.getCompound(key);
                ResourceLocation stampItem = parseId(entry, NBT_STAMP_ITEM);
                ResourceLocation stampFill = parseId(entry, NBT_STAMP_FILL);
                StampData stamp = stampItem != null && stampFill != null ? new StampData(stampItem, stampFill) : null;
                FaceAssembly assembly = new FaceAssembly(entry.getBoolean(NBT_INDENT), stamp, parseId(entry, NBT_EMBEDDING));
                if (!assembly.equals(FaceAssembly.EMPTY)) {
                    faces.put(direction, assembly);
                }
            }
        }
        return new ManifoldAssemblyRecord(tag.getBoolean(NBT_ETCHED), fill, temperature, faces);
    }

    public String canonicalKey() {
        StringBuilder key = new StringBuilder("manifold:v").append(FORMAT_VERSION)
                .append("|etched=").append(etched)
                .append("|fill=").append(idOrEmpty(fillFluid))
                .append("|temperature=").append(temperature == null ? "" : temperature.name().toLowerCase(Locale.ROOT));
        for (Direction direction : Direction.values()) {
            FaceAssembly face = this.face(direction);
            key.append("|").append(direction.getSerializedName())
                    .append("(indent=").append(face.indented())
                    .append(",stamp=").append(face.stamp() == null ? "" : face.stamp().itemId())
                    .append(",stamp_fill=").append(face.stamp() == null ? "" : face.stamp().fillId())
                    .append(",embedding=").append(idOrEmpty(face.embeddedItem()))
                    .append(")");
        }
        return key.toString();
    }

    public String identityHash() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonicalKey().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format(Locale.ROOT, "%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static String idOrEmpty(@Nullable ResourceLocation id) {
        return id == null ? "" : id.toString();
    }

    private static @Nullable ResourceLocation parseId(CompoundTag tag, String key) {
        return tag.contains(key) ? ResourceLocation.tryParse(tag.getString(key)) : null;
    }

    private static @Nullable ExtendedHeatCondition parseTemperature(String name) {
        if (name.isEmpty()) {
            return null;
        }
        try {
            return ExtendedHeatCondition.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public record FaceAssembly(boolean indented, @Nullable StampData stamp, @Nullable ResourceLocation embeddedItem) {
        public static final FaceAssembly EMPTY = new FaceAssembly(false, null, null);

        public FaceAssembly withIndent() {
            return indented ? this : new FaceAssembly(true, stamp, embeddedItem);
        }

        public FaceAssembly withStamp(ResourceLocation stampItemId, ResourceLocation fillId) {
            StampData next = new StampData(stampItemId, fillId);
            return next.equals(stamp) ? this : new FaceAssembly(indented, next, embeddedItem);
        }

        public FaceAssembly withEmbedding(ResourceLocation itemId) {
            return itemId.equals(embeddedItem) ? this : new FaceAssembly(indented, stamp, itemId);
        }
    }

    public record StampData(ResourceLocation itemId, ResourceLocation fillId) {
        public StampData {
            Objects.requireNonNull(itemId, "itemId");
            Objects.requireNonNull(fillId, "fillId");
        }
    }
}
