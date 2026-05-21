package com.github.silencesu.Easy3dNav.internal;

import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavOptions;
import com.github.silencesu.Easy3dNav.api.NavPathApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * NavPathApi 的默认实现。
 *
 * <p>路径能力通过当前线程的 NavMeshQuery 执行，QueryFilter 仍沿用 NavMeshContext 中的默认实例。</p>
 */
public class DefaultNavPathApi implements NavPathApi {

    private final NavMeshContext context;
    private final DetourQueries detourQueries;
    private final Supplier<float[]> defaultExtentsSupplier;

    /**
     * 创建路径查询入口。
     *
     * @param context                NavMesh 与线程级查询上下文
     * @param detourQueries          Detour 组合查询实现
     * @param defaultExtentsSupplier 默认搜索范围提供者，保证 setExtents 后新值可立即生效
     */
    public DefaultNavPathApi(NavMeshContext context, DetourQueries detourQueries,
                             Supplier<float[]> defaultExtentsSupplier) {
        this.context = context;
        this.detourQueries = detourQueries;
        this.defaultExtentsSupplier = defaultExtentsSupplier;
    }

    @Override
    public List<Vector3f> find(Vector3f start, Vector3f end, NavOptions options) {
        NavOptions safeOptions = optionsOrDefault(options);
        List<float[]> points = detourQueries.find(context.query(), context.filter(), toFloatArray(start),
                toFloatArray(end), extents(safeOptions), safeOptions.getMaxStraightPath(),
                safeOptions.getStraightPathOptions(), safeOptions.getFindPathOptions(),
                safeOptions.getRaycastLimit());
        return toVectorList(points);
    }

    @Override
    public boolean isReachable(Vector3f start, Vector3f end, NavOptions options) {
        NavOptions safeOptions = optionsOrDefault(options);
        return detourQueries.isReachable(context.query(), context.filter(), toFloatArray(start), toFloatArray(end),
                extents(safeOptions), safeOptions.getFindPathOptions(), safeOptions.getRaycastLimit());
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

    private List<Vector3f> toVectorList(List<float[]> points) {
        List<Vector3f> vectors = new ArrayList<>();
        if (points == null) {
            return vectors;
        }
        for (float[] point : points) {
            if (point != null && point.length >= 3) {
                vectors.add(new Vector3f(point[0], point[1], point[2]));
            }
        }
        return vectors;
    }

    private static float[] toFloatArray(Vector3f vector) {
        if (vector == null) {
            return null;
        }
        return new float[]{vector.getX(), vector.getY(), vector.getZ()};
    }
}
