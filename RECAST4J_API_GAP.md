# recast4j Detour 接口差距对比

本文档只对比 `org.recast4j:detour:1.5.7` 原生能力与 Easy3dNav 当前封装能力的差别，除覆盖状态和落后点外不包含其他内容。

对比口径：

- `已覆盖`：Easy3dNav 已提供业务可直接使用的公开 API。
- `部分覆盖`：Easy3dNav 已使用底层能力，但只暴露了简化结果或固定参数。
- `内部使用`：Easy3dNav 内部依赖该能力，但调用方无法直接使用。
- `未覆盖`：当前没有公开 API，也没有完整封装。

## 1. 总体差距

| 能力类别 | Easy3dNav 当前覆盖 | 相对 recast4j 的主要差距 |
| --- | --- | --- |
| 基础寻路 | 部分覆盖 | 已封装 `findPath` + `findStraightPath`，并通过 `NavOptions` 支持常用路径参数；仍未暴露路径状态、polygon path 和自定义 heuristic。 |
| 射线检测 | 部分覆盖 | 已封装基础 `raycast`，但未暴露 `RaycastHit` 的 path、cost、法线和 `DT_RAYCAST_USE_COSTS`。 |
| 基础位置查询 | 大部分覆盖 | 已封装最近点、可走性、高度；未封装 `closestPointOnPolyBoundary` 和 polygon ref 有效性查询。 |
| 随机点 | 大部分覆盖 | 已封装整图、附近、范围内随机点；未暴露自定义 `PolygonByCircleConstraint` 和随机源配置。 |
| 范围查询 | 部分覆盖 | 已封装 `hasWalkableAround` 布尔判断和 `findLocalNeighbourhood` 局部 polygon 几何结果；未暴露 polygon refs、parent refs、costs、自定义形状和 AABB 查询结果。 |
| 墙体与边界 | 部分覆盖 | 已封装最近墙体距离、命中信息和 wall segment 列表；不暴露 portal refs 或 polygon refs。 |
| 移动约束 | 已覆盖 | 已封装 `moveAlongSurface` 返回实际可移动位置；未暴露 visited polygon refs 或 `MoveAlongSurfaceResult`。 |
| 分片寻路 | 未覆盖 | sliced path 全部未封装。 |
| 过滤器与区域代价 | 部分覆盖 | 内部使用默认 `QueryFilter`，但未对外暴露 include flags、exclude flags、area cost、自定义 cost。 |
| Tile/NavMesh 管理 | 部分覆盖 | 已提供 NavMesh metadata 摘要；tile 增删改、tile 查询、poly flags/area、off-mesh endpoints 均未封装。 |
| IO | 部分覆盖 | 支持 `init(String)`、`init(File)` 和 U3D/标准格式开关；未公开 InputStream、ByteBuffer、写回或导出能力。 |

## 2. NavMeshQuery 查询能力

| recast4j 接口或常量 | Easy3dNav 当前接口 | 覆盖状态 | 落后点 |
| --- | --- | --- | --- |
| `findNearestPoly` | `findNearest`、`isWalkable`、内部前置查询 | 部分覆盖 | 不暴露 polygon ref、nearest ref 状态或 Detour result。 |
| `closestPointOnPoly` | `nav.query().closestPoint(...)` | 已覆盖 | 只能按坐标查询，不能指定 polygon ref。 |
| `closestPointOnPolyBoundary` | 无 | 未覆盖 | 无边界内投影结果。 |
| `getPolyHeight` | `nav.query().getHeight(...)` | 已覆盖 | 只能按坐标查询，不能指定 polygon ref。 |
| `isValidPolyRef` | 无 | 未覆盖 | 公开 API 不暴露 polygon ref，因此也没有 ref 校验入口。 |
| `findPath` | `find(...)`、`nav.path().find(...)`、`nav.path().isReachable(...)` | 部分覆盖 | 不返回 polygon path，不暴露 partial path 状态。 |
| `findPath(..., options, raycastLimit)` | `NavOptions.findPathOptions`、`NavOptions.raycastLimit` | 部分覆盖 | 支持 `DT_FINDPATH_ANY_ANGLE` 和 raycast limit，不支持自定义 heuristic。 |
| `findStraightPath` | `find(...)`、`nav.path().find(..., NavOptions)` | 部分覆盖 | 支持 `maxStraightPath` 和 crossing options，不暴露 `StraightPathItem` flags/ref。 |
| `raycast` | `raycast(...)` | 部分覆盖 | 只返回终点或阻挡点，不暴露 `RaycastHit`、命中法线、path、cost。 |
| `DT_RAYCAST_USE_COSTS` | 无 | 未覆盖 | 射线检测不计算或返回 cost。 |
| `moveAlongSurface` | `nav.path().moveAlongSurface(...)` | 已覆盖 | 返回 `Vector3f`，不暴露 visited polygon refs、`MoveAlongSurfaceResult` 或自动高度修正。 |
| `findRandomPoint` | `nav.random().point()` | 已覆盖 | 不暴露 random ref。 |
| `findRandomPointAroundCircle` | `nav.random().pointAround(...)` | 已覆盖 | 不暴露 random ref 或随机源配置。 |
| `findRandomPointWithinCircle` | `nav.random().pointWithin(...)` | 已覆盖 | 不暴露 random ref 或随机源配置。 |
| `findRandomPointAroundCircle(..., PolygonByCircleConstraint)` | 无 | 未覆盖 | 不支持自定义圆形约束。 |
| `findPolysAroundCircle` | `nav.query().hasWalkableAround(...)` | 部分覆盖 | 只返回布尔值，不返回 refs、parent refs、costs。 |
| `findPolysAroundShape` | 无 | 未覆盖 | 无自定义形状范围查询。 |
| `findLocalNeighbourhood` | `nav.query().findLocalNeighbourhood(...)` | 部分覆盖 | 返回自有 `NavLocalPolygon` 几何信息，不暴露 refs、parent refs 或 Detour 原始 result。 |
| `queryPolygons` | 无 | 未覆盖 | 无 AABB polygon 查询。 |
| `queryTiles` | 无 | 未覆盖 | 无 AABB tile 查询。 |
| `getPolyWallSegments` | `nav.debug().getWallSegments(...)` | 部分覆盖 | 返回自有 `NavWallSegment`，不暴露 portal refs、polygon refs 或 Detour 原始 result。 |
| `findDistanceToWall` | `distanceToWall(...)`、`nearestWall(...)` | 已覆盖 | 不暴露 Detour 原始 result。 |
| `getPathFromDijkstraSearch` | 无 | 未覆盖 | 无 Dijkstra 搜索路径读取。 |
| `isInClosedList` | 无 | 未覆盖 | 无 closed-list 调试状态。 |
| `getNodePool` | 无 | 未覆盖 | 无 Detour 节点池调试入口。 |
| `getAttachedNavMesh` | 无 | 未覆盖 | 不暴露底层 `NavMesh`。 |

## 3. 分片寻路能力

| recast4j 接口 | Easy3dNav 当前接口 | 覆盖状态 | 落后点 |
| --- | --- | --- | --- |
| `initSlicedFindPath` | 无 | 未覆盖 | 无分片寻路初始化。 |
| `updateSlicedFindPath` | 无 | 未覆盖 | 无按步推进寻路。 |
| `finalizeSlicedFindPath` | 无 | 未覆盖 | 无分片寻路完成入口。 |
| `finalizeSlicedFindPathPartial` | 无 | 未覆盖 | 无部分路径完成入口。 |

## 4. QueryFilter 与区域代价

| recast4j 能力 | Easy3dNav 当前接口 | 覆盖状态 | 落后点 |
| --- | --- | --- | --- |
| `DefaultQueryFilter` | 内部默认使用 | 部分覆盖 | 调用方不能配置默认过滤器。 |
| `includeFlags` | 无 | 未覆盖 | 不能按 polygon flags 控制可通行区域。 |
| `excludeFlags` | 无 | 未覆盖 | 不能按 polygon flags 排除区域。 |
| `areaCost` | 无 | 未覆盖 | 不能配置不同 area 的通行代价。 |
| 自定义 `QueryFilter.passFilter` | 无 | 未覆盖 | 不能自定义过滤逻辑。 |
| 自定义 `QueryFilter.getCost` | 无 | 未覆盖 | 不能自定义路径代价计算。 |

## 5. NavMesh 与 Tile 管理

| recast4j 接口 | Easy3dNav 当前接口 | 覆盖状态 | 落后点 |
| --- | --- | --- | --- |
| `getMaxTiles`、`getTileCount` | `nav.debug().meshInfo()` | 部分覆盖 | 提供 loaded/max tile 数量摘要，不返回 tile 对象或 tile ref。 |
| `getTile`、`getTileAt`、`getTilesAt` | 无 | 未覆盖 | 无 tile 查询。 |
| `getTileRef`、`getTileByRef`、`getTileRefAt` | 无 | 未覆盖 | 无 tile ref 查询。 |
| `calcTileLoc` | 无 | 未覆盖 | 无世界坐标到 tile 坐标转换。 |
| `addTile` | 无 | 未覆盖 | 无运行时添加 tile。 |
| `removeTile` | 无 | 未覆盖 | 无运行时移除 tile。 |
| `updateTile` | 无 | 未覆盖 | 无运行时更新 tile。 |
| `setPolyFlags`、`getPolyFlags` | 无 | 未覆盖 | 无 polygon flags 读写。 |
| `setPolyArea`、`getPolyArea` | 无 | 未覆盖 | 无 polygon area 读写。 |
| `getOffMeshConnectionPolyEndPoints` | 无 | 未覆盖 | 无 off-mesh connection 端点查询。 |
| `getParams`、bounds、poly/vert/detail 数量 | `nav.debug().meshInfo()` | 部分覆盖 | 已提供结构化 metadata 摘要；不暴露完整 `NavMeshParams`、底层 tile 数据或打印 API。 |

## 6. IO 能力

| recast4j / 当前相关能力 | Easy3dNav 当前接口 | 覆盖状态 | 落后点 |
| --- | --- | --- | --- |
| `MeshSetReader.read32Bit(InputStream, ...)` | `init(String)`、`init(File)` 内部使用 | 部分覆盖 | 不公开 `InputStream` 加载。 |
| `MeshSetReader.read32Bit(ByteBuffer, ...)` | 无 | 未覆盖 | 不公开 `ByteBuffer` 加载。 |
| recast4j 标准格式读取 | `setUseU3dData(false)` 后通过 `init(String)`、`init(File)` 加载 | 部分覆盖 | 格式选择通过布尔值表达，不是显式格式 API；暂不做自动识别。 |
| U3D/CritterAI 格式读取 | 默认 `setUseU3dData(true)` 后通过 `init(String)`、`init(File)` 加载 | 已覆盖 | 只作为当前默认格式暴露。 |
| NavMesh 写回/导出 | 无 | 未覆盖 | 无 writer 或导出入口。 |
