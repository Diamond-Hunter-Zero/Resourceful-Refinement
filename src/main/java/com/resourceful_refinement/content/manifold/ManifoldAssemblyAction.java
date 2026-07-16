package com.resourceful_refinement.content.manifold;

import com.resourceful_refinement.utilities.heating.ExtendedHeatCondition;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** A single canonical mutation which can be applied to a manifold. */
public sealed interface ManifoldAssemblyAction permits ManifoldAssemblyAction.Etching,
        ManifoldAssemblyAction.Indent, ManifoldAssemblyAction.Fill, ManifoldAssemblyAction.Stamp,
        ManifoldAssemblyAction.Temperature, ManifoldAssemblyAction.Embedding {

    ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record);

    record Etching() implements ManifoldAssemblyAction {
        @Override
        public ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record) {
            return record.withEtching();
        }
    }

    record Indent(Direction face) implements ManifoldAssemblyAction {
        public Indent {
            Objects.requireNonNull(face, "face");
        }

        @Override
        public ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record) {
            return record.withIndent(face);
        }
    }

    record Fill(ResourceLocation fluidId) implements ManifoldAssemblyAction {
        public Fill {
            Objects.requireNonNull(fluidId, "fluidId");
        }

        @Override
        public ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record) {
            return record.withFill(fluidId);
        }
    }

    record Stamp(Direction face, ResourceLocation stampItemId, ResourceLocation fillId) implements ManifoldAssemblyAction {
        public Stamp {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(stampItemId, "stampItemId");
            Objects.requireNonNull(fillId, "fillId");
        }

        @Override
        public ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record) {
            return record.withStamp(face, stampItemId, fillId);
        }
    }

    record Temperature(ExtendedHeatCondition heatCondition) implements ManifoldAssemblyAction {
        public Temperature {
            Objects.requireNonNull(heatCondition, "heatCondition");
        }

        @Override
        public ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record) {
            return record.withTemperature(heatCondition);
        }
    }

    record Embedding(Direction face, ResourceLocation itemId) implements ManifoldAssemblyAction {
        public Embedding {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(itemId, "itemId");
        }

        @Override
        public ManifoldAssemblyRecord apply(ManifoldAssemblyRecord record) {
            return record.withEmbedding(face, itemId);
        }
    }
}
