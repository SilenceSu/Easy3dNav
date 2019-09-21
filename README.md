
# navigation4j
navmesh  java版本库


### 数据来源critterai 插件

unity中navmesh数据使用以下工具导出 [https://github.com/kbengine/unity3d_nav_critterai](https://github.com/kbengine/unity3d_nav_critterai "kbengine/unity3d_nav_critterai")


### 使用
直接引用lib包或者maven引用

### Code Demo

    //初始化寻路对象
    RecastNav nav = new RecastNav(String filePath)
    //使用寻路接口，寻路
     List<Float> paths=nav.find(float[] start, float[] end);

 
注：坐标系和unity中相同，y轴向上。一定要注意。

 

 
 