/*
Recast4J Copyright (c) 2015 Piotr Piastucki piotr@jtilia.org

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package com.github.silencesu.Easy3dNav.detour.io;

import com.github.silencesu.Easy3dNav.detour.DetourCommon;
import com.github.silencesu.Easy3dNav.detour.MeshData;
import com.github.silencesu.Easy3dNav.detour.NavMesh;
import com.github.silencesu.Easy3dNav.detour.NavMeshParams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 读取从
 * U3D插件CritterAI导出的
 */
public class MeshSetReaderU3d {

	private final MeshDataReader meshReader = new MeshDataReader();
	private final NavMeshParamReader paramReader = new NavMeshParamReader();

	public NavMesh read(InputStream is, int maxVertPerPoly) throws IOException {
		ByteBuffer bb = IOUtils.toByteBuffer(is);
		return read(bb, maxVertPerPoly, true);
	}

	public NavMesh read(ByteBuffer bb, int maxVertPerPoly) throws IOException {
		return read(bb, maxVertPerPoly, false);
	}

	public NavMesh read32Bit(InputStream is, int maxVertPerPoly) throws IOException {
		ByteBuffer bb = IOUtils.toByteBuffer(is);
		return read(bb, maxVertPerPoly, true);
	}

	public NavMesh read32Bit(ByteBuffer bb, int maxVertPerPoly) throws IOException {
		return read(bb, maxVertPerPoly, true);
	}

	NavMesh read(ByteBuffer bb, int maxVertPerPoly, boolean is32Bit) throws IOException {
		NavMeshSetHeader header = new NavMeshSetHeader();

		//TODO java 默认为大端序
		//需要设置为小端读取
		bb.order(ByteOrder.LITTLE_ENDIAN);
		header.version = bb.getInt();
		if (header.version != NavMeshSetHeader.NAVMESHSET_VERSION) {
			if (header.version != NavMeshSetHeader.NAVMESHSET_VERSION_RECAST4J) {
				throw new IOException("Invalid version");
			}
		}
		boolean cCompatibility = header.version == NavMeshSetHeader.NAVMESHSET_VERSION;
		header.numTiles = bb.getInt();
		header.params = paramReader.read(bb);
		NavMesh mesh = new NavMesh(header.params, maxVertPerPoly);

		// Read tiles.
		for (int i = 0; i < header.numTiles; ++i) {
			NavMeshTileHeader tileHeader = new NavMeshTileHeader();
 			if (is32Bit) {
				tileHeader.tileRef = convert32BitRef(bb.getInt(), header.params);
			} else {
				tileHeader.tileRef = bb.getLong();
			}
			tileHeader.dataSize = bb.getInt();
			if (tileHeader.tileRef == 0 || tileHeader.dataSize == 0) {
				break;
			}
			if (cCompatibility && !is32Bit) {
				bb.getInt(); // C struct padding
			}
			MeshData data = meshReader.read(bb, mesh.getMaxVertsPerPoly(), is32Bit);
			mesh.addTile(data, i, tileHeader.tileRef);
		}
		return mesh;
	}

	/**
	 * 转换为32位ref
	 * @param ref
	 * @param params
	 * @return
	 */
	private long convert32BitRef(int ref, NavMeshParams params) {
		int m_tileBits = DetourCommon.ilog2(DetourCommon.nextPow2(params.maxTiles));
		int m_polyBits = DetourCommon.ilog2(DetourCommon.nextPow2(params.maxPolys));
		// Only allow 31 salt bits, since the salt mask is calculated using 32bit uint and it will overflow.
		int m_saltBits = Math.min(31, 32 - m_tileBits - m_polyBits);
		int saltMask = (1 << m_saltBits) - 1;
		int tileMask = (1 << m_tileBits) - 1;
		int polyMask = (1 << m_polyBits) - 1;
		int salt = ((ref >> (m_polyBits + m_tileBits)) & saltMask);
		int it = ((ref >> m_polyBits) & tileMask);
		int ip = ref & polyMask;
		return NavMesh.encodePolyId(salt, it, ip);
	}

}
