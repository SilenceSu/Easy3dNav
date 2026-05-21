package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * 随机可走点 API。
 *
 * <p>该接口面向巡逻点、出生点、逃跑点等业务场景，只使用 Vector3f 表达坐标，
 * 不暴露 Detour 的 polygon ref 或随机查询结果类型。</p>
 */
public interface NavRandomApi {

    /**
     * 在整张 NavMesh 上随机生成一个可走点。
     *
     * @return 随机可走点；查询失败时返回 null
     */
    Vector3f point();

    /**
     * 在中心点附近随机生成一个可走点。
     *
     * @param center 中心点
     * @param radius 搜索半径
     * @return 随机可走点；中心点无效、半径无效或查询失败时返回 null
     */
    Vector3f pointAround(Vector3f center, float radius);

    /**
     * 在指定圆形范围内随机生成一个可走点。
     *
     * @param center 中心点
     * @param radius 搜索半径
     * @return 随机可走点；中心点无效、半径无效或查询失败时返回 null
     */
    Vector3f pointWithin(Vector3f center, float radius);
}
