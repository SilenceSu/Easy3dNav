package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * 路径相关 API。
 *
 * <p>该接口用于承载不适合继续堆到 Easy3dNav 门面类上的路径能力。</p>
 */
public interface NavPathApi {

    /**
     * 使用默认搜索范围判断两点是否可达。
     *
     * @param start 起点坐标
     * @param end   终点坐标
     * @return 起点和终点都有效且路径能到达终点 polygon 时返回 true
     */
    boolean isReachable(Vector3f start, Vector3f end);

    /**
     * 使用显式搜索范围判断两点是否可达。
     *
     * @param start   起点坐标
     * @param end     终点坐标
     * @param extents 搜索范围
     * @return 起点和终点都有效且路径能到达终点 polygon 时返回 true
     */
    boolean isReachable(Vector3f start, Vector3f end, Vector3f extents);
}
