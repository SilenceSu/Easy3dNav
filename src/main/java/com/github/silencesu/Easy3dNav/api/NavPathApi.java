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
}
