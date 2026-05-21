package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

import java.util.List;

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
    default boolean isWalkable(Vector3f point) {
        return isWalkable(point, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 判断点附近是否存在可行走 polygon。
     *
     * <p>当前只使用 `NavOptions.extents`；`maxStraightPath`、`straightPathOptions`、
     * `findPathOptions`、`raycastLimit` 对该 API 不生效。</p>
     *
     * @param point   待检测坐标
     * @param options 本次查询参数；传 null 时使用默认参数
     * @return 找到有效 polygon 返回 true，否则返回 false
     */
    boolean isWalkable(Vector3f point, NavOptions options);

    /**
     * 使用默认搜索范围把坐标吸附到最近的 NavMesh 表面。
     *
     * @param point 待修正坐标
     * @return 最近可行走点；找不到时返回 null
     */
    default Vector3f closestPoint(Vector3f point) {
        return closestPoint(point, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 把坐标吸附到最近的 NavMesh 表面。
     *
     * <p>当前只使用 `NavOptions.extents`；`maxStraightPath`、`straightPathOptions`、
     * `findPathOptions`、`raycastLimit` 对该 API 不生效。</p>
     *
     * @param point   待修正坐标
     * @param options 本次查询参数；传 null 时使用默认参数
     * @return 最近可行走点；找不到时返回 null
     */
    Vector3f closestPoint(Vector3f point, NavOptions options);

    /**
     * 使用默认搜索范围获取点附近 NavMesh 表面的高度。
     *
     * @param point 待查询坐标
     * @return 表面高度；找不到有效 polygon 或高度查询失败时返回 null
     */
    default Float getHeight(Vector3f point) {
        return getHeight(point, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 获取点附近 NavMesh 表面的高度。
     *
     * <p>当前只使用 `NavOptions.extents`；`maxStraightPath`、`straightPathOptions`、
     * `findPathOptions`、`raycastLimit` 对该 API 不生效。</p>
     *
     * @param point   待查询坐标
     * @param options 本次查询参数；传 null 时使用默认参数
     * @return 表面高度；找不到有效 polygon 或高度查询失败时返回 null
     */
    Float getHeight(Vector3f point, NavOptions options);

    /**
     * 判断中心点附近是否存在可走区域。
     *
     * <p>当前未接入 `NavOptions`，使用 Easy3dNav 默认搜索范围定位中心点。</p>
     *
     * @param center 中心点
     * @param radius 查询半径
     * @return 找到可走 polygon 返回 true；中心点无效、半径无效或查询失败时返回 false
     */
    boolean hasWalkableAround(Vector3f center, float radius);

    /**
     * 使用默认搜索范围查询中心点附近连通的局部邻域 polygon。
     *
     * @param center 中心点
     * @param radius 查询半径
     * @return 局部邻域 polygon 列表；中心点无效、半径无效或查询失败时返回空列表
     */
    default List<NavLocalPolygon> findLocalNeighbourhood(Vector3f center, float radius) {
        return findLocalNeighbourhood(center, radius, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 查询中心点附近连通的局部邻域 polygon。
     *
     * <p>当前只使用 `NavOptions.extents` 定位中心点 polygon；其他 NavOptions 参数对该 API
     * 不生效。返回结果只包含几何信息，不暴露 Detour 的 polygon ref、parent ref 或原始 result。</p>
     *
     * @param center  中心点
     * @param radius  查询半径
     * @param options 本次查询参数；传 null 或未设置 extents 时使用默认搜索范围
     * @return 局部邻域 polygon 列表；中心点无效、半径无效或查询失败时返回空列表
     */
    List<NavLocalPolygon> findLocalNeighbourhood(Vector3f center, float radius, NavOptions options);

    /**
     * 查询点到最近墙体或边界的距离。
     *
     * <p>当前未接入 `NavOptions`，使用 Easy3dNav 默认搜索范围定位查询点。</p>
     *
     * @param point  待查询点
     * @param radius 查询半径
     * @return 最近墙体距离；点无效、半径无效或查询失败时返回 null
     */
    Float distanceToWall(Vector3f point, float radius);

    /**
     * 查询最近墙体或边界的完整信息。
     *
     * <p>当前未接入 `NavOptions`，使用 Easy3dNav 默认搜索范围定位查询点。</p>
     *
     * @param point  待查询点
     * @param radius 查询半径
     * @return 最近墙体结果；点无效、半径无效或查询失败时返回 null
     */
    NavWallHit nearestWall(Vector3f point, float radius);
}
