package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 局部邻域中的 NavMesh polygon 几何信息。
 *
 * <p>该对象用于表达 `nav.query().findLocalNeighbourhood` 的公开结果，只包含中心点、
 * polygon 顶点和 bounds，不暴露 Detour 的 polygon ref、parent ref、MeshTile 或 Poly。</p>
 */
public class NavLocalPolygon {

    private final Vector3f center;
    private final List<Vector3f> vertices;
    private final Vector3f boundsMin;
    private final Vector3f boundsMax;

    /**
     * 创建局部邻域 polygon 结果。
     *
     * @param center    polygon 顶点平均得到的中心点
     * @param vertices  polygon 顶点列表
     * @param boundsMin polygon bounds 最小点
     * @param boundsMax polygon bounds 最大点
     */
    public NavLocalPolygon(Vector3f center, List<Vector3f> vertices, Vector3f boundsMin, Vector3f boundsMax) {
        this.center = copy(center);
        this.vertices = copyList(vertices);
        this.boundsMin = copy(boundsMin);
        this.boundsMax = copy(boundsMax);
    }

    /**
     * 返回 polygon 中心点。
     */
    public Vector3f getCenter() {
        return copy(center);
    }

    /**
     * 返回 polygon 顶点列表。
     */
    public List<Vector3f> getVertices() {
        return copyList(vertices);
    }

    /**
     * 返回 polygon bounds 最小点。
     */
    public Vector3f getBoundsMin() {
        return copy(boundsMin);
    }

    /**
     * 返回 polygon bounds 最大点。
     */
    public Vector3f getBoundsMax() {
        return copy(boundsMax);
    }

    private static List<Vector3f> copyList(List<Vector3f> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        List<Vector3f> copy = new ArrayList<>();
        for (Vector3f vertex : source) {
            copy.add(copy(vertex));
        }
        return Collections.unmodifiableList(copy);
    }

    private static Vector3f copy(Vector3f source) {
        if (source == null) {
            return null;
        }
        return new Vector3f(source.getX(), source.getY(), source.getZ());
    }
}
