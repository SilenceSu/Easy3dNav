
# recastNav
基于recast4j封装的开箱即用项目。


### 数据来源critterai 插件

unity中navmesh数据使用以下工具导出 [https://github.com/kbengine/unity3d_nav_critterai](https://github.com/kbengine/unity3d_nav_critterai "kbengine/unity3d_nav_critterai")


### 使用
1、clone
2、mvn install 
3、引用到自己的项目


### Code Demo

    //初始化寻路对象
    RecastNav nav = new RecastNav(String filePath)
    //使用寻路接口，寻路
    List<Float> paths=nav.find(float[] start, float[] end);
 
注：坐标系和unity中相同，y轴向上。一定要注意。

 


### 依赖&参考
https://github.com/recastnavigation/recastnavigation

https://github.com/ppiastucki/recast4j
 

### 学习资料：
http://www.critterai.org/projects/nmgen_study/


### TODO
- 增加recastDemo build的文件
- 增加demo.exe 查看工具
 
 