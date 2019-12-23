package com.github.silencesu.recastNav;

import com.github.silencesu.recastNav.detour.*;
import com.github.silencesu.recastNav.detour.io.MeshSetReader;
import com.github.silencesu.recastNav.detour.io.MeshSetReaderU3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author SilenceSu
 * @Email Silence.Sx@Gmail.com
 * Created by Silence on 2018/5/3.
 */
public class RecastNav {

    private static Logger LOGGER = LoggerFactory.getLogger(RecastNav.class);


    /**
     * 是否使用CritterAI导出的格式
     */
    public static final boolean critterAI = true;

    private NavMeshQuery query;
    private QueryFilter filter;

    private float[] extents = {2.f, 2.f, 2.f};


    //navmesh 文件路径
    public RecastNav(String filePath) throws IOException {
        init(filePath);
    }

    /**
     * 初始化寻路需要参数
     *
     * @param filePath
     */
    private void init(String filePath) throws IOException {
        init(filePath, true);
    }

    private void init(String filePath, boolean critterAI) throws IOException {

        InputStream inputStream = null;
        NavMesh mesh;
        try {
            //获取文件流
            inputStream = new FileInputStream(new File(filePath));

            if (critterAI) {
                MeshSetReaderU3d reader = new MeshSetReaderU3d();
                mesh = reader.read32Bit(inputStream, 6);
            } else {
                MeshSetReader reader = new MeshSetReader();
                mesh = reader.read32Bit(inputStream, 6);
            }

            query = new NavMeshQuery(mesh);
            filter = new QueryFilter();

        } finally {
            //使用完，关闭流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

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
            if (tile == null) {
                continue;
            }
            tileCount++;
            nodeCount +=tile.data.header.bvNodeCount;
            polyCount += tile.data.header.polyCount;
            vertCount += tile.data.header.vertCount;
            triCount += tile.data.header.detailTriCount;
            triVertCount += tile.data.header.detailVertCount;
//			dataSize += tile.data.header.size;


            System.out.printf("%f %f %f\n",tile.data.verts[0]  , tile.data.verts[1], tile.data.verts[2]);
            for (int m = 0; m < tile.data.header.detailVertCount; m++) {
                System.out.printf("%f %f %f\n", tile.data.detailVerts[m * 3], tile.data.detailVerts[m * 3 + 1], tile.data.detailVerts[m * 3 + 2]);
            }

        }

        System.out.printf("\t==> tiles loaded: %d\n" , tileCount);
        System.out.printf("\t==> BVTree nodes: %d\n", nodeCount);
        System.out.printf("\t==> %d polygons (%d vertices)\n", polyCount, vertCount);

        System.out.printf("\t==> %d triangles (%d vertices)\n",triCount, triVertCount);

    }


    /**
     * 获取文件路径
     *
     * @param start 开始坐标点  {x,y,z}
     * @param end   结束坐标点   {x,y,z}
     * @return
     */
    public List<float[]> find(float[] start, float[] end, float[] extents) {


        List<float[]> pathRet = new ArrayList<>();

        //获取开始点，附近的点
        FindNearestPolyResult startResult = query.findNearestPoly(start, extents, filter);
        //获取结束点，附近的点
        FindNearestPolyResult endResult = query.findNearestPoly(end, extents, filter);

        //寻找开始点和结束点附近的多边形
        if (startResult.getNearestRef() == 0 || endResult.getNearestRef() == 0) {
            LOGGER.info("start or end point not found poly");
            return Collections.emptyList();
        }

        //获取路径ids
        FindPathResult path = query.findPath(startResult.getNearestRef(), endResult.getNearestRef(), startResult.getNearestPos(), endResult.getNearestPos(), filter);

        if (path.getStatus() != Status.SUCCSESS) {
            LOGGER.info("nav path  status is  {}", path.getStatus());
            return Collections.emptyList();
        }

        //路径平滑
        List<StraightPathItem> straightPath = query.findStraightPath(startResult.getNearestPos(), endResult.getNearestPos(), path.getRefs(), Integer.MAX_VALUE, 0);
        for (StraightPathItem straightPathItem : straightPath) {
            float[] p = new float[3];
            System.arraycopy(straightPathItem.getPos(), 0, p, 0, straightPathItem.getPos().length);
            pathRet.add(p);
        }
        return pathRet;
    }


    public List<float[]> find(float[] start, float[] end) {
        return find(start, end, extents);
    }


    /**
     * 光线照射发，寻找可以支线通过的hit点，如果可通过则返回hit
     *
     * @param start
     * @param end
     * @return
     */
    public float[] raycast(float[] start, float[] end, float[] extents) {
        FindNearestPolyResult startResult = query.findNearestPoly(start, extents, filter);
        if (startResult.getNearestRef() == 0) {
            throw new IllegalArgumentException("not find nearestPoly");
        }
        float[] hitPoint = null;

        RaycastHit hitReasult = query.raycast(startResult.getNearestRef(), startResult.getNearestPos(), end, filter, 0, 0);
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

    public float[] raycast(float[] start, float[] end) {
        return raycast(start, end, new float[]{0.f, 2.f, 0.f});
    }


    /**
     * 获取指定点附近可行走的点
     *
     * @param point
     * @return
     */
    public float[] findNearest(float[] point) {
        FindNearestPolyResult result = query.findNearestPoly(point, extents, filter);
        return result.getNearestPos();
    }

    /**
     * 获取指定点附近可行走的点
     *
     * @param point   查找怪物位置
     * @param extents 扩展 {x,y,z} 可以寻找的范围
     * @return
     */
    public float[] findNearest(float[] point, float[] extents) {
        FindNearestPolyResult result = query.findNearestPoly(point, extents, filter);
        return result.getNearestPos();
    }

    public void setExtents(float[] extents) {
        this.extents = extents;
    }
}

