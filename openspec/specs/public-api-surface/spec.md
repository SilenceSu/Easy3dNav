## Requirements

### Requirement: Root Package Exposes Only Stable Simple API

Easy3dNav SHALL keep the root package focused on stable user-facing API types.

#### Scenario: User imports simple navigation API

- **GIVEN** a caller wants basic pathfinding, raycast, or nearest-point lookup
- **WHEN** the caller imports types from `com.github.silencesu.Easy3dNav`
- **THEN** the caller can use `Easy3dNav`, `EasyNavFunc`, and `Vector3f`
- **AND** the caller is not required to import implementation helper classes

### Requirement: Deprecated RecastNav Compatibility Class Is Removed

Easy3dNav SHALL remove the deprecated `RecastNav` compatibility class from the public API.

#### Scenario: New code uses Easy3dNav directly

- **GIVEN** a caller needs to load a NavMesh from a file path
- **WHEN** the caller creates a navigation object
- **THEN** the caller uses `Easy3dNav`
- **AND** `RecastNav` is not available as an alternative wrapper class

#### Scenario: Existing RecastNav users migrate

- **GIVEN** existing code previously used `new RecastNav(filePath)`
- **WHEN** the code is migrated to the simplified API
- **THEN** it can use `new Easy3dNav(filePath)` instead
- **AND** no recast4j-specific setup is required for the migration

### Requirement: Internal Implementation Details Are Isolated

Easy3dNav SHALL place non-user-facing implementation details outside of the root API surface.

#### Scenario: Query context is an implementation detail

- **GIVEN** Easy3dNav manages `NavMesh`, `NavMeshQuery`, and default query filtering internally
- **WHEN** maintainers reorganize the implementation
- **THEN** query context helpers are placed under an internal package or kept private
- **AND** they are not documented as stable user API

#### Scenario: I/O adapters remain separate from the facade

- **GIVEN** Easy3dNav supports U3D/CritterAI and recast4j NavMesh loading
- **WHEN** loading code is organized
- **THEN** reader and loader implementations remain under `io` or internal packages
- **AND** the facade exposes simple initialization methods

### Requirement: Automated Tests Do Not Silently Skip

Easy3dNav SHALL avoid Maven default tests that pass by returning early when local data is missing.

#### Scenario: Default test suite runs deterministic tests

- **GIVEN** the Maven test suite is executed on a clean machine
- **WHEN** a test class is included in the default Surefire run
- **THEN** the test either executes deterministic assertions
- **AND** it does not pass solely because a local absolute-path fixture was not configured

### Requirement: 分组查询与路径 API

Easy3dNav SHALL 暴露位置查询和路径查询的分组 API 入口。

#### Scenario: 调用方访问位置查询 API

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **WHEN** 调用方执行 `nav.query()`
- **THEN** 返回对象暴露 `isWalkable`、`closestPoint`、`getHeight` 等位置查询方法
- **AND** 这些方法不要求调用方使用 recast4j 实现类型

#### Scenario: 调用方访问路径 API

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **WHEN** 调用方执行 `nav.path()`
- **THEN** 返回对象暴露 `isReachable` 等路径相关方法
- **AND** 现有 `Easy3dNav.find` 快捷方法继续可用

### Requirement: 随机点分组 API

Easy3dNav SHALL 暴露随机点生成的分组 API 入口。

#### Scenario: 调用方访问随机点 API

- **GIVEN** 一个已初始化的 Easy3dNav 实例
- **WHEN** 调用方执行 `nav.random()`
- **THEN** 返回对象暴露 `point`、`pointAround`、`pointWithin` 等随机可走点方法
- **AND** 这些方法不要求调用方使用 recast4j 实现类型

#### Scenario: 直接入口继续可用

- **GIVEN** 调用方使用现有 `Easy3dNav.find`、`raycast` 或 `findNearest`
- **WHEN** 新增 `random()` 分组入口后
- **THEN** 现有快捷入口继续可用
