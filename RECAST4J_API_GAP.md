# recast4j Detour 剩余功能差距

本文档按大功能分类记录 Easy3dNav 相对 `org.recast4j:detour:1.5.7` 仍缺少的能力。它是差距记录，不代表这些能力都需要实现；当前原则是保持 Easy3dNav API 简单，只有出现明确业务场景时再设计公开接口。

## 1. 路径结果增强

当前已经能查找路径、判断可达性，并支持常用路径参数。仍缺少：

- 路径查询状态，例如 partial path、失败状态和 Detour 原始状态分类。
- polygon path 或等价的结构化路径段信息。
- straight path 的 flags/ref 等细节。
- 自定义 heuristic 或更细的寻路策略控制。

## 2. 射线检测增强

当前 `raycast` 只返回终点或阻挡点。仍缺少：

- 命中法线。
- raycast 经过的路径或 polygon 信息。
- 射线检测 cost。
- 基于 cost 的射线检测选项。

## 3. 位置与边界查询增强

当前已经有可走性、最近点、高度、最近墙体和墙体线段等能力。仍缺少：

- 边界内投影结果。
- 指定 polygon 或底层 ref 的校验和查询能力。
- 更细的墙体 portal/ref 信息。

## 4. 范围与随机查询增强

当前已经有整图/附近/范围内随机点、附近是否可走、局部邻域 polygon。仍缺少：

- AABB 范围内 polygon 查询。
- 自定义形状范围查询。
- 范围查询的 cost、parent 或搜索树细节。
- 自定义随机源。
- 自定义圆形随机约束。

## 5. 过滤器与区域代价

当前公开 API 使用默认过滤器，不暴露过滤器配置。仍缺少：

- include flags / exclude flags 配置。
- area cost 配置。
- 自定义通行过滤逻辑。
- 自定义路径代价计算。

这类能力暂不处理，避免把 recast4j 的低层过滤模型直接暴露给业务调用方。

## 6. 调试搜索状态

当前调试能力主要是 NavMesh metadata 和墙体线段。仍缺少：

- Dijkstra 搜索路径读取。
- closed-list 查询。
- node pool 调试信息。

## 7. Tile 与 NavMesh 管理

当前只提供 NavMesh metadata 摘要，不暴露底层 tile/ref。仍缺少：

- 只读 tile 查询。
- 世界坐标到 tile 坐标转换。
- off-mesh connection 端点摘要。
- tile 增删改。
- polygon flags / area 运行时读写。

其中 tile 增删改和 polygon flags/area 修改属于动态 NavMesh 管理，当前暂不处理。

## 8. IO 与格式入口

当前支持 `init(String)`、`init(File)`，并通过 `setUseU3dData(boolean)` 选择 U3D/CritterAI 或 recast4j 标准格式。仍缺少：

- `InputStream` 加载。
- `ByteBuffer` 加载。
- 显式格式枚举或格式自动识别。
- NavMesh 写回或导出。

这些能力只有在资源包、对象存储、内存缓存或工具链导出场景明确后再设计。

## 9. 明确不暴露的低层能力

以下能力当前不作为 Easy3dNav 公开 API 目标：

- 直接返回 Detour polygon ref。
- 直接暴露底层 `NavMesh`、`NavMeshQuery`、`QueryFilter`、tile、poly 或 recast4j result 类型。
- 分片寻路 sliced path 系列接口。
- 运行时动态修改 NavMesh 数据。
