# 仓库指南

## 项目结构与模块组织

Easy3dNav 是一个基于 Java 8 和 Maven 的 3D NavMesh 寻路库。核心 API 位于 `src/main/java/com/github/silencesu/Easy3dNav`，主要包括 `Easy3dNav`、`EasyNavFunc`、`RecastNav` 和 `Vector3f`。底层 Detour 查询能力来自官方依赖 `org.recast4j:detour`；本仓库只保留 Easy3dNav 封装和 `src/main/java/com/github/silencesu/Easy3dNav/io` 下的 U3D/CritterAI 读取适配层。测试和示例代码放在 `src/test/java`。仓库没有独立资源目录，除非用于小型测试夹具，否则不要提交本地导出的 `.navmesh` 文件。

## 构建、测试与开发命令

- `mvn test`：编译项目并运行 JUnit 测试。
- `mvn clean package`：构建库 JAR，并附带源码 JAR 和 Javadoc JAR。
- `mvn clean package -DskipTests=true`：仅打包不运行测试，与 Travis 构建流程一致。
- `mvn -PdeployMvnCenter deploy`：使用 Sonatype 发布配置部署，需要本地 OSSRH 和 GPG 凭据。

## 编码风格与命名约定

使用 Java 8 语法，源码保持 UTF-8 编码。遵循现有四空格缩进和大括号风格。公共 API 类使用 `PascalCase`，方法和字段使用 `camelCase`，新增常量使用 `UPPER_SNAKE_CASE`。保持当前包名 `com.github.silencesu.Easy3dNav`。公共能力优先通过 `EasyNavFunc` 和 `Easy3dNav` 暴露，不要重新复制 recast4j 核心源码。

## 测试指南

测试框架为 JUnit 4，使用 `org.junit.Test` 和 `org.junit.Before`。测试类按组件或行为命名，例如 `NavMeshQueryTest` 或 `Easy3dNavTest`。避免因缺少数据而静默返回的测试；应使用小型、确定性的夹具，或明确测试前置条件。提交前运行 `mvn test`。修改寻路、射线检测、最近可行走点、线程级查询上下文或 NavMesh I/O 时，应补充回归测试；并发相关修改需要覆盖多线程调用场景。

## 提交与 Pull Request 规范

近期提交历史使用简短直接的标题，例如 `Update README.md`、`fix test` 和版本说明。提交应保持聚焦，尽量使用祈使句摘要，例如 `Fix navmesh tile bounds`。Pull Request 应说明行为变化，列出已运行的测试命令，关联相关 issue；如果路径结果或 NavMesh 格式发生变化，应提供示例输入和输出。

## 安全与配置提示

不要提交本地 NavMesh 导出文件、机器上的绝对路径、Sonatype 凭据或 GPG 材料。发布所需凭据应保存在 Maven settings 或环境专用配置中，不应放入仓库。
