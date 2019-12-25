package com.github.silencesu.Easy3dNav.detour.io;

import com.github.silencesu.Easy3dNav.detour.NavMeshParams;


public class NavMeshSetHeader {

	static final int NAVMESHSET_MAGIC = 'M'<<24 | 'S'<<16 | 'E'<<8 | 'T'; //'MSET';
	static final int NAVMESHSET_VERSION = 1;
	static final int NAVMESHSET_VERSION_RECAST4J = 0x8801;

	int magic;
	int version;
	int numTiles;
	NavMeshParams params = new NavMeshParams();

}

