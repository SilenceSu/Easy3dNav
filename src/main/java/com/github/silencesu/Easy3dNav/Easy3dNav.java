package com.github.silencesu.Easy3dNav;

import com.github.silencesu.Easy3dNav.io.MeshSetReaderU3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.RaycastHit;
import org.recast4j.detour.Result;
import org.recast4j.detour.Status;
import org.recast4j.detour.StraightPathItem;
import org.recast4j.detour.io.MeshSetReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 3d游戏服务端寻路组件
 *
 * @author SilenceSu
 * @Email Silence.Sx@Gmail.com
 * Created by Silence on 2018/5/3.
 */
public class Easy3dNav implements EasyNavFunc {

    private static Logger LOGGER = LoggerFactory.getLogger(Easy3dNav.class);


    /**
     * 是否使用U3d插件CritterAI导出的格式
     */
    private boolean useU3dData = true;
    /**
     * 是否打印地图信息
     */
    private boolean printMeshInfo = false;

    private NavMesh navMesh;
    private ThreadLocal<NavMeshQuery> queryLocal;
    private QueryFilter filter;

    private float[] extents = {2.f, 2.f, 2.f};


    public Easy3dNav() {

    }

    /**
     * 修改配置 构造
     * @param useU3dData  是否使用critterAi数据
     * @param printMeshInfo  是否打印地图西信息
     */
    public Easy3dNav(boolean useU3dData, boolean printMeshInfo) {
        this.useU3dData = useU3dData;
        this.printMeshInfo = printMeshInfo;
    }

    //navmesh 文件路径
    public Easy3dNav(String filePath) throws IOException {
        init(filePath);
    }

    /**
     * 初始化寻路需要参数
     *
     * @param filePath
     */
    public void init(String filePath) throws IOException {

        clearThreadLocalQuery();

        NavMesh mesh = loadNavMesh(filePath);
        navMesh = mesh;
        queryLocal = ThreadLocal.withInitial(() -> new NavMeshQuery(mesh));
        filter = createQueryFilter();

        if (printMeshInfo) {
            printMeshInfo(mesh);
        }

    }

    private NavMesh loadNavMesh(String meshFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(meshFile))) {
            //获取文件流
            if (useU3dData) {
                MeshSetReaderU3d reader = new MeshSetReaderU3d();
                return reader.read32Bit(inputStream, 6);
            }
            MeshSetReader reader = new MeshSetReader();
            return reader.read32Bit(inputStream, 6);
        }
    }

    /**
     * 清理当前线程缓存的查询上下文。使用线程池或重载地图时可主动调用。
     */
    public void clearThreadLocalQuery() {
        if (queryLocal != null) {
            queryLocal.remove();
        }
    }

    private NavMeshQuery query() {
        if (queryLocal == null) {
            throw new IllegalStateException("Easy3dNav is not initialized");
        }
        return queryLocal.get();
    }

    private QueryFilter filter() {
        if (filter == null) {
            throw new IllegalStateException("Easy3dNav is not initialized");
        }
        return filter;
    }

    private QueryFilter createQueryFilter() {
        return new DefaultQueryFilter();
    }

    /**
     * 输出mesh信息
     *
     * @param mesh meshd对象
     */
    private void printMeshInfo(NavMesh mesh) {
        //输出地图基本信息
        int tileCount = 0;
        int nodeCount = 0;
        int polyCount = 0;
        int vertCount = 0;
        int triCount = 0;
        int triVertCount = 0;
        int dataSize = 0;
        for (int i = 0; i < mesh.getMaxTiles(); i++) {

            MeshTile tile = mesh.getTile(i);
            if (tile == null || tile.data == null) {
                continue;
            }
            tileCount++;
            nodeCount += tile.data.header.bvNodeCount;
            polyCount += tile.data.header.polyCount;
            vertCount += tile.data.header.vertCount;
            triCount += tile.data.header.detailTriCount;
            triVertCount += tile.data.header.detailVertCount;


            System.out.printf("%f %f %f\n", tile.data.verts[0], tile.data.verts[1], tile.data.verts[2]);
            for (int m = 0; m < tile.data.header.detailVertCount; m++) {
                System.out.printf("%f %f %f\n", tile.data.detailVerts[m * 3], tile.data.detailVerts[m * 3 + 1], tile.data.detailVerts[m * 3 + 2]);
            }
        }

        System.out.printf("\t==> tiles loaded: %d\n", tileCount);
        System.out.printf("\t==> BVTree nodes: %d\n", nodeCount);
        System.out.printf("\t==> %d polygons (%d vertices)\n", polyCount, vertCount);

        System.out.printf("\t==> %d triangles (%d vertices)\n", triCount, triVertCount);
    }

    /**
     * 设置默认搜索范围
     *
     * @param extents 搜索范围
     */
    public void setExtents(float[] extents) {
        this.extents = extents;
    }

    /**
     * 设置是否使用critterAI导出的数据
     *
     * @param useU3dData
     */
    public void setUseU3dData(boolean useU3dData) {
        this.useU3dData = useU3dData;
    }

    public void setPrintMeshInfo(boolean printMeshInfo) {
        this.printMeshInfo = printMeshInfo;
    }

    @Override
    public List<float[]> find(float[] start, float[] end, float[] extents) {


        List<float[]> pathRet = new ArrayList<>();
        NavMeshQuery query = query();
        QueryFilter filter = filter();

        //获取开始点，附近的点
        Result<FindNearestPolyResult> startQuery = query.findNearestPoly(start, extents, filter);
        if (startQuery.failed() || startQuery.result == null) {
            LOGGER.info("start point not found poly");
            return Collections.emptyList();
        }
        FindNearestPolyResult startResult = startQuery.result;
        //获取结束点，附近的点
        Result<FindNearestPolyResult> endQuery = query.findNearestPoly(end, extents, filter);
        if (endQuery.failed() || endQuery.result == null) {
            LOGGER.info("end point not found poly");
            return Collections.emptyList();
        }
        FindNearestPolyResult endResult = endQuery.result;

        //寻找开始点和结束点附近的多边形
        if (startResult.getNearestRef() == 0 || endResult.getNearestRef() == 0) {
            LOGGER.info("start or end point not found poly");
            return Collections.emptyList();
        }

        //获取路径ids
        Result<List<Long>> path = query.findPath(startResult.getNearestRef(), endResult.getNearestRef(), startResult.getNearestPos(), endResult.getNearestPos(), filter);

        if (path.status != Status.SUCCSESS || path.result == null) {
            LOGGER.info("nav path  status is  {}", path.status);
            return Collections.emptyList();
        }

        //路径平滑
        Result<List<StraightPathItem>> straightPathQuery = query.findStraightPath(startResult.getNearestPos(), endResult.getNearestPos(), path.result, Integer.MAX_VALUE, 0);
        if (straightPathQuery.failed() || straightPathQuery.result == null) {
            LOGGER.info("straight path status is {}", straightPathQuery.status);
            return Collections.emptyList();
        }
        List<StraightPathItem> straightPath = straightPathQuery.result;
        for (StraightPathItem straightPathItem : straightPath) {
            float[] p = new float[3];
            System.arraycopy(straightPathItem.getPos(), 0, p, 0, straightPathItem.getPos().length);
            pathRet.add(p);
        }
        return pathRet;
    }

    @Override
    public List<Vector3f> find(Vector3f start, Vector3f end, Vector3f extents) {
        List<float[]> paths = find(v3fToFArr(start), v3fToFArr(end), v3fToFArr(extents));
        return paths.stream().map(p -> new Vector3f(p[0], p[1], p[2])).collect(Collectors.toList());
    }


    public List<float[]> find(float[] start, float[] end) {
        return find(start, end, extents);
    }

    @Override
    public List<Vector3f> find(Vector3f start, Vector3f end) {
        return find(start, end, FArrTov3f(extents));
    }


    @Override
    public float[] raycast(float[] start, float[] end, float[] extents) {
        NavMeshQuery query = query();
        QueryFilter filter = filter();
        Result<FindNearestPolyResult> startQuery = query.findNearestPoly(start, extents, filter);
        if (startQuery.failed() || startQuery.result == null) {
            throw new IllegalArgumentException("not find nearestPoly");
        }
        FindNearestPolyResult startResult = startQuery.result;
        if (startResult.getNearestRef() == 0) {
            throw new IllegalArgumentException("not find nearestPoly");
        }
        float[] hitPoint = null;

        Result<RaycastHit> hitQuery = query.raycast(startResult.getNearestRef(), startResult.getNearestPos(), end, filter, 0, 0);
        if (hitQuery.failed() || hitQuery.result == null) {
            throw new IllegalStateException("raycast failed");
        }
        RaycastHit hitReasult = hitQuery.result;
        if (hitReasult.t > 1) {
            return end;
        } else {
            hitPoint = new float[3];
            hitPoint[0] = start[0] + (end[0] - start[0]) * hitReasult.t;
            hitPoint[1] = start[1] + (end[1] - start[1]) * hitReasult.t;
            hitPoint[2] = start[2] + (end[2] - start[2]) * hitReasult.t;
        }
        return hitPoint;
    }

    @Override
    public Vector3f raycast(Vector3f start, Vector3f end, Vector3f extents) {
        float[] point = raycast(v3fToFArr(start), v3fToFArr(end), v3fToFArr(extents));
        return FArrTov3f(point);
    }

    @Override
    public float[] raycast(float[] start, float[] end) {
        return raycast(start, end, new float[]{0.f, 2.f, 0.f});
    }

    @Override
    public Vector3f raycast(Vector3f start, Vector3f end) {
        return raycast(start, end, FArrTov3f(extents));
    }


    @Override
    public float[] findNearest(float[] point) {
        NavMeshQuery query = query();
        QueryFilter filter = filter();
        Result<FindNearestPolyResult> queryResult = query.findNearestPoly(point, extents, filter);
        if (queryResult.failed() || queryResult.result == null) {
            return null;
        }
        FindNearestPolyResult result = queryResult.result;
        return result.getNearestPos();
    }

    @Override
    public Vector3f findNearest(Vector3f point) {
        float[] p = findNearest(v3fToFArr(point));
        return FArrTov3f(p);
    }


    @Override
    public float[] findNearest(float[] point, float[] extents) {
        NavMeshQuery query = query();
        QueryFilter filter = filter();
        Result<FindNearestPolyResult> queryResult = query.findNearestPoly(point, extents, filter);
        if (queryResult.failed() || queryResult.result == null) {
            return null;
        }
        FindNearestPolyResult result = queryResult.result;
        return result.getNearestPos();
    }

    @Override
    public Vector3f findNearest(Vector3f point, Vector3f extents) {
        float[] p = findNearest(v3fToFArr(point), v3fToFArr(extents));
        return FArrTov3f(p);

     }

    private static float[] v3fToFArr(Vector3f vector3f) {
        float[] arr = new float[3];
        arr[0] = vector3f.getX();
        arr[1] = vector3f.getY();
        arr[2] = vector3f.getZ();
        return arr;
    }

    private static Vector3f FArrTov3f(float[] point) {
        return new Vector3f(point[0], point[1], point[2]);
    }


}
