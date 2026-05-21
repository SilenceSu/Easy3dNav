package com.github.silencesu.Easy3dNav.api;

import com.github.silencesu.Easy3dNav.Vector3f;

/**
 * 单次导航查询参数。
 *
 * <p>该对象用于承载 recast4j Detour 常见查询参数，但不直接暴露 recast4j 类型。
 * 没有设置 extents 时，查询会继续使用 Easy3dNav 的默认搜索范围。</p>
 *
 * <p>当前各 API 对参数的支持范围如下：</p>
 *
 * <ul>
 *   <li>`nav.query().isWalkable/closestPoint/getHeight`：只使用 `extents`。</li>
 *   <li>`nav.path().find`：使用 `extents`、`maxStraightPath`、`straightPathOptions`、`findPathOptions`、`raycastLimit`。</li>
 *   <li>`nav.path().isReachable`：使用 `extents`、`findPathOptions`、`raycastLimit`。</li>
 *   <li>`nav.path().moveAlongSurface`：只使用 `extents`。</li>
 * </ul>
 */
public final class NavOptions {

    /**
     * 默认 straight path 行为，只返回拉直后的关键路径点。
     */
    public static final int STRAIGHT_PATH_NONE = 0;

    /**
     * 对应 recast4j 的 DT_STRAIGHTPATH_AREA_CROSSINGS，返回区域变化穿越点。
     */
    public static final int STRAIGHT_PATH_AREA_CROSSINGS = 1;

    /**
     * 对应 recast4j 的 DT_STRAIGHTPATH_ALL_CROSSINGS，返回所有 polygon 穿越点。
     */
    public static final int STRAIGHT_PATH_ALL_CROSSINGS = 2;

    /**
     * 默认 findPath 行为。
     */
    public static final int FIND_PATH_NONE = 0;

    /**
     * 对应 recast4j 的 DT_FINDPATH_ANY_ANGLE，允许 Detour 尝试更直接的任意角路径。
     */
    public static final int FIND_PATH_ANY_ANGLE = 2;

    private final Vector3f extents;
    private final int maxStraightPath;
    private final int straightPathOptions;
    private final int findPathOptions;
    private final float raycastLimit;

    private NavOptions(Builder builder) {
        this.extents = copy(builder.extents);
        this.maxStraightPath = builder.maxStraightPath;
        this.straightPathOptions = builder.straightPathOptions;
        this.findPathOptions = builder.findPathOptions;
        this.raycastLimit = builder.raycastLimit;
    }

    /**
     * 创建默认查询参数。
     */
    public static NavOptions defaults() {
        return builder().build();
    }

    /**
     * 创建可链式配置的构建器。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 是否为本次查询显式设置了搜索范围。
     *
     * <p>支持 API：`nav.query().isWalkable`、`nav.query().closestPoint`、
     * `nav.query().getHeight`、`nav.path().find`、`nav.path().isReachable`、
     * `nav.path().moveAlongSurface`。</p>
     */
    public boolean hasExtents() {
        return extents != null;
    }

    /**
     * 返回本次查询的搜索范围。未显式设置时返回 null。
     *
     * <p>支持 API：`nav.query().isWalkable`、`nav.query().closestPoint`、
     * `nav.query().getHeight`、`nav.path().find`、`nav.path().isReachable`、
     * `nav.path().moveAlongSurface`。</p>
     */
    public Vector3f getExtents() {
        return copy(extents);
    }

    /**
     * 返回 straight path 最大点数。
     *
     * <p>支持 API：`nav.path().find`。其他 API 会忽略该参数。</p>
     */
    public int getMaxStraightPath() {
        return maxStraightPath;
    }

    /**
     * 返回 straight path 选项。
     *
     * <p>支持 API：`nav.path().find`。其他 API 会忽略该参数。</p>
     */
    public int getStraightPathOptions() {
        return straightPathOptions;
    }

    /**
     * 返回 findPath 选项。
     *
     * <p>支持 API：`nav.path().find`、`nav.path().isReachable`。其他 API 会忽略该参数。</p>
     */
    public int getFindPathOptions() {
        return findPathOptions;
    }

    /**
     * 返回任意角路径使用的 raycast limit。
     *
     * <p>支持 API：`nav.path().find`、`nav.path().isReachable`。只有启用
     * `FIND_PATH_ANY_ANGLE` 时该参数才会影响 Detour 寻路。</p>
     */
    public float getRaycastLimit() {
        return raycastLimit;
    }

    private static Vector3f copy(Vector3f source) {
        if (source == null) {
            return null;
        }
        return new Vector3f(source.getX(), source.getY(), source.getZ());
    }

    /**
     * NavOptions 构建器。
     */
    public static final class Builder {

        private Vector3f extents;
        private int maxStraightPath = Integer.MAX_VALUE;
        private int straightPathOptions = STRAIGHT_PATH_NONE;
        private int findPathOptions = FIND_PATH_NONE;
        private float raycastLimit = 0f;

        private Builder() {
        }

        /**
         * 设置本次查询的 nearest polygon 搜索范围。
         *
         * <p>支持 API：位置查询、路径查询和局部移动约束。随机点、墙体查询当前仍使用 Easy3dNav 默认搜索范围。</p>
         */
        public Builder extents(Vector3f extents) {
            this.extents = copy(extents);
            return this;
        }

        /**
         * 设置最多返回的 straight path 点数。
         *
         * <p>仅 `nav.path().find` 生效。</p>
         */
        public Builder maxStraightPath(int maxStraightPath) {
            if (maxStraightPath <= 0) {
                throw new IllegalArgumentException("maxStraightPath must be greater than 0");
            }
            this.maxStraightPath = maxStraightPath;
            return this;
        }

        /**
         * 设置 straight path 选项。
         *
         * <p>仅 `nav.path().find` 生效。可使用 `STRAIGHT_PATH_AREA_CROSSINGS` 或
         * `STRAIGHT_PATH_ALL_CROSSINGS`。</p>
         */
        public Builder straightPathOptions(int straightPathOptions) {
            this.straightPathOptions = straightPathOptions;
            return this;
        }

        /**
         * 设置 findPath 选项。
         *
         * <p>`nav.path().find` 和 `nav.path().isReachable` 生效。当前主要用于
         * `FIND_PATH_ANY_ANGLE`。</p>
         */
        public Builder findPathOptions(int findPathOptions) {
            this.findPathOptions = findPathOptions;
            return this;
        }

        /**
         * 便捷开关任意角路径。
         *
         * <p>`nav.path().find` 和 `nav.path().isReachable` 生效。</p>
         */
        public Builder anyAngle(boolean enabled) {
            if (enabled) {
                this.findPathOptions |= FIND_PATH_ANY_ANGLE;
            } else {
                this.findPathOptions &= ~FIND_PATH_ANY_ANGLE;
            }
            return this;
        }

        /**
         * 设置任意角路径使用的 raycast limit。负数保持 Detour 的自动限制语义。
         *
         * <p>`nav.path().find` 和 `nav.path().isReachable` 生效。只有启用任意角路径时才影响结果。</p>
         */
        public Builder raycastLimit(float raycastLimit) {
            if (Float.isNaN(raycastLimit) || Float.isInfinite(raycastLimit)) {
                throw new IllegalArgumentException("raycastLimit must be finite");
            }
            this.raycastLimit = raycastLimit;
            return this;
        }

        /**
         * 构建不可变查询参数。
         */
        public NavOptions build() {
            return new NavOptions(this);
        }
    }
}
