## ADDED Requirements

### Requirement: 查询到最近墙体距离

Easy3dNav SHALL 提供 `nav.query().distanceToWall(point, radius)`，用于查询点到最近墙体或边界的距离。

#### Scenario: 找到墙体距离

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **AND** 指定点附近半径范围内存在墙体或边界
- **WHEN** 调用方执行 `nav.query().distanceToWall(point, radius)`
- **THEN** 返回一个 `Float` 距离值

#### Scenario: 找不到墙体距离

- **GIVEN** 一个点无效、半径无效或半径范围内没有墙体的场景
- **WHEN** 调用方执行 `nav.query().distanceToWall(point, radius)`
- **THEN** 返回 `null`

### Requirement: 查询最近墙体信息

Easy3dNav SHALL 提供 `nav.query().nearestWall(point, radius)`，用于一次返回最近墙体距离、墙点坐标和法线。

#### Scenario: 找到最近墙体

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **AND** 指定点附近半径范围内存在墙体或边界
- **WHEN** 调用方执行 `nav.query().nearestWall(point, radius)`
- **THEN** 返回 `NavWallHit`
- **AND** `NavWallHit` 包含距离、墙点坐标和法线

#### Scenario: 找不到最近墙体

- **GIVEN** 一个点无效、半径无效或半径范围内没有墙体的场景
- **WHEN** 调用方执行 `nav.query().nearestWall(point, radius)`
- **THEN** 返回 `null`

### Requirement: 墙体 API 不暴露 Detour 类型

Easy3dNav SHALL 使用自有结果对象表达墙体查询结果，不要求调用方依赖 recast4j 类型。

#### Scenario: 调用方读取墙体结果

- **GIVEN** 调用方获得 `NavWallHit`
- **WHEN** 调用方读取距离、墙点坐标和法线
- **THEN** 调用方只使用 `Float` 和 `Vector3f`
- **AND** 不需要使用 `FindDistanceToWallResult`、polygon ref 或 `float[]`
