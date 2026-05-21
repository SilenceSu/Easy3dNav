## Context

Easy3dNav 已经形成 `Easy3dNav` 门面 + 分组 API + internal Detour 封装的结构。基础位置查询和可达性已经放入 `nav.query()`、`nav.path()`，并确定新增分组 API 优先使用 `Vector3f`，不继续暴露 `float[]`。

本变更继续沿用该结构，补齐 AI 和移动校验常用能力：随机可走点、范围可走性、墙体距离和最近墙体信息。

## Goals / Non-Goals

**Goals:**

- 新增 `NavRandomApi`，通过 `nav.random()` 承载随机可走点能力。
- 在 `NavQueryApi` 中增加 `hasWalkableAround`、`distanceToWall`、`nearestWall`。
- 新增 `NavWallHit` 结果对象，封装距离、墙点坐标和法线。
- 继续使用 `Vector3f` 作为公开坐标类型。
- 复用 `NavMeshContext` 的线程级 `NavMeshQuery` 和默认 `QueryFilter`。
- 对失败语义保持简单：对象返回 `null`，布尔返回 `false`。

**Non-Goals:**

- 不引入 `NavOptions`。
- 不对外暴露 polygon ref、parent ref、cost、`FindRandomPointResult`、`FindPolysAroundResult` 或 `FindDistanceToWallResult`。
- 不开放 `getPolyWallSegments`，该能力后续放入 `nav.debug()` 单独设计。
- 不支持自定义随机源或 seed。
- 不实现自定义 shape 范围查询。

## Decisions

### Decision 1: 使用 `nav.random()` 而不是 `nav.sample()`

`sample` 更接近算法术语，普通调用方不一定能立刻理解。`random()` 更直观，符合 Easy3dNav 简化 API 的定位。

新增入口采用：

```java
nav.random().point();
nav.random().pointAround(center, radius);
nav.random().pointWithin(center, radius);
```

### Decision 2: 范围可走性留在 `nav.query()`

`hasWalkableAround(center, radius)` 的语义是查询某个已有位置附近是否存在可走区域，不生成新点，因此放在 `nav.query()`，而不是 `nav.random()`。

### Decision 3: 墙体信息使用结果对象

`findDistanceToWall` 一次返回距离、墙点坐标和法线。为了避免把墙点和法线拆成多个公开方法后重复执行底层查询，本变更提供：

```java
NavWallHit hit = nav.query().nearestWall(point, radius);
```

并保留轻量快捷方法：

```java
Float distance = nav.query().distanceToWall(point, radius);
```

### Decision 4: 范围查询先不返回 polygon 列表

`findPolysAroundCircle` 返回 refs、parentRefs、costs，偏 Detour 内部结构。第一版只用它支持 `hasWalkableAround` 这种业务语义，避免把 polygon path 数据模型暴露出去。后续如需要附近候选点，再设计 `pointsAround(center, radius, maxCount)` 或结果对象。

### Decision 5: 失败语义保持一致

- 随机点找不到时返回 `null`。
- 范围内没有可走 polygon 时 `hasWalkableAround` 返回 `false`。
- 墙体查询失败、点无效或半径范围内没有墙体时返回 `null`。
- 非法半径（小于等于 0）按失败处理，不抛出异常。

## Risks / Trade-offs

- **Risk:** 默认随机源不可控，测试和业务复现能力有限。  
  **Mitigation:** 第一版先保持简单；后续通过 `NavOptions` 或 `NavRandomOptions` 增加 seed。

- **Risk:** `hasWalkableAround` 只返回布尔值，无法告诉调用方具体有哪些区域。  
  **Mitigation:** 当前目标是轻量校验；高级范围结果后续单独设计。

- **Risk:** `NavWallHit` 会增加一个公开类型。  
  **Mitigation:** 该类型表达业务语义清晰，比暴露 recast4j result 更稳定。

## Migration Plan

1. 新增 `api/NavRandomApi` 和 `api/NavWallHit`。
2. 在 `Easy3dNav` 中暴露 `random()`。
3. 扩展 `NavQueryApi`，增加范围可走性和墙体查询方法。
4. 在 internal 查询层封装 Detour 随机点、范围查询和墙体查询调用。
5. 增加动态 fixture 测试，覆盖成功、失败、半径边界和公开 API 类型。
6. 更新 README、`VERSION_PLAN.md` 和 `RECAST4J_API_GAP.md`。
