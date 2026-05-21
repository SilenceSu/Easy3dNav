package com.github.silencesu.Easy3dNav.internal;

import org.recast4j.detour.ClosestPointOnPolyResult;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.RaycastHit;
import org.recast4j.detour.Result;
import org.recast4j.detour.Status;
import org.recast4j.detour.StraightPathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 封装 recast4j Detour 的底层查询流程。
 *
 * <p>该类属于 internal 实现细节：对外 API 仍由 Easy3dNav 暴露，调用方不需要直接接触
 * NavMeshQuery、QueryFilter、polygon ref 或 recast4j Result。</p>
 */
public class DetourQueries {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetourQueries.class);

    /**
     * 查找路径并返回平滑后的路径点。
     *
     * <p>Detour 的完整流程是：先把起点和终点吸附到最近 polygon，再查找 polygon path，
     * 最后把 polygon path 转成 straight path 点列表。</p>
     */
    public List<float[]> find(NavMeshQuery query, QueryFilter filter, float[] start, float[] end, float[] extents) {
        List<float[]> pathRet = new ArrayList<>();

        // 起点和终点都必须先找到可用 polygon，后续 findPath 才有合法的起止 ref。
        Result<FindNearestPolyResult> startQuery = query.findNearestPoly(start, extents, filter);
        if (startQuery.failed() || startQuery.result == null) {
            LOGGER.info("start point not found poly");
            return Collections.emptyList();
        }
        FindNearestPolyResult startResult = startQuery.result;

        Result<FindNearestPolyResult> endQuery = query.findNearestPoly(end, extents, filter);
        if (endQuery.failed() || endQuery.result == null) {
            LOGGER.info("end point not found poly");
            return Collections.emptyList();
        }
        FindNearestPolyResult endResult = endQuery.result;

        if (startResult.getNearestRef() == 0 || endResult.getNearestRef() == 0) {
            LOGGER.info("start or end point not found poly");
            return Collections.emptyList();
        }

        // findPath 返回的是 polygon ref 列表，还不是最终可直接给业务使用的坐标点。
        Result<List<Long>> path = query.findPath(startResult.getNearestRef(), endResult.getNearestRef(),
                startResult.getNearestPos(), endResult.getNearestPos(), filter);

        if (path.status != Status.SUCCSESS || path.result == null) {
            LOGGER.info("nav path  status is  {}", path.status);
            return Collections.emptyList();
        }

        // 将 polygon path 拉直为路径点，Easy3dNav 对外只返回坐标，不暴露 Detour path item。
        Result<List<StraightPathItem>> straightPathQuery = query.findStraightPath(startResult.getNearestPos(),
                endResult.getNearestPos(), path.result, Integer.MAX_VALUE, 0);
        if (straightPathQuery.failed() || straightPathQuery.result == null) {
            LOGGER.info("straight path status is {}", straightPathQuery.status);
            return Collections.emptyList();
        }

        for (StraightPathItem straightPathItem : straightPathQuery.result) {
            float[] p = new float[3];
            System.arraycopy(straightPathItem.getPos(), 0, p, 0, straightPathItem.getPos().length);
            pathRet.add(p);
        }
        return pathRet;
    }

    /**
     * 沿 NavMesh 做射线检测。
     *
     * <p>返回值保持 Easy3dNav 旧语义：没有阻挡时返回终点；有阻挡时按 Detour 返回的 t
     * 计算命中点。</p>
     */
    public float[] raycast(NavMeshQuery query, QueryFilter filter, float[] start, float[] end, float[] extents) {
        // raycast 需要从一个有效 polygon ref 开始，因此先定位起点所在 polygon。
        Result<FindNearestPolyResult> startQuery = query.findNearestPoly(start, extents, filter);
        if (startQuery.failed() || startQuery.result == null) {
            throw new IllegalArgumentException("not find nearestPoly");
        }
        FindNearestPolyResult startResult = startQuery.result;
        if (startResult.getNearestRef() == 0) {
            throw new IllegalArgumentException("not find nearestPoly");
        }

        Result<RaycastHit> hitQuery = query.raycast(startResult.getNearestRef(), startResult.getNearestPos(), end,
                filter, 0, 0);
        if (hitQuery.failed() || hitQuery.result == null) {
            throw new IllegalStateException("raycast failed");
        }
        RaycastHit hitResult = hitQuery.result;
        // t > 1 表示从起点到终点没有被墙或边界阻挡。
        if (hitResult.t > 1) {
            return end;
        }

        // Detour 返回的是 0..1 的插值比例，这里转换成 Easy3dNav 对外使用的坐标点。
        float[] hitPoint = new float[3];
        hitPoint[0] = start[0] + (end[0] - start[0]) * hitResult.t;
        hitPoint[1] = start[1] + (end[1] - start[1]) * hitResult.t;
        hitPoint[2] = start[2] + (end[2] - start[2]) * hitResult.t;
        return hitPoint;
    }

    /**
     * 查找指定点附近最近的可行走点。
     */
    public float[] findNearest(NavMeshQuery query, QueryFilter filter, float[] point, float[] extents) {
        Result<FindNearestPolyResult> queryResult = query.findNearestPoly(point, extents, filter);
        if (queryResult.failed() || queryResult.result == null) {
            return null;
        }
        return queryResult.result.getNearestPos();
    }

    /**
     * 判断指定点附近是否存在有效可行走 polygon。
     *
     * <p>公开 API 不暴露 polygon ref，这里只把 Detour 的 nearestRef 转成布尔语义。</p>
     */
    public boolean isWalkable(NavMeshQuery query, QueryFilter filter, float[] point, float[] extents) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, point, extents);
        return nearest != null && nearest.getNearestRef() != 0;
    }

    /**
     * 把指定点吸附到最近 polygon 上的可行走位置。
     *
     * <p>Detour 的 closestPointOnPoly 需要 polygon ref，所以先通过 findNearestPoly 定位 polygon，
     * 再把真实吸附点返回给上层。</p>
     */
    public float[] closestPoint(NavMeshQuery query, QueryFilter filter, float[] point, float[] extents) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, point, extents);
        if (nearest == null || nearest.getNearestRef() == 0) {
            return null;
        }

        Result<ClosestPointOnPolyResult> closestQuery = query.closestPointOnPoly(nearest.getNearestRef(), point);
        if (closestQuery.failed() || closestQuery.result == null) {
            return null;
        }
        return closestQuery.result.getClosest();
    }

    /**
     * 查询指定点附近 NavMesh 表面的高度。
     *
     * <p>这里使用 findNearestPoly 返回的 nearestPos 调用 getPolyHeight，确保传入点已经落在目标
     * polygon 的 XZ 范围内，避免外部点只靠 extents 命中 polygon 时高度查询失败。</p>
     */
    public Float getHeight(NavMeshQuery query, QueryFilter filter, float[] point, float[] extents) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, point, extents);
        if (nearest == null || nearest.getNearestRef() == 0) {
            return null;
        }

        Result<Float> heightQuery = query.getPolyHeight(nearest.getNearestRef(), nearest.getNearestPos());
        if (heightQuery.failed() || heightQuery.result == null) {
            return null;
        }
        return heightQuery.result;
    }

    /**
     * 判断两点是否能通过 NavMesh 连通。
     *
     * <p>findPath 可能返回部分路径，因此除了查询成功外，还要检查返回的 polygon path 最后一个
     * ref 是否就是终点 polygon ref。</p>
     */
    public boolean isReachable(NavMeshQuery query, QueryFilter filter, float[] start, float[] end, float[] extents) {
        FindNearestPolyResult startNearest = findNearestPoly(query, filter, start, extents);
        FindNearestPolyResult endNearest = findNearestPoly(query, filter, end, extents);
        if (startNearest == null || endNearest == null) {
            return false;
        }

        long startRef = startNearest.getNearestRef();
        long endRef = endNearest.getNearestRef();
        if (startRef == 0 || endRef == 0) {
            return false;
        }
        if (startRef == endRef) {
            return true;
        }

        Result<List<Long>> pathQuery = query.findPath(startRef, endRef,
                startNearest.getNearestPos(), endNearest.getNearestPos(), filter);
        if (pathQuery.failed() || pathQuery.result == null || pathQuery.result.isEmpty()) {
            return false;
        }
        return pathQuery.result.get(pathQuery.result.size() - 1) == endRef;
    }

    private FindNearestPolyResult findNearestPoly(NavMeshQuery query, QueryFilter filter, float[] point,
                                                  float[] extents) {
        if (point == null || extents == null) {
            return null;
        }
        Result<FindNearestPolyResult> queryResult = query.findNearestPoly(point, extents, filter);
        if (queryResult.failed() || queryResult.result == null) {
            return null;
        }
        return queryResult.result;
    }
}
