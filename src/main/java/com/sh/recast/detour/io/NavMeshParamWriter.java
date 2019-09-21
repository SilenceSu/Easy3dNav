package com.sh.recast.detour.io;

import com.sh.recast.detour.NavMeshParams;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

public class NavMeshParamWriter extends DetourWriter {

	public void write(OutputStream stream, NavMeshParams params, ByteOrder order) throws IOException {
		write(stream, params.orig[0], order);
		write(stream, params.orig[1], order);
		write(stream, params.orig[2], order);
		write(stream, params.tileWidth, order);
		write(stream, params.tileHeight, order);
		write(stream, params.maxTiles, order);
		write(stream, params.maxPolys, order);
	}

}
