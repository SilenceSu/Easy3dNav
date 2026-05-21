package com.github.silencesu.Easy3dNav.internal;

import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavPathApi;

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
    public boolean isReachable(Vector3f start, Vector3f end) {
        return isReachableArray(toFloatArray(start), toFloatArray(end), defaultExtents());
    }

    @Override
    public boolean isReachable(Vector3f start, Vector3f end, Vector3f extents) {
        return isReachableArray(toFloatArray(start), toFloatArray(end), toFloatArray(extents));
    }

    private float[] defaultExtents() {
        return defaultExtentsSupplier.get();
    }

    private boolean isReachableArray(float[] start, float[] end, float[] extents) {
        return detourQueries.isReachable(context.query(), context.filter(), start, end, extents);
    }

    private static float[] toFloatArray(Vector3f vector) {
        if (vector == null) {
            return null;
        }
        return new float[]{vector.getX(), vector.getY(), vector.getZ()};
    }
}
