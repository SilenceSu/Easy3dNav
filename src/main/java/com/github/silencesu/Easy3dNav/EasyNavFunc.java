package com.github.silencesu.Easy3dNav;

import java.util.List;

/**
 * 常用简单寻路方法
 *
 * @author SilenceSu
 * @Email Silence.Sx@Gmail.com
 * Created by Silence on 2019/12/28.
 */
public interface EasyNavFunc {


    /**
     * 寻找路径
     *
     * @param start   开始坐标点
     * @param end     结束坐标点
     * @param extents 搜索范围
     * @return 路经典集合
     */
    List<float[]> find(float[] start, float[] end, float[] extents);

    List<Vector3f> find(Vector3f start, Vector3f end, Vector3f extents);


    /**
     * 寻找路径
     *
     * @param start 开始坐标点
     * @param end   结束坐标点
     * @return 路经典集合
     */
    List<float[]> find(float[] start, float[] end);

    List<Vector3f> find(Vector3f start, Vector3f end);

    /**
     * 光线照射发，寻找可以支线通过的hit点，如果可通过则返回hit
     *
     * @param start   开始点
     * @param end     目标点
     * @param extents 搜索范围
     * @return 光照可以达到的点
     */
    float[] raycast(float[] start, float[] end, float[] extents);

    Vector3f raycast(Vector3f start, Vector3f end, Vector3f extents);


    /**
     * 光线照射发，寻找可以支线通过的hit点，如果可通过则返回hit
     *
     * @param start 开始点
     * @param end   目标点
     * @return 光照可以达到的点
     */
    float[] raycast(float[] start, float[] end);

    Vector3f raycast(Vector3f start, Vector3f end);


    /**
     * 获取指定点附近可行走的点
     *
     * @param point   当前验证点
     * @param extents 搜索范围
     * @return 可以行走的点(如验证点不能行走 ， 则返回可以行走的点)
     */
    float[] findNearest(float[] point, float[] extents);

    Vector3f findNearest(Vector3f point, Vector3f extents);

    /**
     * 获取指定点附近可行走的点
     *
     * @param point 当前验证点
     * @return 可以行走的点(如验证点不能行走 ， 则返回可以行走的点)
     */
    float[] findNearest(float[] point);

    Vector3f findNearest(Vector3f point);


}
