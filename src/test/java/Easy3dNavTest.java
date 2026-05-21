import com.github.silencesu.Easy3dNav.Easy3dNav;
import org.junit.Test;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.io.MeshDataWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Easy3dNavTest {

    @Test
    public void loadsU3dMeshAndFindsPath() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        List<float[]> path = nav.find(new float[]{1f, 0f, 1f}, new float[]{9f, 0f, 9f}, new float[]{2f, 4f, 2f});

        assertFalse(path.isEmpty());
        assertArrayEquals(new float[]{1f, 0f, 1f}, path.get(0), 0.001f);
        assertArrayEquals(new float[]{9f, 0f, 9f}, path.get(path.size() - 1), 0.001f);
    }

    @Test
    public void returnsEmptyPathWhenNoPolygonIsFound() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        List<float[]> path = nav.find(new float[]{100f, 0f, 100f}, new float[]{9f, 0f, 9f}, new float[]{1f, 1f, 1f});

        assertTrue(path.isEmpty());
    }

    @Test
    public void raycastAndFindNearestUseLoadedMesh() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        float[] nearest = nav.findNearest(new float[]{5f, 2f, 5f}, new float[]{2f, 4f, 2f});
        float[] hit = nav.raycast(new float[]{1f, 0f, 1f}, new float[]{9f, 0f, 9f}, new float[]{2f, 4f, 2f});

        assertNotNull(nearest);
        assertNotNull(hit);
        assertArrayEquals(new float[]{5f, 0f, 5f}, nearest, 0.001f);
        assertTrue(hit[0] >= 0f && hit[0] <= 10f);
        assertTrue(hit[2] >= 0f && hit[2] <= 10f);
    }

    @Test
    public void supportsConcurrentFindQueries() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        runConcurrently(() -> {
            List<float[]> path = nav.find(new float[]{1f, 0f, 1f}, new float[]{9f, 0f, 9f}, new float[]{2f, 4f, 2f});
            assertFalse(path.isEmpty());
            assertArrayEquals(new float[]{1f, 0f, 1f}, path.get(0), 0.001f);
            assertArrayEquals(new float[]{9f, 0f, 9f}, path.get(path.size() - 1), 0.001f);
        });
    }

    @Test
    public void supportsConcurrentRaycastQueries() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        runConcurrently(() -> {
            float[] hit = nav.raycast(new float[]{1f, 0f, 1f}, new float[]{9f, 0f, 9f}, new float[]{2f, 4f, 2f});
            assertNotNull(hit);
            assertTrue(hit[0] >= 0f && hit[0] <= 10f);
            assertTrue(hit[2] >= 0f && hit[2] <= 10f);
        });
    }

    @Test
    public void supportsConcurrentFindNearestQueries() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        runConcurrently(() -> {
            float[] nearest = nav.findNearest(new float[]{5f, 2f, 5f}, new float[]{2f, 4f, 2f});
            assertNotNull(nearest);
            assertArrayEquals(new float[]{5f, 0f, 5f}, nearest, 0.001f);
        });
    }

    @Test
    public void reinitUsesNewMeshForThreadLocalQueries() throws Exception {
        File first = writeU3dFixture(10);
        File second = writeU3dFixture(20);
        Easy3dNav nav = new Easy3dNav();

        nav.init(first.getAbsolutePath());
        assertTrue(nav.find(new float[]{15f, 0f, 15f}, new float[]{19f, 0f, 19f}, new float[]{1f, 1f, 1f}).isEmpty());

        nav.init(second.getAbsolutePath());
        List<float[]> path = nav.find(new float[]{15f, 0f, 15f}, new float[]{19f, 0f, 19f}, new float[]{2f, 4f, 2f});

        assertFalse(path.isEmpty());
        assertArrayEquals(new float[]{15f, 0f, 15f}, path.get(0), 0.001f);
        assertArrayEquals(new float[]{19f, 0f, 19f}, path.get(path.size() - 1), 0.001f);
    }

    private File writeU3dFixture() throws Exception {
        return writeU3dFixture(10);
    }

    private File writeU3dFixture(int size) throws Exception {
        MeshData data = createFlatMeshData(size);
        NavMesh mesh = new NavMesh(data, 4, 0);
        ByteArrayOutputStream tile = new ByteArrayOutputStream();
        new MeshDataWriter().write(tile, data, ByteOrder.LITTLE_ENDIAN, false);
        byte[] tileData = tile.toByteArray();

        File file = File.createTempFile("easy3dnav-u3d-", ".navmesh");
        file.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(file)) {
            writeInt(out, 0x8802);
            writeInt(out, 1);
            writeFloat(out, mesh.getParams().orig[0]);
            writeFloat(out, mesh.getParams().orig[1]);
            writeFloat(out, mesh.getParams().orig[2]);
            writeFloat(out, mesh.getParams().tileWidth);
            writeFloat(out, mesh.getParams().tileHeight);
            writeInt(out, mesh.getParams().maxTiles);
            writeInt(out, mesh.getParams().maxPolys);
            writeInt(out, mesh.getMaxVertsPerPoly());
            writeInt(out, 1);
            writeInt(out, tileData.length);
            out.write(tileData);
        }
        return file;
    }

    private MeshData createFlatMeshData(int size) {
        NavMeshDataCreateParams params = new NavMeshDataCreateParams();
        params.verts = new int[]{0, 0, 0, size, 0, 0, size, 0, size, 0, 0, size};
        params.vertCount = 4;
        params.polys = new int[]{0, 1, 2, 3, 0x800f, 0x800f, 0x800f, 0x800f};
        params.polyFlags = new int[]{1};
        params.polyAreas = new int[]{0};
        params.polyCount = 1;
        params.nvp = 4;
        params.bmin = new float[]{0f, 0f, 0f};
        params.bmax = new float[]{size, 1f, size};
        params.walkableHeight = 2f;
        params.walkableRadius = 0.5f;
        params.walkableClimb = 0.5f;
        params.cs = 1f;
        params.ch = 1f;
        params.buildBvTree = true;
        return NavMeshBuilder.createNavMeshData(params);
    }

    private void writeInt(FileOutputStream out, int value) throws Exception {
        out.write(value & 0xff);
        out.write((value >>> 8) & 0xff);
        out.write((value >>> 16) & 0xff);
        out.write((value >>> 24) & 0xff);
    }

    private void writeFloat(FileOutputStream out, float value) throws Exception {
        writeInt(out, Float.floatToIntBits(value));
    }

    private void runConcurrently(CheckedRunnable action) throws Exception {
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    start.await();
                    for (int j = 0; j < 50; j++) {
                        action.run();
                    }
                    return null;
                }
            }));
        }
        start.countDown();
        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdownNow();
    }

    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
