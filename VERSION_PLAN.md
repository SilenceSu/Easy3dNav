# Easy3dNav 版本计划书

## 1. 项目定位

Easy3dNav 的核心定位是对 recast4j Detour 做服务端友好的轻量封装，而不是维护 recast4j 的 fork。项目重点解决游戏服务端使用 NavMesh 时的接入成本、API 复杂度、U3D/CritterAI 数据兼容、常用查询能力封装和调试可观测性。

当前项目已经从复制版 Detour 源码迁移为依赖官方 `org.recast4j:detour`。由于项目仍保持 Java 8 基线，`2.x` 阶段使用 `org.recast4j:detour:1.5.7`。recast4j `1.5.8+` 已使用 Java 11 字节码，后续如需升级应放入 `3.x` 路线单独评估。

## 2. 当前能力边界

当前已实现的核心能力：

- U3D/CritterAI NavMesh 文件加载。
- 基础寻路：`find(start, end)`。
- 射线检测：`raycast(start, end)`。
- 最近可行走点：`findNearest(point)`。
- `float[]` 与 `Vector3f` 两套简单调用方式。

当前刻意不解决的问题：

- 不内置复杂线程调度。
- 不重写 Detour 算法。
- 不复制 recast4j 核心源码。
- 不直接暴露过多 recast4j 内部类型给业务层。

## 3. 设计原则

### 3.1 简单 API 优先

Easy3dNav 的用户应优先面对简单、稳定、语义明确的 API。例如 `find`、`raycast`、`findNearest`、`isReachable`、`isWalkable`。除非有必要，不要求业务方理解 Detour 的 polygon ref、tile、node pool、straight path flags 等内部概念。

### 3.2 保留 Detour 扩展能力

简单 API 之上需要保留高级扩展入口，尤其是 `QueryFilter`、area cost、include/exclude flags、straight path options、partial path、any-angle pathfinding 等能力。推荐通过 `NavOptions`、`NavProfile` 或类似配置对象封装，而不是把参数继续堆到方法签名里。

### 3.3 线程级查询隔离

线程安全边界保持清晰：Easy3dNav 不做全局同步，也不内置线程池。`NavMesh` 加载后作为只读数据共享；每个调用线程自动持有独立的 `NavMeshQuery`，避免 Detour 查询临时状态在线程之间互相影响。默认 `QueryFilter` 暂不做 ThreadLocal 隔离，后续与 `NavOptions`、area cost、include/exclude flags 一起设计。`init`/重载地图不应与查询并发执行；使用线程池并频繁卸载或重载地图时，可调用 `clearThreadLocalQuery()` 清理当前线程缓存。

### 3.4 兼容优先

U3D/CritterAI 数据兼容是项目核心价值之一。任何加载逻辑、坐标语义、默认 extents、失败返回行为的变化都需要回归测试覆盖。真实 U3D fixture 应逐步补齐。

## 4. 版本路线

## 2.0：依赖上游 recast4j，删除复制源码

目标：

- 用官方 `org.recast4j:detour:1.5.7` 替换本地复制版 Detour。
- 保留现有 `Easy3dNav`、`EasyNavFunc`、`Vector3f` 对外 API。
- 保留 U3D/CritterAI 读取能力。
- 增加最小回归测试，证明基础 API 可用。

已完成内容：

- `Easy3dNav` 改为调用 `org.recast4j.detour`。
- 删除 `com.github.silencesu.Easy3dNav.detour` 复制源码。
- 新增 Easy3dNav 专用 U3D reader 适配层。
- 增加动态 NavMesh fixture 单测。

验收标准：

- `mvn test` 通过。
- `mvn clean package` 通过。
- 老用户继续使用 `init`、`find`、`raycast`、`findNearest`。

## 2.1：基础查询能力补齐

目标是把 Detour 中最常用、最适合服务端业务直接使用的查询能力封装出来。

计划 API：

- `isWalkable(point)`：判断点附近是否存在可行走 polygon。
- `isReachable(start, end)`：判断两点是否可达，不返回完整路径。
- `getHeight(point)`：获取 NavMesh 上的高度。
- `closestPoint(point)` 或 `closestPointOnPoly(point)`：把点吸附到 NavMesh。
- `findDistanceToWall(point, radius)`：查询点到最近墙或边界的距离。

配套改造：

- 引入 `NavOptions`，承载 `extents`、`maxStraightPath`、query flags 等参数。
- 明确失败语义：返回空集合、`null`、`Optional` 或结果对象需要统一。
- 增加真实或动态 fixture 测试。

建议验收标准：

- 主要 API 都支持 `float[]` 和 `Vector3f`。
- 每个新增 API 至少有成功与失败场景测试。
- README 增加基础查询示例。

## 2.2：过滤器、地形代价和范围查询

目标是支持更贴近游戏业务的寻路差异化策略。

计划能力：

- `NavProfile` 或 `NavOptions` 支持 include flags、exclude flags、area cost。
- 支持不同单位类型：普通单位、大体型单位、特殊区域单位等。
- 封装 `findRandomPoint`。
- 封装 `findRandomPointAround(center, radius)`。
- 封装 `findPolysAroundCircle(center, radius)`。
- 封装 `findLocalNeighbourhood(center, radius)`。

典型用途：

- 怪物巡逻点。
- 出生点随机化。
- 逃跑目标点选择。
- 附近可走区域扫描。
- 不同地形代价控制，例如道路、草地、水域、危险区。

建议验收标准：

- `NavOptions` 不破坏旧 API。
- 默认参数行为与当前版本一致。
- 不同 area cost 或过滤 flags 有可验证测试。

## 2.3：加载入口、调试能力和真实数据验证

目标是提升工程可用性，而不是增加复杂算法。

计划能力：

- 增加 `init(File)`。
- 增加 `init(InputStream)`。
- 增加 `init(ByteBuffer)`。
- 增加 U3D 与非 U3D 显式加载方法，例如 `loadU3d`、`loadRecast4j`。
- 输出 NavMesh metadata，例如 tile 数量、polygon 数量、顶点数量、bounds。
- 增加真实 U3D/CritterAI `.navmesh` fixture。
- 整理常见问题文档：坐标系、extents、导出插件、加载失败、路径为空。

建议验收标准：

- 加载入口覆盖文件、流和内存数据。
- 测试不依赖本机绝对路径。
- README 中有完整服务端加载示例。

## 3.x：Java 基线升级与新版 recast4j 跟进

`3.x` 是兼容性变化较大的路线，目标是评估升级 Java 11 或 Java 17，并跟进 recast4j `1.5.8+` 或更新版本。

升级前需要确认：

- 当前用户是否仍依赖 Java 8。
- 新版 recast4j API 是否有行为差异。
- U3D/CritterAI reader 是否仍兼容。
- Maven 发布、CI、Javadoc 是否支持新 Java 基线。

可能收益：

- 获取 recast4j 后续修复。
- 使用更现代的 Java 工具链。
- 简化依赖维护。

风险：

- 破坏 Java 8 用户。
- 上游 API 或数据格式变化导致兼容性问题。
- 需要重新验证真实 U3D 数据。

## 5. 线程安全规划

已实现策略：

- `Easy3dNav` 持有加载后的只读 `NavMesh`。
- 每个线程通过 `ThreadLocal` 持有自己的 `NavMeshQuery`。
- 默认 `QueryFilter` 暂不做 ThreadLocal 化，后续过滤器组合能力通过单独版本规划处理。
- `init` 重新加载 NavMesh 时会重建线程级查询上下文。
- `init`/重载地图不应与正在执行的查询并发运行。
- 提供 `clearThreadLocalQuery()`，用于线程池、地图卸载或重载后的当前线程缓存清理。

后续策略：

- 提供 `newQuery()` 或 `createQuery()`。
- 每个 `NavQuery` 内部持有独立 `NavMeshQuery`。
- `NavOptions` 和 `NavProfile` 应设计为不可变对象，避免把可变 filter 配置作为共享状态。

示例方向：

```java
Easy3dNav nav = Easy3dNav.load(file);
NavQuery query = nav.newQuery();

query.find(start, end);
query.raycast(start, end);
```

## 6. 测试规划

测试分三类：

- 动态 fixture：用于验证基础 API，不依赖外部文件。
- 真实 U3D fixture：用于验证 Unity/CritterAI 导出兼容。
- 行为回归测试：用于锁定失败语义、路径为空、不可达、边界点、墙距离等行为。

每次新增 Detour 封装能力都应至少覆盖：

- 正常成功路径。
- 参数无效或找不到 polygon。
- 边界输入，例如点在 polygon 边缘、起终点相同、目标不可达。

## 7. 发布检查清单

每次发布前至少确认：

- `mvn test` 通过。
- `mvn clean package` 通过。
- README 示例可编译或语义正确。
- 没有提交本地 `.navmesh` 大文件、绝对路径、凭据。
- 版本号、变更说明、兼容性说明完整。
- 如果涉及 U3D reader，必须说明测试覆盖的数据格式。

## 8. 非目标

以下内容不是近期目标：

- 不实现完整 recast/recast-demo 构建链。
- 不把 Easy3dNav 变成可视化编辑器。
- 不维护 recast4j 算法分叉。
- 不在 `2.x` 阶段强制升级 Java 11/17。

## 9. 推荐下一步

建议下一步优先做 `2.1`：

1. 引入 `NavOptions`。
2. 增加 `isWalkable`。
3. 增加 `isReachable`。
4. 增加 `findDistanceToWall`。
5. 为这些 API 补齐动态 fixture 测试。

这样可以先把 API 风格、失败语义、参数承载方式确定下来，为 `2.2` 的过滤器和随机点能力打基础。

更详细的原版 recast4j Detour 接口对比见 [recast4j Detour 接口差距分析](RECAST4J_API_GAP.md)。
