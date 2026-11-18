package dev.moneyadvisor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Tiny standalone NBT reader, used only to pull item ids out of the base64+gzip
 * inventory blobs the Hypixel API returns. Independent of Minecraft's own NBT
 * classes so it survives Minecraft version changes.
 */
public final class Nbt {
    private Nbt() {}

    public static Set<String> itemIds(String base64) {
        Set<String> out = new HashSet<>();
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(
                new ByteArrayInputStream(java.util.Base64.getMimeDecoder().decode(base64))))) {
            int type = in.readUnsignedByte();
            if (type != 10) return out;
            in.readUTF();
            collect(readPayload(in, type), out);
        } catch (Exception ignored) {
            // corrupt or unexpected blob: just report no items
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void collect(Object o, Set<String> out) {
        if (o instanceof Map<?, ?> m) {
            for (String key : new String[] {"ExtraAttributes", "custom_data", "minecraft:custom_data"}) {
                Object attrs = m.get(key);
                if (attrs instanceof Map<?, ?> am && am.get("id") instanceof String id && !id.isEmpty()) {
                    out.add(id);
                }
            }
            for (Object v : ((Map<String, Object>) m).values()) collect(v, out);
        } else if (o instanceof List<?> l) {
            for (Object v : l) collect(v, out);
        }
    }

    private static Object readPayload(DataInputStream in, int type) throws IOException {
        switch (type) {
            case 1: return in.readByte();
            case 2: return in.readShort();
            case 3: return in.readInt();
            case 4: return in.readLong();
            case 5: return in.readFloat();
            case 6: return in.readDouble();
            case 7: in.skipNBytes(in.readInt()); return null;
            case 8: return in.readUTF();
            case 9: {
                int t = in.readUnsignedByte();
                int n = in.readInt();
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < n; i++) list.add(readPayload(in, t));
                return list;
            }
            case 10: {
                Map<String, Object> map = new LinkedHashMap<>();
                while (true) {
                    int t = in.readUnsignedByte();
                    if (t == 0) break;
                    String name = in.readUTF();
                    map.put(name, readPayload(in, t));
                }
                return map;
            }
            case 11: in.skipNBytes(4L * in.readInt()); return null;
            case 12: in.skipNBytes(8L * in.readInt()); return null;
            default: throw new IOException("Unknown NBT tag " + type);
        }
    }
}
