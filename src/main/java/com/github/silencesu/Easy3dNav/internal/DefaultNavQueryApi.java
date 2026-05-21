package com.github.silencesu.Easy3dNav.internal;

import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavQueryApi;
import com.github.silencesu.Easy3dNav.api.NavWallHit;

import java.util.function.Supplier;

/**
 * NavQueryApi 的默认实现。
 *
 * <p>该类只负责把公开 API 参数转换为 Detour 查询层需要的 float 数组，并复用
 * NavMeshContext 中当前线程的 NavMeshQuery。</p>
 */
public class DefaultNavQueryApi implements NavQueryApi {

    private final NavMeshContext context;
    private final DetourQueries detourQueries;
    private final Supplier<float[]> defaultExtentsSupplier;

    /**
     * 创建基础位置查询入口。
     *
     * @param context                NavMesh 与线程级查询上下文
     * @param detourQueries          Detour 组合查询实现
     * @param defaultExtentsSupplier 默认搜索范围提供者，保证 setExtents 后新值可立即生效
     */
    public DefaultNavQueryApi(NavMeshContext context, DetourQueries detourQueries,
                              Supplier<float[]> defaultExtentsSupplier) {
        this.context = context;
        this.detourQueries = detourQueries;
        this.defaultExtentsSupplier = defaultExtentsSupplier;
    }

    @Override
    public boolean isWalkable(Vector3f point) {
        return isWalkableArray(toFloatArray(point), defaultExtents());
    }

    @Override
    public boolean isWalkable(Vector3f point, Vector3f extents) {
        return isWalkableArray(toFloatArray(point), toFloatArray(extents));
    }

    @Override
    public Vector3f closestPoint(Vector3f point) {
        return toVector3f(closestPointArray(toFloatArray(point), defaultExtents()));
    }

    @Override
    public Vector3f closestPoint(Vector3f point, Vector3f extents) {
        return toVector3f(closestPointArray(toFloatArray(point), toFloatArray(extents)));
    }

    @Override
    public Float getHeight(Vector3f point) {
        return getHeightArray(toFloatArray(point), defaultExtents());
    }

    @Override
    public Float getHeight(Vector3f point, Vector3f extents) {
        return getHeightArray(toFloatArray(point), toFloatArray(extents));
    }

    @Override
    public boolean hasWalkableAround(Vector3f center, float radius) {
        return detourQueries.hasWalkableAround(context.query(), context.filter(), toFloatArray(center),
                defaultExtents(), radius);
    }

    @Override
    public Float distanceToWall(Vector3f point, float radius) {
        NavWallHit hit = nearestWall(point, radius);
        if (hit == null) {
            return null;
        }
        return hit.getDistance();
    }

    @Override
    public NavWallHit nearestWall(Vector3f point, float radius) {
        return detourQueries.nearestWall(context.query(), context.filter(), toFloatArray(point), defaultExtents(),
                radius);
    }

    private float[] defaultExtents() {
        return defaultExtentsSupplier.get();
    }

    private boolean isWalkableArray(float[] point, float[] extents) {
        return detourQueries.isWalkable(context.query(), context.filter(), point, extents);
    }

    private float[] closestPointArray(float[] point, float[] extents) {
        return detourQueries.closestPoint(context.query(), context.filter(), point, extents);
    }

    private Float getHeightArray(float[] point, float[] extents) {
        return detourQueries.getHeight(context.query(), context.filter(), point, extents);
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
