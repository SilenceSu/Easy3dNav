package com.github.silencesu.Easy3dNav.internal;

import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavDebugApi;
import com.github.silencesu.Easy3dNav.api.NavMeshInfo;
import com.github.silencesu.Easy3dNav.api.NavOptions;
import com.github.silencesu.Easy3dNav.api.NavWallSegment;
import org.recast4j.detour.MeshHeader;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;

import java.util.List;
import java.util.function.Supplier;

/**
 * NavDebugApi 的默认实现。
 *
 * <p>调试查询复用 NavMeshContext 中当前线程的 NavMeshQuery 和默认 QueryFilter，公开结果继续
 * 使用 Easy3dNav 自有类型表达。</p>
 */
public class DefaultNavDebugApi implements NavDebugApi {

    private final NavMeshContext context;
    private final DetourQueries detourQueries;
    private final Supplier<float[]> defaultExtentsSupplier;

    /**
     * 创建调试入口。
     *
     * @param context                NavMesh 与线程级查询上下文
     * @param detourQueries          Detour 组合查询实现
     * @param defaultExtentsSupplier 默认搜索范围提供者，保证 setExtents 后新值可立即生效
     */
    public DefaultNavDebugApi(NavMeshContext context, DetourQueries detourQueries,
                              Supplier<float[]> defaultExtentsSupplier) {
        this.context = context;
        this.detourQueries = detourQueries;
        this.defaultExtentsSupplier = defaultExtentsSupplier;
    }

    @Override
    public List<NavWallSegment> getWallSegments(Vector3f point, NavOptions options) {
        NavOptions safeOptions = optionsOrDefault(options);
        return detourQueries.wallSegments(context.query(), context.filter(), toFloatArray(point), extents(safeOptions));
    }

    @Override
    public NavMeshInfo meshInfo() {
        NavMesh navMesh = context.navMesh();
        int loadedTileCount = 0;
        int polygonCount = 0;
        int vertexCount = 0;
        int detailVertexCount = 0;
        int detailTriangleCount = 0;
        int bvTreeNodeCount = 0;
        float[] boundsMin = null;
        float[] boundsMax = null;

        for (int i = 0; i < navMesh.getMaxTiles(); i++) {
            MeshTile tile = navMesh.getTile(i);
            if (tile == null || tile.data == null || tile.data.header == null) {
                continue;
            }

            MeshHeader header = tile.data.header;
            loadedTileCount++;
            polygonCount += header.polyCount;
            vertexCount += header.vertCount;
            detailVertexCount += header.detailVertCount;
            detailTriangleCount += header.detailTriCount;
            bvTreeNodeCount += header.bvNodeCount;
            boundsMin = min(boundsMin, header.bmin);
            boundsMax = max(boundsMax, header.bmax);
        }

        return new NavMeshInfo(loadedTileCount, navMesh.getMaxTiles(), polygonCount, vertexCount,
                detailVertexCount, detailTriangleCount, bvTreeNodeCount, toVector3f(boundsMin),
                toVector3f(boundsMax));
    }

    private float[] defaultExtents() {
        return defaultExtentsSupplier.get();
    }

    private NavOptions optionsOrDefault(NavOptions options) {
        if (options == null) {
            return NavOptions.defaults();
        }
        return options;
    }

    private float[] extents(NavOptions options) {
        if (options.hasExtents()) {
            return toFloatArray(options.getExtents());
        }
        return defaultExtents();
    }

    private static float[] min(float[] current, float[] candidate) {
        if (candidate == null || candidate.length < 3) {
            return current;
        }
        if (current == null) {
            return new float[]{candidate[0], candidate[1], candidate[2]};
        }
        return new float[]{
                Math.min(current[0], candidate[0]),
                Math.min(current[1], candidate[1]),
                Math.min(current[2], candidate[2])
        };
    }

    private static float[] max(float[] current, float[] candidate) {
        if (candidate == null || candidate.length < 3) {
            return current;
        }
        if (current == null) {
            return new float[]{candidate[0], candidate[1], candidate[2]};
        }
        return new float[]{
                Math.max(current[0], candidate[0]),
                Math.max(current[1], candidate[1]),
                Math.max(current[2], candidate[2])
        };
    }

    private static float[] toFloatArray(Vector3f vector) {
        if (vector == null) {
            return null;
        }
        return new float[]{vector.getX(), vector.getY(), vector.getZ()};
    }

    private static Vector3f toVector3f(float[] point) {
        if (point == null || point.length < 3) {
            return null;
        }
        return new Vector3f(point[0], point[1], point[2]);
    }
}
