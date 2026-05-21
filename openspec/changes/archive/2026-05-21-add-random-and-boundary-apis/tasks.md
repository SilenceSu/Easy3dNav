## 1. API Surface

- [x] 1.1 新增 `api/NavRandomApi`，提供 `point`、`pointAround`、`pointWithin`。
- [x] 1.2 新增 `api/NavWallHit`，封装墙体距离、墙点坐标和法线。
- [x] 1.3 扩展 `api/NavQueryApi`，新增 `hasWalkableAround`、`distanceToWall`、`nearestWall`。
- [x] 1.4 在 `Easy3dNav` 中暴露 `random()` 分组入口。
- [x] 1.5 保持新增分组 API 只使用 `Vector3f`，不新增 `float[]` 公开重载。

## 2. Internal Detour Implementation

- [x] 2.1 确认 recast4j `findRandomPoint`、`findRandomPointAroundCircle`、`findRandomPointWithinCircle`、`findPolysAroundCircle`、`findDistanceToWall` 在 `1.5.7` 中的准确返回类型。
- [x] 2.2 在 internal 查询层实现整图随机点 `point`。
- [x] 2.3 在 internal 查询层实现附近随机点 `pointAround`。
- [x] 2.4 在 internal 查询层实现范围内随机点 `pointWithin`。
- [x] 2.5 在 internal 查询层实现范围可走性 `hasWalkableAround`。
- [x] 2.6 在 internal 查询层实现墙体距离 `distanceToWall`。
- [x] 2.7 在 internal 查询层实现最近墙体结果 `nearestWall`。
- [x] 2.8 为新增 internal 实现补充中文注释，说明 Detour 调用流程、失败语义和线程上下文边界。

## 3. Tests

- [x] 3.1 增加 `point` 成功和失败测试。
- [x] 3.2 增加 `pointAround` 成功和失败测试。
- [x] 3.3 增加 `pointWithin` 成功和失败测试。
- [x] 3.4 增加 `hasWalkableAround` 成功、失败和无效半径测试。
- [x] 3.5 增加 `distanceToWall` 成功、失败和无效半径测试。
- [x] 3.6 增加 `nearestWall` 结果对象字段测试。
- [x] 3.7 检查新增公开 API 未暴露 `float[]` 或 recast4j 类型。

## 4. Documentation And Verification

- [x] 4.1 更新 README，增加 `nav.random()`、范围可走性和墙体查询示例。
- [x] 4.2 更新 `VERSION_PLAN.md` 中对应待办项状态。
- [x] 4.3 更新 `RECAST4J_API_GAP.md`，统一使用 `nav.random()` 命名。
- [x] 4.4 运行 `mvn test`。
- [x] 4.5 运行 `mvn clean package`。
