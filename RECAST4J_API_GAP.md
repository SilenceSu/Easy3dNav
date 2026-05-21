# recast4j Detour 接口差距分析

本文档用于对照 `org.recast4j:detour:1.5.7` 与 Easy3dNav 当前封装能力，辅助后续版本规划。

## 1. 当前 Easy3dNav 已封装能力

当前公开 API 主要集中在 `EasyNavFunc`：

- `find(start, end)`：基础寻路，并返回平滑后的路径点。
- `raycast(start, end)`：射线检测，返回可直达点或阻挡点。
- `findNearest(point)`：查找指定点附近的可行走点。
- `init(filePath)`：加载 NavMesh 文件，默认支持 U3D/CritterAI 导出格式。

这些能力覆盖了最基础的服务端寻路场景，但还没有完整封装 Detour 的查询、过滤、范围搜索、随机点、墙体检测、tile 管理等能力。

## 2. 基础位置查询

| recast4j Detour 接口 | 当前状态 | 作用 | 建议封装 |
| --- | --- | --- | --- |
| `findNearestPoly` | 间接使用 | 查找点附近最近 polygon，是多数查询的前置步骤 | 保持内部使用 |
| `closestPointOnPoly` | 未封装 | 把点投影到指定 polygon 上，可用于坐标修正 | `closestPoint(point)` |
| `closestPointOnPolyBoundary` | 未封装 | 把点限制在 polygon 边界内 | 可选 |
| `getPolyHeight` | 未封装 | 获取 NavMesh 表面高度 | `getHeight(point)` |
| `isValidPolyRef` | 未封装 | 判断 polygon ref 是否仍有效 | 内部使用 |

典型用途：

- 服务端校正客户端坐标。
- 判断角色、怪物、掉落物是否在可走区域。
- 根据 NavMesh 修正 Y 轴高度。
- 将非法点吸附回最近可行走区域。

建议优先级：高，适合放入 `2.1`。

## 3. 可走性与可达性

Easy3dNav 当前可以通过 `findNearest` 或 `find` 间接判断，但缺少语义明确的业务接口。

建议新增：

- `isWalkable(point)`：判断点附近是否存在有效可行走 polygon。
- `isReachable(start, end)`：判断两点是否可达，不返回完整路径。

典型用途：

- 技能落点校验。
- 怪物出生点校验。
- AI 目标点校验。
- 点击移动目标校验。
- 传送点、复活点合法性检查。

建议优先级：高，适合放入 `2.1`。

## 4. 路径控制能力

| recast4j Detour 接口或常量 | 当前状态 | 作用 | 建议封装 |
| --- | --- | --- | --- |
| `findPath` | 已基础使用 | 生成 polygon path | 保持 |
| `findStraightPath` | 已基础使用 | 将 polygon path 转为路径点 | 增强参数 |
| `DT_STRAIGHTPATH_AREA_CROSSINGS` | 未封装 | 返回区域穿越点 | `NavOptions` |
| `DT_STRAIGHTPATH_ALL_CROSSINGS` | 未封装 | 返回所有穿越点 | `NavOptions` |
| `DT_FINDPATH_ANY_ANGLE` | 未封装 | 任意角路径搜索 | `NavOptions` |
| `raycastLimit` | 未封装 | any-angle 查询时限制射线检测距离 | `NavOptions` |

建议新增 `NavOptions`，统一承载：

- `extents`
- `maxStraightPath`
- `straightPathOptions`
- `findPathOptions`
- `raycastLimit`
- 失败处理策略

典型用途：

- 控制路径点数量。
- 返回更详细的路径穿越点。
- 尝试更直接的 any-angle 路径。
- 为不同业务场景使用不同路径精度。

建议优先级：高，`NavOptions` 应作为后续扩展基础。

## 5. 分片寻路

| recast4j Detour 接口 | 当前状态 | 作用 |
| --- | --- | --- |
| `initSlicedFindPath` | 未封装 | 初始化分片寻路 |
| `updateSlicedFindPath` | 未封装 | 分批推进寻路计算 |
| `finalizeSlicedFindPath` | 未封装 | 完成并返回路径 |
| `finalizeSlicedFindPathPartial` | 未封装 | 返回部分路径 |

典型用途：

- 大地图长距离寻路。
- 大量 NPC 同时寻路。
- 将寻路计算拆分到多个 tick，避免单帧耗时过高。

建议优先级：中。可以等基础查询和过滤器稳定后再封装。

## 6. 移动约束

| recast4j Detour 接口 | 当前状态 | 作用 | 建议封装 |
| --- | --- | --- | --- |
| `moveAlongSurface` | 未封装 | 在 NavMesh 表面从当前点向目标点移动，返回实际可移动位置 | `moveAlongSurface(start, end)` 或 `move(start, end)` |

典型用途：

- 服务端高频移动校验。
- 防止角色或怪物穿墙。
- 将客户端位置修正到 NavMesh 表面。
- 不需要完整路径时做局部移动。

建议优先级：中高，适合在 `2.1` 或 `2.2` 早期加入。

## 7. 随机点与范围查询

| recast4j Detour 接口 | 当前状态 | 作用 | 建议封装 |
| --- | --- | --- | --- |
| `findRandomPoint` | 未封装 | 在整个 NavMesh 上找随机点 | `findRandomPoint()` |
| `findRandomPointAroundCircle` | 未封装 | 在某点附近找随机可走点 | `findRandomPointAround(center, radius)` |
| `findRandomPointWithinCircle` | 未封装 | 在指定圆范围内找随机点 | `findRandomPointWithin(center, radius)` |
| `findPolysAroundCircle` | 未封装 | 找圆形范围内相关 polygon | `findPolysAround(center, radius)` |
| `findPolysAroundShape` | 未封装 | 找自定义形状范围内相关 polygon | 后续可选 |
| `findLocalNeighbourhood` | 未封装 | 查找局部邻域 polygon | `findLocalNeighbourhood(center, radius)` |
| `queryPolygons` | 未封装 | 查询 AABB 范围内 polygon | 内部或高级 API |
| `queryTiles` | 未封装 | 查询 AABB 范围内 tile | 内部或调试 API |

典型用途：

- 怪物巡逻点。
- 随机出生点。
- 逃跑目标点。
- 附近可走区域扫描。
- 生成 AI 行为候选点。

建议优先级：中高，适合放入 `2.2`。

## 8. 墙体与边界查询

| recast4j Detour 接口 | 当前状态 | 作用 | 建议封装 |
| --- | --- | --- | --- |
| `findDistanceToWall` | 未封装 | 查询点到最近墙或边界的距离 | `findDistanceToWall(point, radius)` |
| `getPolyWallSegments` | 未封装 | 获取 polygon 墙边界线段 | `getWallSegments(point)` |

典型用途：

- 判断角色或怪物离墙距离。
- AI 避障。
- 防止出生点过于靠墙。
- 调试 NavMesh 边界。
- 做贴边或脱困逻辑。

建议优先级：高，`findDistanceToWall` 适合放入 `2.1`。

## 9. Filter 与区域代价

| recast4j Detour 能力 | 当前状态 | 作用 | 建议封装 |
| --- | --- | --- | --- |
| `DefaultQueryFilter` | 内部默认使用 | 默认 polygon 过滤和 cost 计算 | 保持默认 |
| include flags | 未对外封装 | 指定允许通过的 polygon flags | `NavOptions` / `NavProfile` |
| exclude flags | 未对外封装 | 指定禁止通过的 polygon flags | `NavOptions` / `NavProfile` |
| area cost | 未对外封装 | 不同区域使用不同通行代价 | `NavOptions` / `NavProfile` |
| 自定义 `QueryFilter` | 未对外封装 | 完全自定义过滤和 cost | 高级扩展入口 |

典型用途：

- 不同单位类型走不同区域。
- 水、草地、道路、危险区使用不同寻路代价。
- 动态禁止某类区域通过。
- 为 Boss、大型单位、特殊单位配置不同路径策略。

建议优先级：高。过滤器是 Detour 很核心的扩展点，应和 `NavOptions` 一起设计。

## 10. Tile 与 NavMesh 管理

| recast4j Detour 接口 | 当前状态 | 作用 |
| --- | --- | --- |
| `addTile` | 未封装 | 添加 tile |
| `removeTile` | 未封装 | 移除 tile |
| `updateTile` | 未封装 | 更新 tile |
| `getTileCount` | 未封装 | 获取 tile 数量 |
| `getTileAt` / `getTilesAt` | 未封装 | 根据 tile 坐标查询 tile |
| `getTileRef` / `getTileByRef` | 未封装 | tile 与 ref 互查 |
| `setPolyFlags` / `getPolyFlags` | 未封装 | 修改或读取 polygon flags |
| `setPolyArea` / `getPolyArea` | 未封装 | 修改或读取 polygon area |
| `getOffMeshConnectionPolyEndPoints` | 未封装 | 查询 off-mesh connection 端点 |

典型用途：

- 分块大地图。
- 地图热更新。
- 动态开关区域。
- 修改地形类型或区域 flags。
- off-mesh connection 调试和业务处理。

建议优先级：中低。除非项目明确要支持动态地图，否则不建议过早开放。

## 11. IO 能力

当前 Easy3dNav 主要通过 `init(String filePath)` 加载。

建议补充：

- `init(File file)`
- `init(InputStream inputStream)`
- `init(ByteBuffer buffer)`
- `loadU3d(...)`
- `loadRecast4j(...)`
- NavMesh 写回或导出能力

典型用途：

- 从资源包加载。
- 从数据库或对象存储加载。
- 从内存缓存加载。
- 支持服务端资源热更新。
- 用于测试和调试导出。

建议优先级：中，适合放入 `2.3`。

## 12. 建议实现顺序

推荐优先级如下：

1. `NavOptions`
2. `isWalkable`
3. `isReachable`
4. `getHeight`
5. `findDistanceToWall`
6. `moveAlongSurface`
7. `findRandomPointAround`
8. `QueryFilter` / area cost 封装
9. `InputStream` / `ByteBuffer` 加载
10. tile 管理和动态区域

原因：

- `NavOptions` 是后续扩展参数的基础。
- `isWalkable`、`isReachable`、`getHeight` 是服务端业务最常用的判断类接口。
- `findDistanceToWall` 和 `moveAlongSurface` 能提升移动校验和 AI 稳定性。
- 随机点和范围查询主要服务 AI 行为，适合第二阶段扩展。
- tile 管理涉及动态地图和更多状态变更，应在核心查询能力稳定后再做。

## 13. 版本映射建议

| 版本 | 建议范围 |
| --- | --- |
| `2.1` | `NavOptions`、`isWalkable`、`isReachable`、`getHeight`、`findDistanceToWall` |
| `2.2` | `moveAlongSurface`、随机点、范围查询、过滤器和 area cost |
| `2.3` | 加载入口、真实 U3D fixture、调试信息、基础 tile 查询 |
| `3.x` | Java 11/17、recast4j 新版本、可能的动态 tile 管理增强 |

## 14. 不建议直接暴露的内容

以下能力建议先内部使用或作为高级 API 谨慎暴露：

- polygon ref 细节。
- `NodePool`、`NodeQueue`、`QueryData`。
- Detour 内部 tile/link 结构。
- 过多的 recast4j result 类型。

原因是这些接口容易把 Easy3dNav 变成 recast4j 的薄转发层，削弱项目“简单可靠封装”的定位。
