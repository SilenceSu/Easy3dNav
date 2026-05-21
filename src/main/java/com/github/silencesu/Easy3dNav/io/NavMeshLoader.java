package com.github.silencesu.Easy3dNav.io;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.io.MeshSetReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NavMeshLoader {

    public NavMesh load(String meshFile, boolean useU3dData) throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(meshFile))) {
            if (useU3dData) {
                MeshSetReaderU3d reader = new MeshSetReaderU3d();
                return reader.read32Bit(inputStream, 6);
            }
            MeshSetReader reader = new MeshSetReader();
            return reader.read32Bit(inputStream, 6);
        }
    }
}
