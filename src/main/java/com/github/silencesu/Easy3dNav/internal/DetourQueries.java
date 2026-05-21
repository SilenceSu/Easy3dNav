package com.github.silencesu.Easy3dNav.internal;

import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavWallHit;
import org.recast4j.detour.ClosestPointOnPolyResult;
import org.recast4j.detour.FindDistanceToWallResult;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.FindPolysAroundResult;
import org.recast4j.detour.FindRandomPointResult;
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
import java.util.concurrent.ThreadLocalRandom;

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
        return find(query, filter, start, end, extents, Integer.MAX_VALUE, 0, 0, 0f);
    }

    /**
     * 查找路径并允许调用方通过 NavOptions 控制 Detour 路径参数。
     *
     * <p>findPathOptions 和 raycastLimit 直接作用于 Detour findPath；maxStraightPath 和
     * straightPathOptions 作用于 Detour findStraightPath。公开 API 仍只返回坐标点。</p>
     */
    public List<float[]> find(NavMeshQuery query, QueryFilter filter, float[] start, float[] end, float[] extents,
                              int maxStraightPath, int straightPathOptions, int findPathOptions,
                              float raycastLimit) {
        List<float[]> pathRet = new ArrayList<>();
        if (maxStraightPath <= 0) {
            return pathRet;
        }

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
                startResult.getNearestPos(), endResult.getNearestPos(), filter, findPathOptions, raycastLimit);

        if (path.status != Status.SUCCSESS || path.result == null) {
            LOGGER.info("nav path  status is  {}", path.status);
            return Collections.emptyList();
        }

        // 将 polygon path 拉直为路径点，Easy3dNav 对外只返回坐标，不暴露 Detour path item。
        Result<List<StraightPathItem>> straightPathQuery = query.findStraightPath(startResult.getNearestPos(),
                endResult.getNearestPos(), path.result, maxStraightPath, straightPathOptions);
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
        return isReachable(query, filter, start, end, extents, 0, 0f);
    }

    /**
     * 使用 Detour findPath 参数判断两点是否连通。
     */
    public boolean isReachable(NavMeshQuery query, QueryFilter filter, float[] start, float[] end, float[] extents,
                               int findPathOptions, float raycastLimit) {
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
                startNearest.getNearestPos(), endNearest.getNearestPos(), filter, findPathOptions, raycastLimit);
        if (pathQuery.failed() || pathQuery.result == null || pathQuery.result.isEmpty()) {
            return false;
        }
        return pathQuery.result.get(pathQuery.result.size() - 1) == endRef;
    }

    /**
     * 在整张 NavMesh 上随机取一个可走点。
     *
     * <p>Detour 返回随机 polygon ref 和坐标。Easy3dNav 对外只需要坐标，因此这里丢弃 ref，
     * 并在查询失败、ref 无效或坐标为空时统一返回 null。</p>
     */
    public float[] randomPoint(NavMeshQuery query, QueryFilter filter) {
        Result<FindRandomPointResult> randomQuery = query.findRandomPoint(filter, randomSource());
        return randomPoint(randomQuery);
    }

    /**
     * 在中心点附近随机取一个可走点。
     *
     * <p>findRandomPointAroundCircle 需要中心点所在 polygon ref，所以先通过默认 extents 定位中心点。
     * radius 小于等于 0 时按失败处理，避免把无意义半径传给 Detour。</p>
     */
    public float[] randomPointAround(NavMeshQuery query, QueryFilter filter, float[] center, float[] extents,
                                     float radius) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, center, extents);
        if (nearest == null || nearest.getNearestRef() == 0 || invalidRadius(radius)) {
            return null;
        }

        Result<FindRandomPointResult> randomQuery = query.findRandomPointAroundCircle(nearest.getNearestRef(),
                nearest.getNearestPos(), radius, filter, randomSource());
        return randomPoint(randomQuery);
    }

    /**
     * 在指定圆形范围内随机取一个可走点。
     *
     * <p>该方法使用 recast4j 的 within circle 约束，语义上比 around circle 更适合“必须在半径内”
     * 的业务，例如区域内随机刷怪或技能落点。</p>
     */
    public float[] randomPointWithin(NavMeshQuery query, QueryFilter filter, float[] center, float[] extents,
                                     float radius) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, center, extents);
        if (nearest == null || nearest.getNearestRef() == 0 || invalidRadius(radius)) {
            return null;
        }

        Result<FindRandomPointResult> randomQuery = query.findRandomPointWithinCircle(nearest.getNearestRef(),
                nearest.getNearestPos(), radius, filter, randomSource());
        return randomPoint(randomQuery);
    }

    /**
     * 判断中心点附近是否存在可走 polygon。
     *
     * <p>findPolysAroundCircle 返回 refs、parentRefs 和 costs。第一版公开 API 只需要布尔语义，
     * 因此这里只检查 refs 是否非空，不把 Detour 的 polygon 结构暴露出去。</p>
     */
    public boolean hasWalkableAround(NavMeshQuery query, QueryFilter filter, float[] center, float[] extents,
                                     float radius) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, center, extents);
        if (nearest == null || nearest.getNearestRef() == 0 || invalidRadius(radius)) {
            return false;
        }

        Result<FindPolysAroundResult> aroundQuery = query.findPolysAroundCircle(nearest.getNearestRef(),
                nearest.getNearestPos(), radius, filter);
        return !aroundQuery.failed() && aroundQuery.result != null && aroundQuery.result.getRefs() != null
                && !aroundQuery.result.getRefs().isEmpty();
    }

    /**
     * 查询点到最近墙体或边界的距离。
     */
    public Float distanceToWall(NavMeshQuery query, QueryFilter filter, float[] point, float[] extents,
                                float radius) {
        NavWallHit hit = nearestWall(query, filter, point, extents, radius);
        if (hit == null) {
            return null;
        }
        return hit.getDistance();
    }

    /**
     * 查询最近墙体或边界的完整信息。
     *
     * <p>findDistanceToWall 同时返回距离、墙点和法线。这里先定位点所在 polygon，再将 Detour 的
     * float 数组结果转换成 Easy3dNav 的 NavWallHit，公开 API 不需要感知 FindDistanceToWallResult。</p>
     */
    public NavWallHit nearestWall(NavMeshQuery query, QueryFilter filter, float[] point, float[] extents,
                                  float radius) {
        FindNearestPolyResult nearest = findNearestPoly(query, filter, point, extents);
        if (nearest == null || nearest.getNearestRef() == 0 || invalidRadius(radius)) {
            return null;
        }

        Result<FindDistanceToWallResult> wallQuery = query.findDistanceToWall(nearest.getNearestRef(),
                nearest.getNearestPos(), radius, filter);
        if (wallQuery.failed() || wallQuery.result == null) {
            return null;
        }
        FindDistanceToWallResult result = wallQuery.result;
        if (result.getPosition() == null || result.getNormal() == null) {
            return null;
        }
        return new NavWallHit(result.getDistance(), toVector3f(result.getPosition()), toVector3f(result.getNormal()));
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

    private float[] randomPoint(Result<FindRandomPointResult> randomQuery) {
        if (randomQuery.failed() || randomQuery.result == null || randomQuery.result.getRandomRef() == 0) {
            return null;
        }
        return randomQuery.result.getRandomPt();
    }

    private boolean invalidRadius(float radius) {
        return radius <= 0f;
    }

    private NavMeshQuery.FRand randomSource() {
        return new NavMeshQuery.FRand(ThreadLocalRandom.current().nextLong());
    }

    private Vector3f toVector3f(float[] point) {
        return new Vector3f(point[0], point[1], point[2]);
    }
}
