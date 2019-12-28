
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
 
 