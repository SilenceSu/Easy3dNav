package com.github.silencesu.Easy3dNav.detour.io;

import com.github.silencesu.Easy3dNav.detour.NavMeshParams;

import java.nio.ByteBuffer;

public class NavMeshParamReader {

	public NavMeshParams read(ByteBuffer bb) {
		NavMeshParams params = new NavMeshParams();
		params.orig[0] = bb.getFloat();
		params.orig[1] = bb.getFloat();
		params.orig[2] = bb.getFloat();
		params.tileWidth = bb.getFloat();
		params.tileHeight = bb.getFloat();
		params.maxTiles = bb.getInt();
		params.maxPolys = bb.getInt();
		return params;
	}

}
