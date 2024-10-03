package adris.altoclef.util.serialization;

import adris.altoclef.util.DimensionedZone;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

public class DimensionedZoneSerializer extends StdSerializer<Object> {
    public DimensionedZoneSerializer() {
        this(null);
    }

    public DimensionedZoneSerializer(Class<Object> vc) {
        super(vc);
    }

    private static final BlockPosSerializer blockPosSerializer = new BlockPosSerializer();

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        /* Example format:
        [
          ["minecraft:nether", "192.168.1.251", 0, 0, 0, 1, 1, 1]
        ]
        */

        List<DimensionedZone> dimensionedZones = (List<DimensionedZone>) value;
        gen.writeStartArray();
        for (DimensionedZone dimensionedZone : dimensionedZones) {
            gen.writeStartArray();
            gen.writeString(dimensionedZone.dimension().registryKey.getValue().toString());
            gen.writeString(dimensionedZone.networkName());
            blockPosSerializer.serialize(dimensionedZone.pos1(), gen, provider);
            blockPosSerializer.serialize(dimensionedZone.pos2(), gen, provider);
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }
}
