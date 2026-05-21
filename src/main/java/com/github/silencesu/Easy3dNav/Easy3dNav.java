package com.github.silencesu.Easy3dNav;

import com.github.silencesu.Easy3dNav.api.NavDebugApi;
import com.github.silencesu.Easy3dNav.api.NavPathApi;
import com.github.silencesu.Easy3dNav.api.NavQueryApi;
import com.github.silencesu.Easy3dNav.api.NavRandomApi;
import com.github.silencesu.Easy3dNav.internal.DefaultNavDebugApi;
import com.github.silencesu.Easy3dNav.internal.DefaultNavPathApi;
import com.github.silencesu.Easy3dNav.internal.DefaultNavQueryApi;
import com.github.silencesu.Easy3dNav.internal.DefaultNavRandomApi;
import com.github.silencesu.Easy3dNav.internal.DetourQueries;
import com.github.silencesu.Easy3dNav.internal.NavMeshContext;
import com.github.silencesu.Easy3dNav.io.NavMeshLoader;
import org.recast4j.detour.NavMesh;

import java.io.File;
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
    private final NavMeshLoader navMeshLoader = new NavMeshLoader();
    private final NavMeshContext context = new NavMeshContext();
    private final DetourQueries detourQueries = new DetourQueries();

    private float[] extents = {2.f, 2.f, 2.f};
    private final NavQueryApi queryApi = new DefaultNavQueryApi(context, detourQueries, () -> extents);
    private final NavPathApi pathApi = new DefaultNavPathApi(context, detourQueries, () -> extents);
    private final NavRandomApi randomApi = new DefaultNavRandomApi(context, detourQueries, () -> extents);
    private final NavDebugApi debugApi = new DefaultNavDebugApi(context, detourQueries, () -> extents);


    public Easy3dNav() {

    }

    /**
     * 修改配置 构造
     * @param useU3dData  是否使用critterAi数据
     * @param printMeshInfo  兼容旧构造参数，当前不再触发地图信息打印
     */
    public Easy3dNav(boolean useU3dData, boolean printMeshInfo) {
        this.useU3dData = useU3dData;
    }

    //navmesh 文件路径
    public Easy3dNav(String filePath) throws IOException {
        init(filePath);
    }

    /**
     * 通过文件路径初始化 NavMesh。
     *
     * @param filePath NavMesh 文件路径
     */
    public void init(String filePath) throws IOException {
        init(new File(filePath));
    }

    /**
     * 通过文件对象初始化 NavMesh。
     *
     * <p>该入口只负责文件级加载；流式加载、内存加载和格式自动识别暂不放入 2.0，避免加载语义过早复杂化。</p>
     *
     * @param file NavMesh 文件
     */
    public void init(File file) throws IOException {
        clearThreadLocalQuery();

        NavMesh mesh = navMeshLoader.load(file, useU3dData);
        context.reset(mesh);

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
     * 返回随机可走点分组 API。
     *
     * <p>该分组用于巡逻点、出生点和逃跑点等生成类能力。</p>
     */
    public NavRandomApi random() {
        return randomApi;
    }

    /**
     * 返回调试和可视化分组 API。
     *
     * <p>该分组用于墙体线段、NavMesh 元数据和显式调试打印等偏诊断能力。</p>
     */
    public NavDebugApi debug() {
        return debugApi;
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

    /**
     * 兼容旧打印开关。
     *
     * <p>当前不再提供标准输出打印能力，该方法保留为空实现以避免旧代码编译失败。需要稳定地图信息时，
     * 请使用 `nav.debug().meshInfo()` 读取结构化数据。</p>
     */
    public void setPrintMeshInfo(boolean printMeshInfo) {
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
