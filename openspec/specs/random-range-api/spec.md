## Requirements

### Requirement: 随机生成整图可走点

Easy3dNav SHALL 提供 `nav.random().point()`，用于在整张 NavMesh 上随机生成一个可走点。

#### Scenario: 找到整图随机点

- **GIVEN** 一个已初始化且存在可走 polygon 的 Easy3dNav 实例
- **WHEN** 调用方执行 `nav.random().point()`
- **THEN** 返回一个 `Vector3f` 可走点
- **AND** 不暴露 polygon ref 或 recast4j result 类型

#### Scenario: 整图随机点查询失败

- **GIVEN** 一个未能找到可走 polygon 的查询场景
- **WHEN** 调用方执行 `nav.random().point()`
- **THEN** 返回 `null`

### Requirement: 随机生成附近可走点

Easy3dNav SHALL 提供 `nav.random().pointAround(center, radius)`，用于在中心点附近随机生成可走点。

#### Scenario: 找到附近随机点

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **AND** 中心点附近存在可走区域
- **WHEN** 调用方执行 `nav.random().pointAround(center, radius)`
- **THEN** 返回一个 `Vector3f` 可走点

#### Scenario: 中心点无效或附近没有可走点

- **GIVEN** 一个中心点无法定位到有效 polygon 或附近没有可走区域的场景
- **WHEN** 调用方执行 `nav.random().pointAround(center, radius)`
- **THEN** 返回 `null`

### Requirement: 随机生成范围内可走点

Easy3dNav SHALL 提供 `nav.random().pointWithin(center, radius)`，用于在指定圆形范围内随机生成可走点。

#### Scenario: 找到范围内随机点

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **AND** 指定圆形范围内存在可走区域
- **WHEN** 调用方执行 `nav.random().pointWithin(center, radius)`
- **THEN** 返回一个 `Vector3f` 可走点

#### Scenario: 半径无效或范围内没有可走点

- **GIVEN** 一个半径小于等于 0 或范围内没有可走区域的场景
- **WHEN** 调用方执行 `nav.random().pointWithin(center, radius)`
- **THEN** 返回 `null`

### Requirement: 查询附近是否存在可走区域

Easy3dNav SHALL 提供 `nav.query().hasWalkableAround(center, radius)`，用于判断中心点附近是否存在可走 polygon。

#### Scenario: 附近存在可走区域

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **AND** 中心点附近存在可走 polygon
- **WHEN** 调用方执行 `nav.query().hasWalkableAround(center, radius)`
- **THEN** 返回 `true`

#### Scenario: 附近不存在可走区域

- **GIVEN** 一个中心点无效、半径无效或范围内没有可走 polygon 的场景
- **WHEN** 调用方执行 `nav.query().hasWalkableAround(center, radius)`
- **THEN** 返回 `false`

### Requirement: 随机与范围 API 使用 Vector3f

Easy3dNav SHALL 为随机点与范围查询 API 使用 `Vector3f` 表达坐标。

#### Scenario: 调用方不使用 float 数组

- **GIVEN** 一个调用方使用随机点或范围查询 API
- **WHEN** 调用方传入中心点或接收返回值
- **THEN** 公开 API 使用 `Vector3f`
- **AND** 不要求调用方使用 `float[]`
