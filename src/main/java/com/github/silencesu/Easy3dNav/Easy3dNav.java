package com.github.silencesu.Easy3dNav;

import com.github.silencesu.Easy3dNav.api.NavPathApi;
import com.github.silencesu.Easy3dNav.api.NavQueryApi;
import com.github.silencesu.Easy3dNav.internal.DefaultNavPathApi;
import com.github.silencesu.Easy3dNav.internal.DefaultNavQueryApi;
import com.github.silencesu.Easy3dNav.internal.DetourQueries;
import com.github.silencesu.Easy3dNav.internal.NavMeshContext;
import com.github.silencesu.Easy3dNav.io.NavMeshLoader;
import org.recast4j.detour.MeshTile;
import org.recast4j.detour.NavMesh;

import java.io.IOException;
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

    /**
     * 是否使用U3d插件CritterAI导出的格式
     */
    private boolean useU3dData = true;
    /**
     * 是否打印地图信息
     */
    private boolean printMeshInfo = false;

    private final NavMeshLoader navMeshLoader = new NavMeshLoader();
    private final NavMeshContext context = new NavMeshContext();
    private final DetourQueries detourQueries = new DetourQueries();

    private float[] extents = {2.f, 2.f, 2.f};
    private final NavQueryApi queryApi = new DefaultNavQueryApi(context, detourQueries, () -> extents);
    private final NavPathApi pathApi = new DefaultNavPathApi(context, detourQueries, () -> extents);


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

        NavMesh mesh = navMeshLoader.load(filePath, useU3dData);
        context.reset(mesh);

        if (printMeshInfo) {
            printMeshInfo(mesh);
        }

    }

    /**
     * 清理当前线程缓存的查询上下文。使用线程池或重载地图时可主动调用。
     */
    public void clearThreadLocalQuery() {
        context.clearThreadLocalQuery();
    }

    /**
     * 返回基础位置查询分组 API。
     *
     * <p>新增查询能力优先放在该分组下，避免 Easy3dNav 门面类持续堆积大量方法。</p>
     */
    public NavQueryApi query() {
        return queryApi;
    }

    /**
     * 返回路径能力分组 API。
     *
     * <p>当前先承载可达性判断，后续路径控制类能力也优先放在该分组下。</p>
     */
    public NavPathApi path() {
        return pathApi;
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
        return detourQueries.find(context.query(), context.filter(), start, end, extents);
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
        return detourQueries.raycast(context.query(), context.filter(), start, end, extents);
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
        return findNearest(point, extents);
    }

    @Override
    public Vector3f findNearest(Vector3f point) {
        float[] p = findNearest(v3fToFArr(point));
        return FArrTov3f(p);
    }


    @Override
    public float[] findNearest(float[] point, float[] extents) {
        return detourQueries.findNearest(context.query(), context.filter(), point, extents);
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
