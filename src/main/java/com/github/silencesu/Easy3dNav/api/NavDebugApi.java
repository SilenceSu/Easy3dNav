package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

import java.util.List;

/**
 * 调试和可视化 API。
 *
 * <p>该接口用于承载墙体线段、NavMesh 元数据等偏诊断能力。公开方法只暴露
 * Easy3dNav 自有类型，不暴露 recast4j 的 NavMesh、polygon ref、Result 或 float 数组。</p>
 */
public interface NavDebugApi {

    /**
     * 使用默认搜索范围查询指定点所在 polygon 的墙体或边界线段。
     *
     * @param point 待查询坐标
     * @return 墙体或边界线段列表；点无效、未找到 polygon 或 Detour 查询失败时返回空列表
     */
    default List<NavWallSegment> getWallSegments(Vector3f point) {
        return getWallSegments(point, NavOptions.defaults());
    }

    /**
     * 使用 NavOptions 查询指定点所在 polygon 的墙体或边界线段。
     *
     * <p>当前只使用 `NavOptions.extents` 定位目标 polygon；其他 NavOptions 参数对该调试 API
     * 不生效。返回值不包含 portal ref、polygon ref 或 recast4j 原始结果。</p>
     *
     * @param point   待查询坐标
     * @param options 本次查询参数；传 null 或未设置 extents 时使用默认搜索范围
     * @return 墙体或边界线段列表；点无效、未找到 polygon 或 Detour 查询失败时返回空列表
     */
    List<NavWallSegment> getWallSegments(Vector3f point, NavOptions options);

    /**
     * 返回当前 NavMesh 的结构化调试元数据。
     *
     * @return NavMesh 元数据；未初始化时抛出 IllegalStateException
     */
    NavMeshInfo meshInfo();
}
