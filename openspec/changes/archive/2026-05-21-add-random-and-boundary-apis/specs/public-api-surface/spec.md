## ADDED Requirements

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
