package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * 基础位置查询 API。
 *
 * <p>该接口面向业务调用方，只暴露坐标和简单返回值，不暴露 recast4j 的 polygon ref、
 * NavMeshQuery 或 Result 等底层类型。</p>
 */
public interface NavQueryApi {

    /**
     * 使用默认搜索范围判断点附近是否存在可行走 polygon。
     *
     * @param point 待检测坐标
     * @return 找到有效 polygon 返回 true，否则返回 false
     */
    boolean isWalkable(Vector3f point);

    /**
     * 使用显式搜索范围判断点附近是否存在可行走 polygon。
     *
     * @param point   待检测坐标
     * @param extents 搜索范围
     * @return 找到有效 polygon 返回 true，否则返回 false
     */
    boolean isWalkable(Vector3f point, Vector3f extents);

    /**
     * 使用默认搜索范围把坐标吸附到最近的 NavMesh 表面。
     *
     * @param point 待修正坐标
     * @return 最近可行走点；找不到时返回 null
     */
    Vector3f closestPoint(Vector3f point);

    /**
     * 使用显式搜索范围把坐标吸附到最近的 NavMesh 表面。
     *
     * @param point   待修正坐标
     * @param extents 搜索范围
     * @return 最近可行走点；找不到时返回 null
     */
    Vector3f closestPoint(Vector3f point, Vector3f extents);

    /**
     * 使用默认搜索范围获取点附近 NavMesh 表面的高度。
     *
     * @param point 待查询坐标
     * @return 表面高度；找不到有效 polygon 或高度查询失败时返回 null
     */
    Float getHeight(Vector3f point);

    /**
     * 使用显式搜索范围获取点附近 NavMesh 表面的高度。
     *
     * @param point   待查询坐标
     * @param extents 搜索范围
     * @return 表面高度；找不到有效 polygon 或高度查询失败时返回 null
     */
    Float getHeight(Vector3f point, Vector3f extents);
}
