
# Easy3dNav [![Build Status](https://travis-ci.org/SilenceSu/Easy3dNav.svg?branch=master)](https://travis-ci.org/SilenceSu/recastNav) [![maven](https://maven-badges.herokuapp.com/maven-central/com.github.silencesu/Easy3dNav/badge.svg)](https://search.maven.org/search?q=Easy3dNav)
基于 recast4j 封装的 Java 版本 3D 游戏寻路组件，保留简单的 `find`、`raycast`、`findNearest` API，并通过官方 `org.recast4j:detour` 依赖提供底层 NavMesh 查询能力。

旧兼容类 `RecastNav` 已移除，请统一使用 `Easy3dNav`。


### 使用
````
<dependency>
  <groupId>com.github.silencesu</groupId>
  <artifactId>Easy3dNav</artifactId>
  <version>1.1.0</version>
</dependency>
````

### U3d数据导出插件

unity中navmesh数据使用以下工具导出 [https://github.com/kbengine/unity3d_nav_critterai](https://github.com/kbengine/unity3d_nav_critterai "kbengine/unity3d_nav_critterai")


 

### Code Demo

    //初始化寻路对象
    Easy3dNav nav = new Easy3dNav();
    nav.setUseU3dData(true);//默认为true，可以忽略
    nav.setPrintMeshInfo(true);//默认为false，查看需要设置为true
    //服务端用的为 srv_*.navmesh  
    nav.init(filePath);
    
    Vector3f start = new Vector3f(1f, 0f, 1f);
    Vector3f end = new Vector3f(9f, 0f, 9f);
    Vector3f point = new Vector3f(5f, 0f, 5f);

    //使用寻路接口，寻路
    List<Vector3f> paths = nav.find(start, end);

    //基础位置查询
    boolean walkable = nav.query().isWalkable(point);
    Vector3f closest = nav.query().closestPoint(point);
    Float height = nav.query().getHeight(point);

    //可达性判断
    boolean reachable = nav.path().isReachable(start, end);
    NavOptions options = NavOptions.builder()
            .extents(new Vector3f(2f, 4f, 2f))
            .maxStraightPath(64)
            .straightPathOptions(NavOptions.STRAIGHT_PATH_ALL_CROSSINGS)
            .anyAngle(true)
            .raycastLimit(-1f)
            .build();
    List<Vector3f> controlledPath = nav.path().find(start, end, options);
    Vector3f moved = nav.path().moveAlongSurface(start, end);
    Vector3f movedWithOptions = nav.path().moveAlongSurface(start, end, options);

    //随机可走点
    Vector3f patrol = nav.random().pointAround(point, 20f);
    Vector3f spawn = nav.random().pointWithin(point, 10f);

    //范围与墙体查询
    boolean hasArea = nav.query().hasWalkableAround(point, 3f);
    Float wallDistance = nav.query().distanceToWall(point, 5f);
    NavWallHit wall = nav.query().nearestWall(point, 5f);
    
    /***
    * 更多demo 请参考 src/test/java
    */
 
 
注：坐标系和unity中相同，y轴向上。一定要注意。

`moveAlongSurface` 用于服务端局部移动约束，例如防止客户端一步移动穿墙；它不替代完整寻路，也不保证自动修正到 NavMesh 表面高度。需要贴地高度时，可继续使用 `nav.query().getHeight(moved)` 或 `nav.query().closestPoint(moved)`。


### 参数说明

  - walkable height
  > 最低可通过高度。假设场景有一个桌子，如果设置过低，导致直接从桌子传过去，无视桌子存在。设置过高，会直接从桌子上通过
  - walkbale step
  > 设置从普通平面到有坡度地形，是否可以通过的高度阀值。
    设置过低，导致楼梯斜铺不能能通过
    设置过高，导致所有小桌子、小台阶等本不可走的，都可以走过去

  - walkable radius
  >默认问题不大
  - walkable slope  
  >默认问题不大
  


### 依赖&参考
https://github.com/recastnavigation/recastnavigation

https://github.com/ppiastucki/recast4j

当前项目使用 `org.recast4j:detour:1.5.7`。该版本仍兼容 Java 8；更新到更高版本前需要确认运行时 Java 基线。
 

### 学习资料：
http://www.critterai.org/projects/nmgen_study/

### 参考博客
https://jiangguilong2000.blog.csdn.net/article/details/125592067


### 功能规划

Easy3dNav 的定位是对 recast4j Detour 做服务端友好的轻量封装，不维护 recast4j fork。当前已提供 U3D/CritterAI NavMesh 加载、`find`、`raycast`、`findNearest`、`nav.query()` 基础位置查询、`nav.path()` 路径查询、`NavOptions` 和 `nav.random()` 随机点等接口。后续扩展优先围绕 Detour 查询能力补齐：

- 查询能力：已增加 `isWalkable`、`isReachable`、`getHeight`、`closestPoint`、`hasWalkableAround`、`distanceToWall`、`nearestWall`，后续补充 `getPolyWallSegments` 等调试能力。
- 路径控制：已增加 `NavOptions`，统一管理 `extents`、`maxStraightPath`、straight path options、find path options 和 raycast limit。
- 移动约束：已增加 `moveAlongSurface`，用于服务端局部移动校验和防穿墙，返回实际可移动到的位置。
- 区域与过滤器：封装 `QueryFilter`，支持不同单位类型、地形代价、区域屏蔽和业务自定义过滤策略。
- 随机点与范围查询：已增加 `random().point`、`pointAround`、`pointWithin`，后续补充候选点列表、`findLocalNeighbourhood` 等高级范围能力。
- 加载与管理：已补充 `File` 加载入口；`InputStream`、`ByteBuffer`、tile 信息查询、tile 热更新和调试数据输出后续按需要再设计。
- 并发约定：`Easy3dNav` 加载后的 `NavMesh` 按只读数据共享；每个线程会自动持有独立的 `NavMeshQuery`。默认 `QueryFilter` 不做 ThreadLocal 隔离，由后续过滤器封装统一规划。`init`/重载地图不应与查询并发执行；使用线程池并频繁卸载或重载地图时，可调用 `clearThreadLocalQuery()` 清理当前线程缓存。

### 版本规划

详细路线见 [Easy3dNav 版本计划书](VERSION_PLAN.md)，接口差距见 [recast4j Detour 接口差距分析](RECAST4J_API_GAP.md)。

- `2.0`：保持 Java 8 基线，使用 `org.recast4j:detour:1.5.7`，逐项补齐 Detour 查询封装、U3D 兼容测试和服务端常用 API。
- `3.x`：考虑升级 Java 11/17，并跟进 recast4j `1.5.8+` 或更高版本；该路线需要单独评估兼容性。
 
 
