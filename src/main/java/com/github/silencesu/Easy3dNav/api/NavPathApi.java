package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

import java.util.List;

/**
 * 路径相关 API。
 *
 * <p>该接口用于承载不适合继续堆到 Easy3dNav 门面类上的路径能力。</p>
 */
public interface NavPathApi {

    /**
     * 使用默认查询参数查找路径。
     *
     * @param start 起点坐标
     * @param end   终点坐标
     * @return 路径点列表；找不到路径时返回空列表
     */
    default List<Vector3f> find(Vector3f start, Vector3f end) {
        return find(start, end, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 查找路径。
     *
     * <p>支持参数：`extents`、`maxStraightPath`、`straightPathOptions`、
     * `findPathOptions`、`raycastLimit`。</p>
     *
     * @param start   起点坐标
     * @param end     终点坐标
     * @param options 本次查询参数；传 null 时使用默认参数
     * @return 路径点列表；找不到路径时返回空列表
     */
    List<Vector3f> find(Vector3f start, Vector3f end, NavOptions options);

    /**
     * 使用默认搜索范围判断两点是否可达。
     *
     * @param start 起点坐标
     * @param end   终点坐标
     * @return 起点和终点都有效且路径能到达终点 polygon 时返回 true
     */
    default boolean isReachable(Vector3f start, Vector3f end) {
        return isReachable(start, end, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 判断两点是否可达。
     *
     * <p>支持参数：`extents`、`findPathOptions`、`raycastLimit`。`maxStraightPath`
     * 和 `straightPathOptions` 对可达性判断不生效。</p>
     *
     * @param start   起点坐标
     * @param end     终点坐标
     * @param options 本次查询参数；传 null 时使用默认参数
     * @return 起点和终点都有效且路径能到达终点 polygon 时返回 true
     */
    boolean isReachable(Vector3f start, Vector3f end, NavOptions options);

    /**
     * 使用默认查询参数沿 NavMesh 表面做一次局部移动约束。
     *
     * <p>该方法适合服务端移动校验或位置修正，不替代完整路径查询。返回点不保证自动修正到
     * NavMesh 表面高度；如需贴地高度，可继续调用 `nav.query().getHeight` 或 `closestPoint`。</p>
     *
     * @param start 起点坐标
     * @param end   期望移动到的目标点
     * @return 实际可移动到的位置；起点无效、参数无效或查询失败时返回 null
     */
    default Vector3f moveAlongSurface(Vector3f start, Vector3f end) {
        return moveAlongSurface(start, end, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 沿 NavMesh 表面做一次局部移动约束。
     *
     * <p>当前仅支持 `NavOptions.extents`，用于定位起点 polygon。`maxStraightPath`、
     * `straightPathOptions`、`findPathOptions`、`raycastLimit` 对该 API 不生效。</p>
     *
     * <p>该方法返回 recast4j `moveAlongSurface` 的结果位置，不暴露 visited polygon refs，
     * 也不额外修正 Y 高度。</p>
     *
     * @param start   起点坐标
     * @param end     期望移动到的目标点
     * @param options 本次查询参数；传 null 时使用默认参数
     * @return 实际可移动到的位置；起点无效、参数无效或查询失败时返回 null
     */
    Vector3f moveAlongSurface(Vector3f start, Vector3f end, NavOptions options);
}
