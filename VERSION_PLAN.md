# Easy3dNav 2.0 版本计划书

## 1. 版本定位

`2.0` 是 Easy3dNav 的一次大革新版本，不再拆成 `2.1`、`2.2`、`2.3` 这种细路线。当前目标是围绕 recast4j Detour 做一层简单、可靠、服务端友好的封装，让业务方优先使用 Easy3dNav API，而不是直接理解 polygon ref、tile、node pool、Detour result 等内部概念。

`2.0` 阶段继续保持 Java 8 基线，底层使用 `org.recast4j:detour:1.5.7`。升级 Java 11/17 或 recast4j 更高版本不放入当前版本目标。

## 2. 已完成内容

- 使用官方 `org.recast4j:detour` 依赖替换本地复制版 Detour 源码。
- 删除 `com.github.silencesu.Easy3dNav.detour` 复制源码。
- 保留 U3D/CritterAI NavMesh 读取适配层。
- 增加动态 NavMesh fixture 单测，覆盖 `find`、`raycast`、`findNearest`。
- `NavMeshQuery` 改为线程级隔离，`NavMesh` 作为只读数据共享。
- 删除废弃兼容类 `RecastNav`，统一公开入口为 `Easy3dNav`。
- 拆出 `internal/DetourQueries`、`internal/NavMeshContext` 和 `io/NavMeshLoader`，让 `Easy3dNav` 保持门面职责。
- 新增 `nav.query()` 分组入口，提供 `isWalkable`、`closestPoint`、`getHeight` 基础位置查询。
- 新增 `nav.path()` 分组入口，提供 `isReachable` 可达性判断。
- 新增 `nav.random()` 分组入口，提供整图、附近和范围内随机可走点。
- 新增 `hasWalkableAround`、`distanceToWall`、`nearestWall`，覆盖范围可走性和基础墙体查询。
- 补充 `init(File)` 文件加载入口，保留 `init(String)` 兼容用法。

## 3. 2.0 待实现清单

按“先补公共参数和高频查询，再补调试与文档收尾”的顺序推进。

### 3.1 2.0 可继续处理

- [ ] `NavOptions`：统一承载 `extents`、`maxStraightPath`、straight path options、find path options、raycast limit 等查询参数。
- [ ] 路径能力：提供支持 `NavOptions` 的路径查询入口，例如 `nav.path().find(start, end, options)`。
- [ ] 移动约束：封装 `moveAlongSurface(start, end)`，用于服务端移动校验、贴地修正和防穿墙。
- [ ] 过滤器与区域代价：设计 `NavProfile` 或等价方案，支持 include flags、exclude flags、area cost 和自定义过滤器组合。
- [ ] 范围查询：在已完成随机点基础上，补充候选点列表、`findLocalNeighbourhood(center, radius)` 等业务可直接消费的范围能力。
- [ ] 加载格式：明确当前 U3D/CritterAI 与 recast4j 标准格式的配置方式，暂不扩展更多加载入口。
- [ ] 分组 API：补充 `nav.debug()`，承载调试、可视化和 metadata 能力，避免 `Easy3dNav` 堆积过多方法。
- [ ] 墙体与边界：将 `getWallSegments(point)` 等边界线段能力放入 `nav.debug()`，只作为调试/可视化接口。
- [ ] 调试信息：提供 NavMesh metadata，例如 tile 数量、polygon 数量、顶点数量、bounds。
- [ ] 返回语义：统一空路径、不可达、找不到 polygon、非法参数等失败表现。
- [ ] 测试补齐：每个新增 API 至少覆盖成功、失败和边界场景；避免依赖本机绝对路径。
- [ ] 文档示例：README 增加分组 API、加载、线程安全和常见失败原因说明。

### 3.2 2.0 暂不处理

- [ ] 分片寻路：不封装 `initSlicedFindPath`、`updateSlicedFindPath`、`finalizeSlicedFindPath`、`finalizeSlicedFindPathPartial`。
- [ ] 分块加载和动态地图：不封装 `addTile`、`removeTile`、`updateTile` 等 tile 增删改能力。
- [ ] 动态区域修改：不封装运行时 `setPolyFlags`、`setPolyArea` 等会改变 NavMesh 状态的能力。
- [ ] 流式和内存加载：不新增 `init(InputStream)`、`init(ByteBuffer)`，等确实需要资源包、对象存储或内存缓存加载时再设计。
- [ ] 更高版本升级：暂不升级 Java 11/17，也不升级到 recast4j 更高版本。

## 4. API 设计约束

- `Easy3dNav` 只做总入口、生命周期管理和少量高频快捷方法。
- 新能力优先放到分组 API 中，例如 `path()`、`query()`、`random()`、`debug()`。
- 新分组 API 优先使用 `Vector3f` 表达坐标，`float[]` 保留在旧快捷入口和 internal 实现中。
- `EasyNavFunc` 保持克制，只放长期稳定的基础能力。
- 简单 API 不暴露 recast4j 的 `NavMeshQuery`、`QueryFilter`、`Result`、tile、polygon ref 等类型。
- `internal` 包不作为稳定公开 API，后续可按实现需要调整。
- `QueryFilter` 暂不做 ThreadLocal 化，过滤器组合能力通过 `NavOptions` / `NavProfile` 单独设计。

## 5. 发布验收

发布 `2.0` 前至少满足：

- `mvn test` 通过。
- `mvn clean package` 通过。
- README 示例可用。
- 不提交本地 `.navmesh` 大文件、绝对路径、Maven/GPG/Sonatype 凭据。
- 新增能力都有确定性测试。
- 公开 API 不要求业务方直接依赖 recast4j 类型。

接口差距分析见 [recast4j Detour 接口差距分析](RECAST4J_API_GAP.md)。
