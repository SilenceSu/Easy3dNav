## Why

`RECAST4J_API_GAP.md` 中“随机点与范围查询”“墙体与边界查询”是 AI、出生点、巡逻、避障和脱困逻辑常用能力。当前调用方只能直接理解 Detour 的 polygon ref 或自行组合底层查询，和 Easy3dNav 简化 API 的方向不一致。

## What Changes

- 新增随机点分组 API：
  - `nav.random().point()`：在整张 NavMesh 上随机取一个可走点。
  - `nav.random().pointAround(center, radius)`：在中心点附近随机取一个可走点。
  - `nav.random().pointWithin(center, radius)`：在指定圆形范围内随机取一个可走点。
- 新增范围可走性 API：
  - `nav.query().hasWalkableAround(center, radius)`：判断中心点附近是否存在可走区域。
- 新增墙体与边界查询 API：
  - `nav.query().distanceToWall(point, radius)`：查询点到最近墙体或边界的距离。
  - `nav.query().nearestWall(point, radius)`：一次返回距离、墙点坐标和墙体法线。
- 新增轻量结果对象 `NavWallHit`，不暴露 recast4j `FindDistanceToWallResult`。
- 保持新分组 API 使用 `Vector3f`，不新增 `float[]` 重载。
- 暂不开放 `getPolyWallSegments`、polygon ref、tile 查询或自定义随机源。

## Capabilities

### New Capabilities

- `random-range-api`: 定义随机可走点生成和范围可走性查询的公开 API 语义。
- `boundary-query-api`: 定义墙体距离、最近墙点和墙体法线的公开 API 语义。

### Modified Capabilities

- `public-api-surface`: 增加 `random()` 分组入口，并约束新增 API 不暴露 recast4j 类型。

## Impact

- 影响 `Easy3dNav` 对外 API：新增 `random()` 分组入口。
- 新增 `api/NavRandomApi`，并扩展 `api/NavQueryApi`。
- 新增 `NavWallHit` 结果对象，面向业务返回墙体距离、墙点和法线。
- 扩展 internal Detour 查询层，封装 `findRandomPoint`、`findRandomPointAroundCircle`、`findRandomPointWithinCircle`、`findPolysAroundCircle`、`findDistanceToWall`。
- 需要新增动态 fixture 测试覆盖成功、失败、`Vector3f`、半径边界和公开 API 类型检查。
