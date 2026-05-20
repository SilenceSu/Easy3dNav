package com.github.silencesu.Easy3dNav.io;

import org.recast4j.detour.DetourCommon;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshParams;
import org.recast4j.detour.io.IOUtils;
import org.recast4j.detour.io.MeshDataReader;
import org.recast4j.detour.io.NavMeshParamReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 读取 Unity/CritterAI 导出的 NavMeshSet。
 */
public class MeshSetReaderU3d {

    private static final int NAVMESHSET_VERSION = 1;
    private static final int NAVMESHSET_VERSION_RECAST4J_1 = 0x8801;
    private static final int NAVMESHSET_VERSION_RECAST4J = 0x8802;

    private final MeshDataReader meshReader = new MeshDataReader();
    private final NavMeshParamReader paramReader = new NavMeshParamReader();

    public NavMesh read(InputStream is, int maxVertPerPoly) throws IOException {
        return read32Bit(is, maxVertPerPoly);
    }

    public NavMesh read(ByteBuffer bb, int maxVertPerPoly) throws IOException {
        return read32Bit(bb, maxVertPerPoly);
    }

    public NavMesh read32Bit(InputStream is, int maxVertPerPoly) throws IOException {
        return read32Bit(IOUtils.toByteBuffer(is), maxVertPerPoly);
    }

    public NavMesh read32Bit(ByteBuffer bb, int maxVertPerPoly) throws IOException {
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int version = bb.getInt();
        if (version != NAVMESHSET_VERSION
                && version != NAVMESHSET_VERSION_RECAST4J_1
                && version != NAVMESHSET_VERSION_RECAST4J) {
            throw new IOException("Invalid version " + version);
        }

        int numTiles = bb.getInt();
        NavMeshParams params = paramReader.read(bb);
        int vertsPerPoly = version == NAVMESHSET_VERSION_RECAST4J ? bb.getInt() : maxVertPerPoly;
        if (vertsPerPoly <= 0) {
            throw new IOException("Invalid number of verts per poly " + vertsPerPoly);
        }

        NavMesh mesh = new NavMesh(params, vertsPerPoly);
        for (int i = 0; i < numTiles; ++i) {
            long tileRef = convert32BitRef(bb.getInt(), params);
            int dataSize = bb.getInt();
            if (tileRef == 0 || dataSize == 0) {
                break;
            }

            MeshData data = meshReader.read32Bit(bb, mesh.getMaxVertsPerPoly());
            mesh.addTile(data, i, tileRef);
        }
        return mesh;
    }

    private long convert32BitRef(int ref, NavMeshParams params) {
        int tileBits = DetourCommon.ilog2(DetourCommon.nextPow2(params.maxTiles));
        int polyBits = DetourCommon.ilog2(DetourCommon.nextPow2(params.maxPolys));
        int saltBits = Math.min(31, 32 - tileBits - polyBits);
        int saltMask = (1 << saltBits) - 1;
        int tileMask = (1 << tileBits) - 1;
        int polyMask = (1 << polyBits) - 1;
        int salt = (ref >> (polyBits + tileBits)) & saltMask;
        int tile = (ref >> polyBits) & tileMask;
        int poly = ref & polyMask;
        return NavMesh.encodePolyId(salt, tile, poly);
    }
}
