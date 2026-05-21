package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * 最近墙体或边界查询结果。
 *
 * <p>Detour 的 findDistanceToWall 会同时返回距离、命中点和法线。该对象把这些信息
 * 转换成 Easy3dNav 自有类型，避免调用方依赖 recast4j 结果对象。</p>
 */
public class NavWallHit {

    private final Float distance;
    private final Vector3f position;
    private final Vector3f normal;

    /**
     * 创建最近墙体查询结果。
     *
     * @param distance 点到最近墙体或边界的距离
     * @param position 最近墙点坐标
     * @param normal   最近墙体法线
     */
    public NavWallHit(Float distance, Vector3f position, Vector3f normal) {
        this.distance = distance;
        this.position = position;
        this.normal = normal;
    }

    /**
     * 返回点到最近墙体或边界的距离。
     */
    public Float getDistance() {
        return distance;
    }

    /**
     * 返回最近墙点坐标。
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * 返回最近墙体法线。
     */
    public Vector3f getNormal() {
        return normal;
    }
}
