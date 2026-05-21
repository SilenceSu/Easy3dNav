package com.github.silencesu.Easy3dNav.io;

import org.recast4j.detour.NavMesh;
import org.recast4j.detour.io.MeshSetReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NavMeshLoader {

    public NavMesh load(String meshFile, boolean useU3dData) throws IOException {
        return load(new File(meshFile), useU3dData);
    }

    /**
     * 从本地文件加载 NavMesh。
     *
     * <p>当前只在文件层面区分 U3D/CritterAI 与 recast4j 标准格式，避免把流关闭责任和内存缓冲区语义暴露给公开 API。</p>
     */
    public NavMesh load(File meshFile, boolean useU3dData) throws IOException {
        try (InputStream inputStream = new FileInputStream(meshFile)) {
            if (useU3dData) {
                MeshSetReaderU3d reader = new MeshSetReaderU3d();
                return reader.read32Bit(inputStream, 6);
            }
            MeshSetReader reader = new MeshSetReader();
            return reader.read32Bit(inputStream, 6);
        }
    }
}
