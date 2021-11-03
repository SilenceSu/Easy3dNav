
# Easy3dNav [![Build Status](https://travis-ci.org/SilenceSu/Easy3dNav.svg?branch=master)](https://travis-ci.org/SilenceSu/recastNav) [![maven](https://maven-badges.herokuapp.com/maven-central/com.github.silencesu/Easy3dNav/badge.svg)](https://search.maven.org/search?q=Easy3dNav)
基于recast4j封装的Java版本3D游戏寻路组件


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
    nav.init(filePath);
    
    //使用寻路接口，寻路
    List<Float> paths=nav.find(float[] start, float[] end);
 
注：坐标系和unity中相同，y轴向上。一定要注意。


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
 

### 学习资料：
http://www.critterai.org/projects/nmgen_study/


### TODO
- 重构引用recast4j包
- 增加新功能
- 增加recastDemo build的文件
- 增加demo.exe 查看工具
 
 
