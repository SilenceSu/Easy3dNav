package com.github.silencesu.Easy3dNav.internal;

import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavRandomApi;

import java.util.function.Supplier;

/**
 * NavRandomApi 的默认实现。
 *
 * <p>该类把公开的 Vector3f 参数转换为 Detour 需要的 float 数组，并通过 NavMeshContext
 * 使用当前线程独立的 NavMeshQuery，避免查询临时状态在线程之间共享。</p>
 */
public class DefaultNavRandomApi implements NavRandomApi {

    private final NavMeshContext context;
    private final DetourQueries detourQueries;
    private final Supplier<float[]> defaultExtentsSupplier;

    /**
     * 创建随机点查询入口。
     *
     * @param context                NavMesh 与线程级查询上下文
     * @param detourQueries          Detour 组合查询实现
     * @param defaultExtentsSupplier 默认搜索范围提供者，保证 setExtents 后新值可立即生效
     */
    public DefaultNavRandomApi(NavMeshContext context, DetourQueries detourQueries,
                               Supplier<float[]> defaultExtentsSupplier) {
        this.context = context;
        this.detourQueries = detourQueries;
        this.defaultExtentsSupplier = defaultExtentsSupplier;
    }

    @Override
    public Vector3f point() {
        return toVector3f(detourQueries.randomPoint(context.query(), context.filter()));
    }

    @Override
    public Vector3f pointAround(Vector3f center, float radius) {
        return toVector3f(detourQueries.randomPointAround(context.query(), context.filter(), toFloatArray(center),
                defaultExtents(), radius));
    }

    @Override
    public Vector3f pointWithin(Vector3f center, float radius) {
        return toVector3f(detourQueries.randomPointWithin(context.query(), context.filter(), toFloatArray(center),
                defaultExtents(), radius));
    }

    private float[] defaultExtents() {
        return defaultExtentsSupplier.get();
    }

    private static float[] toFloatArray(Vector3f vector) {
        if (vector == null) {
            return null;
        }
        return new float[]{vector.getX(), vector.getY(), vector.getZ()};
    }

    private static Vector3f toVector3f(float[] point) {
        if (point == null) {
            return null;
        }
        return new Vector3f(point[0], point[1], point[2]);
    }
}
