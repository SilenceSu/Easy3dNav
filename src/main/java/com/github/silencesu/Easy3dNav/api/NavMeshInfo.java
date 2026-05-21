package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * NavMesh 结构化调试元数据。
 *
 * <p>该对象用于调试、监控和工具可视化，只暴露统计值和整体 bounds，不暴露 recast4j 的
 * NavMesh、MeshTile、Poly 或其他底层类型。</p>
 */
public class NavMeshInfo {

    private final int loadedTileCount;
    private final int maxTileCount;
    private final int polygonCount;
    private final int vertexCount;
    private final int detailVertexCount;
    private final int detailTriangleCount;
    private final int bvTreeNodeCount;
    private final Vector3f boundsMin;
    private final Vector3f boundsMax;

    /**
     * 创建 NavMesh 元数据。
     *
     * @param loadedTileCount    已加载 tile 数
     * @param maxTileCount       NavMesh 最大 tile 数
     * @param polygonCount       polygon 数
     * @param vertexCount        顶点数
     * @param detailVertexCount  detail 顶点数
     * @param detailTriangleCount detail 三角形数
     * @param bvTreeNodeCount    BVTree 节点数
     * @param boundsMin          整体 bounds 最小点；空地图时可能为 null
     * @param boundsMax          整体 bounds 最大点；空地图时可能为 null
     */
    public NavMeshInfo(int loadedTileCount, int maxTileCount, int polygonCount, int vertexCount,
                       int detailVertexCount, int detailTriangleCount, int bvTreeNodeCount,
                       Vector3f boundsMin, Vector3f boundsMax) {
        this.loadedTileCount = loadedTileCount;
        this.maxTileCount = maxTileCount;
        this.polygonCount = polygonCount;
        this.vertexCount = vertexCount;
        this.detailVertexCount = detailVertexCount;
        this.detailTriangleCount = detailTriangleCount;
        this.bvTreeNodeCount = bvTreeNodeCount;
        this.boundsMin = copy(boundsMin);
        this.boundsMax = copy(boundsMax);
    }

    /**
     * 返回已加载 tile 数。
     */
    public int getLoadedTileCount() {
        return loadedTileCount;
    }

    /**
     * 返回 NavMesh 最大 tile 数。
     */
    public int getMaxTileCount() {
        return maxTileCount;
    }

    /**
     * 返回 polygon 数。
     */
    public int getPolygonCount() {
        return polygonCount;
    }

    /**
     * 返回顶点数。
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * 返回 detail 顶点数。
     */
    public int getDetailVertexCount() {
        return detailVertexCount;
    }

    /**
     * 返回 detail 三角形数。
     */
    public int getDetailTriangleCount() {
        return detailTriangleCount;
    }

    /**
     * 返回 BVTree 节点数。
     */
    public int getBvTreeNodeCount() {
        return bvTreeNodeCount;
    }

    /**
     * 返回整体 bounds 最小点。空地图时可能为 null。
     */
    public Vector3f getBoundsMin() {
        return copy(boundsMin);
    }

    /**
     * 返回整体 bounds 最大点。空地图时可能为 null。
     */
    public Vector3f getBoundsMax() {
        return copy(boundsMax);
    }

    private static Vector3f copy(Vector3f source) {
        if (source == null) {
            return null;
        }
        return new Vector3f(source.getX(), source.getY(), source.getZ());
    }
}
