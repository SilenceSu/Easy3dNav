# Easy3dNav [![Build Status](https://travis-ci.org/SilenceSu/Easy3dNav.svg?branch=master)](https://travis-ci.org/SilenceSu/recastNav)

基于 recast4j 封装的 Java 版本 3D 游戏寻路组件，面向服务端 NavMesh 查询场景，保留简单的 `find`、`raycast`、`findNearest` 快捷入口，并通过官方 `org.recast4j:detour` 依赖提供底层 NavMesh 查询能力。

### 重大升级说明

当前版本是一次面向 2.0 的重大升级，目标是把 Easy3dNav 从早期的简单寻路封装整理成更清晰的服务端 NavMesh 查询库。升级后不保证完全兼容旧 API，旧项目接入前需要做一次编译和行为回归检查。

主要不兼容点：

- 旧兼容类 `RecastNav` 已移除，请统一使用 `Easy3dNav`。
- 本地复制的 recast4j Detour 源码已替换为官方 `org.recast4j:detour` Maven 依赖。
- 新能力优先通过 `nav.query()`、`nav.path()`、`nav.random()`、`nav.debug()` 分组入口暴露，旧快捷入口只保留少量高频能力。
- 公开 API 尽量使用 `Vector3f`、自有结果对象和 Java 集合，不再鼓励业务代码依赖 recast4j 底层类型、polygon ref 或 `float[]`。
- 普通查询失败语义已统一，列表返回空列表，点/数值返回 `null`，布尔返回 `false`。

### 主要特性

- **路径查询**：支持 `find`、`nav.path().find`、可达性判断和 `NavOptions` 路径参数。
- **位置查询**：支持可走性、最近可走点、高度、附近是否有可走区域、局部邻域 polygon、最近墙体距离和墙体命中信息。
- **移动约束**：支持 `moveAlongSurface`，可用于服务端局部移动校验和防穿墙。
- **随机点**：支持整图随机点、中心点附近随机点和指定范围内随机点。
- **调试与可视化**：支持 NavMesh metadata 摘要和 polygon 墙体/边界线段查询。
- **加载格式**：支持 Unity/CritterAI U3D `.navmesh` 和 recast4j 标准 MeshSet 格式，通过 `setUseU3dData(boolean)` 在初始化前选择。
- **线程模型**：加载后的 `NavMesh` 按只读数据共享，每个线程自动持有独立的 `NavMeshQuery`。
- **Java 基线**：继续保持 Java 8，当前底层依赖 `org.recast4j:detour:1.5.7`。recast4j `1.5.11` 已使用 Java 11 编译，不能用于当前 Java 8 基线。

### 快速开始

```java
Easy3dNav nav = new Easy3dNav();
nav.setUseU3dData(true); // 默认 true，读取 Unity/CritterAI U3D navmesh
nav.init(filePath);

Vector3f start = new Vector3f(1f, 0f, 1f);
Vector3f end = new Vector3f(9f, 0f, 9f);
Vector3f point = new Vector3f(5f, 0f, 5f);

List<Vector3f> path = nav.path().find(start, end);
boolean reachable = nav.path().isReachable(start, end);

boolean walkable = nav.query().isWalkable(point);
Vector3f closest = nav.query().closestPoint(point);
Float height = nav.query().getHeight(point);

Vector3f patrol = nav.random().pointAround(point, 20f);
List<NavLocalPolygon> localPolygons = nav.query().findLocalNeighbourhood(point, 5f);
NavWallHit wall = nav.query().nearestWall(point, 5f);

NavMeshInfo meshInfo = nav.debug().meshInfo();
```

坐标系和 Unity 相同，Y 轴向上。更多示例可参考 `src/test/java`。

### API 列表与使用场景

优先使用分组 API：`nav.query()` 做位置/范围查询，`nav.path()` 做路径和移动约束，`nav.random()` 做随机点生成，`nav.debug()` 做调试和可视化。`Easy3dNav.find/raycast/findNearest` 是保留的快捷入口，适合旧代码迁移或非常简单的调用场景。

#### 初始化与配置

| API | 什么时候用 | 示例 |
| --- | --- | --- |
| `new Easy3dNav()` | 创建寻路对象，后续再手动 `init`。 | `Easy3dNav nav = new Easy3dNav();` |
| `new Easy3dNav(filePath)` | 路径已确定，创建时直接加载 NavMesh。 | `Easy3dNav nav = new Easy3dNav("srv_map.navmesh");` |
| `setUseU3dData(true)` | 读取 Unity/CritterAI 导出的 U3D `.navmesh`，这是默认模式。 | `nav.setUseU3dData(true);` |
| `setUseU3dData(false)` | 读取 recast4j 标准 MeshSet 格式。 | `nav.setUseU3dData(false);` |
| `init(String)` / `init(File)` | 加载或重新加载 NavMesh；切地图时使用。 | `nav.init(filePath);` |
| `setExtents(float[])` | 设置默认 nearest polygon 搜索范围；点可能离 NavMesh 表面有高度差时调大 Y。 | `nav.setExtents(new float[]{2f, 4f, 2f});` |
| `clearThreadLocalQuery()` | 使用线程池并频繁重载地图时，清理当前线程缓存的查询对象。 | `nav.clearThreadLocalQuery();` |

#### 快捷入口

| API | 什么时候用 | 示例 |
| --- | --- | --- |
| `find(start, end)` | 只需要最简单的路径点列表，旧代码迁移时也可以继续用。 | `List<Vector3f> path = nav.find(start, end);` |
| `raycast(start, end)` | 判断一条直线在 NavMesh 上是否被墙体或边界阻挡；返回终点或阻挡点。 | `Vector3f hit = nav.raycast(start, end);` |
| `findNearest(point)` | 把一个点吸附到附近最近的 NavMesh 表面。 | `Vector3f p = nav.findNearest(rawPoint);` |

#### `nav.query()` 位置与范围查询

| API | 什么时候用 | 示例 |
| --- | --- | --- |
| `isWalkable(point)` | 只想知道某个坐标附近是否存在可走 polygon。 | `boolean ok = nav.query().isWalkable(point);` |
| `closestPoint(point)` | 客户端上报的位置可能偏离 NavMesh，需要吸附到最近可走点。 | `Vector3f fixed = nav.query().closestPoint(point);` |
| `getHeight(point)` | 需要获取 NavMesh 表面高度，例如把角色贴地。 | `Float y = nav.query().getHeight(point);` |
| `hasWalkableAround(center, radius)` | 只判断附近是否有可走区域，不关心具体点或 polygon。 | `boolean has = nav.query().hasWalkableAround(center, 3f);` |
| `findLocalNeighbourhood(center, radius)` | 需要拿到中心点附近连通的 NavMesh polygon，用于局部范围分析或可视化。 | `List<NavLocalPolygon> polys = nav.query().findLocalNeighbourhood(center, 5f);` |
| `distanceToWall(point, radius)` | 只需要知道角色离最近墙体/边界还有多远。 | `Float d = nav.query().distanceToWall(point, 5f);` |
| `nearestWall(point, radius)` | 需要最近墙体的距离、位置和法线，用于避障或调试。 | `NavWallHit wall = nav.query().nearestWall(point, 5f);` |

#### `nav.path()` 路径与移动约束

| API | 什么时候用 | 示例 |
| --- | --- | --- |
| `find(start, end)` | 推荐的新路径查询入口，返回 `Vector3f` 路径点。 | `List<Vector3f> path = nav.path().find(start, end);` |
| `find(start, end, options)` | 需要控制搜索范围、最大路径点数、straight path 选项或任意角路径时使用。 | `List<Vector3f> path = nav.path().find(start, end, options);` |
| `isReachable(start, end)` | 只判断两点是否连通，不需要完整路径。 | `boolean ok = nav.path().isReachable(start, end);` |
| `moveAlongSurface(start, end)` | 服务端校验一次移动是否能沿 NavMesh 表面走到目标点，适合防穿墙。 | `Vector3f moved = nav.path().moveAlongSurface(start, target);` |

#### `nav.random()` 随机可走点

| API | 什么时候用 | 示例 |
| --- | --- | --- |
| `point()` | 在整张 NavMesh 上随机取一个可走点，例如全局测试或随机出生。 | `Vector3f p = nav.random().point();` |
| `pointAround(center, radius)` | 在某个点附近随机取点，例如巡逻点、逃跑点。 | `Vector3f patrol = nav.random().pointAround(center, 20f);` |
| `pointWithin(center, radius)` | 必须限制在圆形范围内随机取点，例如技能落点或区域刷怪。 | `Vector3f spawn = nav.random().pointWithin(center, 10f);` |

#### `nav.debug()` 调试与可视化

| API | 什么时候用 | 示例 |
| --- | --- | --- |
| `getWallSegments(point)` | 需要画出某个 polygon 的墙体/边界线段，主要用于调试和可视化。 | `List<NavWallSegment> walls = nav.debug().getWallSegments(point);` |
| `meshInfo()` | 需要查看当前加载地图的 tile、polygon、vertex、bounds 等摘要信息。 | `NavMeshInfo info = nav.debug().meshInfo();` |

#### `NavOptions` 单次查询参数

| 参数 | 什么时候用 | 示例 |
| --- | --- | --- |
| `extents` | 点不一定正好落在 NavMesh 上，需要扩大 nearest polygon 搜索范围。 | `NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();` |
| `maxStraightPath` | 限制 `nav.path().find` 最多返回多少个路径点。 | `.maxStraightPath(64)` |
| `straightPathOptions` | 需要返回区域穿越点或所有 polygon 穿越点时使用。 | `.straightPathOptions(NavOptions.STRAIGHT_PATH_ALL_CROSSINGS)` |
| `anyAngle(true)` | 希望 Detour 尝试更直接的任意角路径时使用。 | `.anyAngle(true)` |
| `raycastLimit` | 配合任意角路径控制 Detour raycast limit。 | `.raycastLimit(-1f)` |

### 关键说明

`moveAlongSurface` 用于服务端局部移动约束，例如防止客户端一步移动穿墙；它不替代完整寻路，也不保证自动修正到 NavMesh 表面高度。需要贴地高度时，可继续使用 `nav.query().getHeight(moved)` 或 `nav.query().closestPoint(moved)`。

`findLocalNeighbourhood` 返回中心点附近连通的 NavMesh polygon 几何信息，可用于局部范围分析或可视化。结果是导航网格的简化 polygon，不等于原始场景模型网格；方法不会暴露 Detour polygon ref 或 parent ref。

### 加载格式

Easy3dNav 当前只提供文件路径和 `File` 两个加载入口：`init(String)`、`init(File)`。加载格式通过 `setUseU3dData(boolean)` 在初始化前选择：

- 默认 `true`：读取 Unity/CritterAI 导出的 U3D `.navmesh` 格式，通常对应服务端使用的 `srv_*.navmesh` 文件。
- 设置为 `false`：读取 recast4j 标准 MeshSet 格式。
- 该开关必须在 `init(...)` 前设置；`init(...)` 后修改只会影响下一次加载。
- 当前不提供 `InputStream`、`ByteBuffer` 加载，也不做格式自动识别。

Unity 中 NavMesh 数据可使用以下工具导出：[kbengine/unity3d_nav_critterai](https://github.com/kbengine/unity3d_nav_critterai "kbengine/unity3d_nav_critterai")。

### 返回语义

公开 API 按返回类型表达普通查询失败：

- 列表型查询返回空列表，例如 `find`、`nav.path().find`、`nav.query().findLocalNeighbourhood`、`nav.debug().getWallSegments`。
- 点结果查询返回 `null`，例如 `closestPoint`、`moveAlongSurface`、`random().pointAround`。
- 布尔查询返回 `false`，例如 `isWalkable`、`isReachable`、`hasWalkableAround`。
- 数值查询返回 `null`，例如 `getHeight`、`distanceToWall`。
- `nav.debug().meshInfo()` 需要已初始化 NavMesh；未初始化调用会抛出 `IllegalStateException`。

普通无结果包括找不到有效 polygon、半径小于等于 0、空坐标参数或 Detour 查询失败。旧快捷入口 `raycast` 保持兼容语义：起点无效或参数为空时抛出 `IllegalArgumentException`。

### 线程模型

`Easy3dNav` 加载后的 `NavMesh` 按只读数据共享，每个线程会自动持有独立的 `NavMeshQuery`。`init` 或重载地图不应与查询并发执行；使用线程池并频繁卸载或重载地图时，可调用 `clearThreadLocalQuery()` 清理当前线程缓存。

### 参数说明

以下参数通常在 Unity/CritterAI 导出 NavMesh 时生效。Easy3dNav 读取的是已经生成好的 `.navmesh` 数据，运行时查询一般不会再改变这些生成参数。

- `walkable height`：最低可通过高度。设置过低，桌子、低矮遮挡等可能被当成可穿过空间；设置过高，原本可以通过的矮洞或低顶区域会不可走。
- `walkable step`：可跨越台阶高度。设置过低，楼梯或小台阶可能不可走；设置过高，小桌子、小台阶等本不可走的障碍可能被当成可走。
- `walkable radius`：角色半径，表示角色在水平方向需要占用的空间。值越大，NavMesh 会离墙、柱子、障碍物边缘越远，窄通道也越容易被判定为不可走。
- `walkable slope`：最大可行走坡度，表示角色能走上去的最大斜坡角度。值越小，稍微陡一点的坡就会被判定为不可走；值越大，越陡的坡也可能进入 NavMesh。

### 能力边界

Easy3dNav 的定位是对 recast4j Detour 做服务端友好的轻量封装，不维护 recast4j fork。当前暂不扩展公开过滤器配置，不直接暴露 polygon ref、tile、poly、`NavMeshQuery`、`QueryFilter` 或 recast4j result 类型。剩余功能差距见 [recast4j Detour 剩余功能差距](RECAST4J_API_GAP.md)。

### 版本说明

- `2.0`：重大升级版本，不保证完全兼容旧 API；保持 Java 8 基线，使用 `org.recast4j:detour:1.5.7`，优先补齐服务端常用查询能力。
- `3.x`：考虑升级 Java 11/17，并跟进 recast4j `1.5.8+` 或更高版本；该路线需要单独评估兼容性。

### 依赖与参考

- [recastnavigation/recastnavigation](https://github.com/recastnavigation/recastnavigation)
- [recast4j](https://github.com/ppiastucki/recast4j)
- [CritterAI 学习资料](http://www.critterai.org/projects/nmgen_study/)
- [参考博客](https://jiangguilong2000.blog.csdn.net/article/details/125592067)
