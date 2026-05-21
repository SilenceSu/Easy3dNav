package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * NavMesh 墙体或边界线段。
 *
 * <p>该对象只包含线段起点和终点，不暴露 Detour 的 polygon ref、portal ref 或原始 float 数组，
 * 主要用于调试绘制和地图可视化。</p>
 */
public class NavWallSegment {

    private final Vector3f start;
    private final Vector3f end;

    /**
     * 创建墙体或边界线段。
     *
     * @param start 线段起点
     * @param end   线段终点
     */
    public NavWallSegment(Vector3f start, Vector3f end) {
        this.start = copy(start);
        this.end = copy(end);
    }

    /**
     * 返回线段起点。
     */
    public Vector3f getStart() {
        return copy(start);
    }

    /**
     * 返回线段终点。
     */
    public Vector3f getEnd() {
        return copy(end);
    }

    private static Vector3f copy(Vector3f source) {
        if (source == null) {
            return null;
        }
        return new Vector3f(source.getX(), source.getY(), source.getZ());
    }
}
