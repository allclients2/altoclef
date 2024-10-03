package adris.altoclef.util.serialization;

import adris.altoclef.multiversion.IdentifierVer;
import adris.altoclef.util.DimensionedZone;
import adris.altoclef.util.publicenums.Dimension;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//#if MC <= 11802
//$$ import net.minecraft.util.registry.Registry;
//$$ import net.minecraft.util.registry.RegistryKey;
//#else
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
//#endif

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DimensionedZoneDeserializer extends StdDeserializer<Object> {
    public DimensionedZoneDeserializer() {
        this(null);
    }

    public DimensionedZoneDeserializer(Class<Object> vc) {
        super(vc);
    }

    private static final BlockPosDeserializer blockPosDeserializer = new BlockPosDeserializer();

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // NOTE FOR EDITING: p.getCurrentToken() DOES NOT MOVE POINTER FORWARD! only `p.nextToken()` does! and COMMAS ARE NOT CONSIDERED TOKENS!

        List<DimensionedZone> result = new ArrayList<>();

        if (p.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new JsonParseException(p, "Start array expected");
        }
        while (p.nextToken() != JsonToken.END_ARRAY) {
            Dimension dimension = null;
            String networkName = null;
            BlockPos pos1 = null;
            BlockPos pos2 = null;

            if (p.getCurrentToken() != JsonToken.START_ARRAY) {
                throw new JsonParseException(p, "Start array expected (for a dz object)");
            }

            // Get dimension
            if (p.nextToken() != JsonToken.VALUE_STRING) {
                throw new JsonParseException(p, "Excepted a string for dimension while parsing dz object");
            } else {
                String dimensionName = p.getText();
                Identifier dimensionId = IdentifierVer.newCreation(dimensionName);
                RegistryKey<World> worldRegistryKey =
                        RegistryKey.of(
                            //#if MC <= 11802
                            //$$ Registry.WORLD_KEY,
                            //#else
                            RegistryKeys.WORLD,
                            //#endif
                            dimensionId
                        );
                dimension = Dimension.dimensionFromWorldKey(worldRegistryKey);
            }

            // Get networkName
            if (p.nextToken() != JsonToken.VALUE_STRING) {
                throw new JsonParseException(p, "Excepted a string for networkName while parsing dz object.");
            } else {
                networkName = p.getText();
            }

            // Get pos1 and pos2
            p.nextToken(); // progress pointer
            pos1 = blockPosDeserializer.deserialize(p, ctxt);
            p.nextToken(); // progress pointer
            pos2 = blockPosDeserializer.deserialize(p, ctxt);

            if (p.nextToken() != JsonToken.END_ARRAY) {
                throw new JsonParseException(p, "DZ OBJECT array too long");
            }

            result.add(new DimensionedZone(dimension, networkName, pos1, pos2));
        }

        return result;
    }
}
