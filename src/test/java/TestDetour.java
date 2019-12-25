import com.github.silencesu.Easy3dNav.Easy3dNav;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author SilenceSu
 * @Email Silence.Sx@Gmail.com
 * Created by Silence on 2018/8/10.
 */
public class TestDetour {

    private Easy3dNav nav;


    private float[][] startPoints = new float[][]{
            { -72.77f, 84.43f,   8.23f},
//            {17.9f, 18.69f, 15.12f},
//            {22.76f, 14.31f, 0.28f},
//            {29.4f,12.2f,-7.9f},
//            {-11.06f,10.24f,-38.54f},
//            {-0.32f,8.15f,-36.71f},
//            {9.05f,6.21f,-42.38f},
    };

    private float[][] endPoints = new float[][]{
            {-79.20f, 84.44f,  7.37f},
//            {-79.20f, 85.f, 26.3f},
//            {5.15f, 16.3f, 26.3f},
//            {27.8f, 12.84f, -4.84f},
//            {35.4f,11.57f,-14.22f},
//            {-4.3f,9.11f,-38.17f},
//            {6.53f,7f,-40.31f},
//            {15.56f,4.49f,-43.63f},
    };


    @Before
    public void init() throws IOException {
//        nav = new Easy3dNav("E:\\srv_CAIBakedNavmesh.navmesh");
//        nav = new Easy3dNav("D:\\Nfantasy\\client\\data\\map\\srv_11_hd_pk_01.navmesh");
//        nav = new Easy3dNav("D:\\JProject\\Easy3dNav\\src\\main\\resources\\all_tiles_navmesh.bin");

    }


    @Test
    public void findPath() {
        if (nav == null) {
            return;
        }
//        List<float[]> list = nav.find(new float[]{-13.22f, 18.86f, 21.06f}, new float[]{-18.48f, 18.83f, 20.29f}, new float[]{2.f, 2.f, 2.f});
////        List<float[]> list = nav.find(new float[]{7.3517575f, 2.6341214f, -38.87209f}, new float[]{13.129725f, 2.119f, -32.205204f});
//        list.forEach(f -> {
//            System.out.print(f[0]);
//            System.out.print("   ");
//            System.out.print(f[1]);
//            System.out.print("   ");
//            System.out.print(f[2]);
//            System.out.println();
//        });

        int group = startPoints.length;

        for (int i = 0; i < group; i++) {

            float[] startP = startPoints[i];
            float[] endP = endPoints[i];
            System.out.println("输出第"+(i+1)+"组数据start:{"+startP[0]+","+startP[1]+","+startP[2]+"}"+"end:{"+endP[0]+","+endP[1]+","+endP[2]+"}");



          List<float[]> list = nav.find(startP, endP, new float[]{2.f, 2.f, 2.f});
             list.forEach(f -> {
                System.out.print(f[0]);
                System.out.print("   ");
                System.out.print(f[1]);
                System.out.print("   ");
                System.out.print(f[2]);
                System.out.println();
            });




        }

    }



}
