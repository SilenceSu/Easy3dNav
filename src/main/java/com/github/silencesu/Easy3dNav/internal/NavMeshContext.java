package com.github.silencesu.Easy3dNav.internal;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;

/**
 * 管理一次 NavMesh 加载后的查询上下文。
 *
 * <p>该类只服务 internal 包：Easy3dNav 对外保持简单 API，这里集中处理
 * NavMesh、每线程独立 NavMeshQuery 和默认 QueryFilter 的生命周期。</p>
 */
public class NavMeshContext {

    /**
     * 加载后的 NavMesh 作为只读数据共享。
     */
    private NavMesh navMesh;

    /**
     * NavMeshQuery 内部有查询临时状态，每个线程必须使用自己的实例。
     */
    private ThreadLocal<NavMeshQuery> queryLocal;

    /**
     * 默认 QueryFilter 暂不做 ThreadLocal 化，后续过滤器组合由 NavOptions/NavProfile 统一设计。
     */
    private QueryFilter filter;

    /**
     * 绑定新的 NavMesh，并重建线程级查询入口。
     *
     * <p>调用方应保证 reset 不与正在执行的查询并发运行。</p>
     */
    public void reset(NavMesh mesh) {
        clearThreadLocalQuery();
        navMesh = mesh;
        queryLocal = ThreadLocal.withInitial(() -> new NavMeshQuery(mesh));
        filter = createQueryFilter();
    }

    /**
     * 清理当前线程缓存的 NavMeshQuery。
     *
     * <p>线程池或地图重载场景下，业务线程可通过 Easy3dNav 暴露的方法触发清理。</p>
     */
    public void clearThreadLocalQuery() {
        if (queryLocal != null) {
            queryLocal.remove();
        }
    }

    /**
     * 返回当前加载的 NavMesh。
     */
    public NavMesh navMesh() {
        if (navMesh == null) {
            throw new IllegalStateException("Easy3dNav is not initialized");
        }
        return navMesh;
    }

    /**
     * 返回当前线程的 NavMeshQuery，首次调用时由 ThreadLocal 自动创建。
     */
    public NavMeshQuery query() {
        if (queryLocal == null) {
            throw new IllegalStateException("Easy3dNav is not initialized");
        }
        return queryLocal.get();
    }

    /**
     * 返回默认 QueryFilter。
     */
    public QueryFilter filter() {
        if (filter == null) {
            throw new IllegalStateException("Easy3dNav is not initialized");
        }
        return filter;
    }

    private QueryFilter createQueryFilter() {
        return new DefaultQueryFilter();
    }
}
