import com.github.silencesu.Easy3dNav.Easy3dNav;
import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavPathApi;
import com.github.silencesu.Easy3dNav.api.NavQueryApi;
import com.github.silencesu.Easy3dNav.api.NavRandomApi;
import com.github.silencesu.Easy3dNav.api.NavWallHit;
import org.junit.Test;
import org.recast4j.detour.MeshData;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshBuilder;
import org.recast4j.detour.NavMeshDataCreateParams;
import org.recast4j.detour.io.MeshDataWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    public void loadsU3dMeshFromFileAndFindsPath() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh);

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
    public void queryApiReportsWalkableAndNotWalkable() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        assertTrue(nav.query().isWalkable(new Vector3f(5f, 0f, 5f), new Vector3f(2f, 4f, 2f)));
        assertFalse(nav.query().isWalkable(new Vector3f(100f, 0f, 100f), new Vector3f(1f, 1f, 1f)));
    }

    @Test
    public void queryApiReturnsClosestPointOrNull() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        Vector3f closest = nav.query().closestPoint(new Vector3f(5f, 2f, 5f), new Vector3f(2f, 4f, 2f));
        Vector3f missing = nav.query().closestPoint(new Vector3f(100f, 0f, 100f), new Vector3f(1f, 1f, 1f));

        assertVectorEquals(new Vector3f(5f, 0f, 5f), closest);
        assertNull(missing);
    }

    @Test
    public void queryApiReturnsHeightOrNull() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        Float height = nav.query().getHeight(new Vector3f(5f, 2f, 5f), new Vector3f(2f, 4f, 2f));
        Float missing = nav.query().getHeight(new Vector3f(100f, 0f, 100f), new Vector3f(1f, 1f, 1f));

        assertNotNull(height);
        assertEquals(0f, height.floatValue(), 0.001f);
        assertNull(missing);
    }

    @Test
    public void pathApiReportsReachableAndUnreachable() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());

        assertTrue(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(9f, 0f, 9f),
                new Vector3f(2f, 4f, 2f)));
        assertFalse(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(100f, 0f, 100f),
                new Vector3f(1f, 1f, 1f)));
    }

    @Test
    public void groupedApisSupportVector3fAndDefaultExtents() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.setExtents(new float[]{2f, 4f, 2f});
        nav.init(navmesh.getAbsolutePath());

        Vector3f point = new Vector3f(5f, 2f, 5f);
        Vector3f extents = new Vector3f(2f, 4f, 2f);
        Vector3f closest = nav.query().closestPoint(point, extents);
        Float height = nav.query().getHeight(point, extents);

        assertTrue(nav.query().isWalkable(point));
        assertNotNull(closest);
        assertEquals(5f, closest.getX(), 0.001f);
        assertEquals(0f, closest.getY(), 0.001f);
        assertEquals(5f, closest.getZ(), 0.001f);
        assertNotNull(height);
        assertEquals(0f, height.floatValue(), 0.001f);
        assertTrue(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(9f, 0f, 9f)));
        assertFalse(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(100f, 0f, 100f),
                new Vector3f(1f, 1f, 1f)));
    }

    @Test
    public void groupedApiInterfacesDoNotExposeFloatArrays() {
        assertNoFloatArrayInPublicMethods(NavQueryApi.class);
        assertNoFloatArrayInPublicMethods(NavPathApi.class);
        assertNoFloatArrayInPublicMethods(NavRandomApi.class);
        assertNoFloatArrayInPublicMethods(NavWallHit.class);
        assertNoRecast4jTypesInPublicMethods(NavQueryApi.class);
        assertNoRecast4jTypesInPublicMethods(NavPathApi.class);
        assertNoRecast4jTypesInPublicMethods(NavRandomApi.class);
        assertNoRecast4jTypesInPublicMethods(NavWallHit.class);
    }

    @Test
    public void randomApiReturnsPointOrNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());
        Easy3dNav empty = new Easy3dNav();
        empty.init(writeEmptyU3dFixture().getAbsolutePath());

        Vector3f point = nav.random().point();

        assertVectorInFlatMesh(point, 10f);
        assertNull(empty.random().point());
    }

    @Test
    public void randomApiReturnsPointAroundOrNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        Vector3f point = nav.random().pointAround(new Vector3f(5f, 0f, 5f), 3f);
        Vector3f missing = nav.random().pointAround(new Vector3f(100f, 0f, 100f), 3f);

        assertVectorInFlatMesh(point, 10f);
        assertNull(missing);
    }

    @Test
    public void randomApiReturnsPointWithinOrNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        Vector3f point = nav.random().pointWithin(new Vector3f(5f, 0f, 5f), 3f);
        Vector3f invalidRadius = nav.random().pointWithin(new Vector3f(5f, 0f, 5f), 0f);
        Vector3f missing = nav.random().pointWithin(new Vector3f(100f, 0f, 100f), 3f);

        assertVectorInFlatMesh(point, 10f);
        assertNull(invalidRadius);
        assertNull(missing);
    }

    @Test
    public void queryApiReportsWalkableAround() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        assertTrue(nav.query().hasWalkableAround(new Vector3f(5f, 0f, 5f), 3f));
        assertFalse(nav.query().hasWalkableAround(new Vector3f(5f, 0f, 5f), 0f));
        assertFalse(nav.query().hasWalkableAround(new Vector3f(100f, 0f, 100f), 3f));
    }

    @Test
    public void queryApiReturnsDistanceToWallOrNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        Float distance = nav.query().distanceToWall(new Vector3f(5f, 0f, 5f), 10f);
        Float invalidRadius = nav.query().distanceToWall(new Vector3f(5f, 0f, 5f), 0f);
        Float missing = nav.query().distanceToWall(new Vector3f(100f, 0f, 100f), 10f);

        assertNotNull(distance);
        assertTrue(distance >= 0f);
        assertTrue(distance <= 5.1f);
        assertNull(invalidRadius);
        assertNull(missing);
    }

    @Test
    public void queryApiReturnsNearestWallHitOrNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        NavWallHit hit = nav.query().nearestWall(new Vector3f(5f, 0f, 5f), 10f);
        NavWallHit invalidRadius = nav.query().nearestWall(new Vector3f(5f, 0f, 5f), 0f);

        assertNotNull(hit);
        assertNotNull(hit.getDistance());
        assertNotNull(hit.getPosition());
        assertNotNull(hit.getNormal());
        assertTrue(hit.getDistance() >= 0f);
        assertTrue(hit.getPosition().getX() >= 0f && hit.getPosition().getX() <= 10f);
        assertTrue(hit.getPosition().getZ() >= 0f && hit.getPosition().getZ() <= 10f);
        assertNull(invalidRadius);
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

    private File writeEmptyU3dFixture() throws Exception {
        File file = File.createTempFile("easy3dnav-empty-u3d-", ".navmesh");
        file.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(file)) {
            writeInt(out, 0x8802);
            writeInt(out, 0);
            writeFloat(out, 0f);
            writeFloat(out, 0f);
            writeFloat(out, 0f);
            writeFloat(out, 10f);
            writeFloat(out, 10f);
            writeInt(out, 1);
            writeInt(out, 1);
            writeInt(out, 4);
        }
        return file;
    }

    private MeshData createFlatMeshData(int size) {
        NavMeshDataCreateParams params = new NavMeshDataCreateParams();
        params.verts = new int[]{0, 0, 0, size, 0, 0, size, 0, size, 0, 0, size};
        params.vertCount = 4;
        params.polys = new int[]{0, 3, 2, 1, 0x800f, 0x800f, 0x800f, 0x800f};
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

    private void assertVectorEquals(Vector3f expected, Vector3f actual) {
        assertNotNull(actual);
        assertEquals(expected.getX(), actual.getX(), 0.001f);
        assertEquals(expected.getY(), actual.getY(), 0.001f);
        assertEquals(expected.getZ(), actual.getZ(), 0.001f);
    }

    private void assertNoFloatArrayInPublicMethods(Class<?> apiClass) {
        for (Method method : apiClass.getMethods()) {
            assertFalse(method.getName() + " should not return float[]",
                    method.getReturnType().isArray() && method.getReturnType().getComponentType() == float.class);
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertFalse(method.getName() + " should not accept float[]",
                        parameterType.isArray() && parameterType.getComponentType() == float.class);
            }
        }
    }

    private void assertNoRecast4jTypesInPublicMethods(Class<?> apiClass) {
        for (Method method : apiClass.getMethods()) {
            assertFalse(method.getName() + " should not return recast4j type", isRecast4jType(method.getReturnType()));
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertFalse(method.getName() + " should not accept recast4j type", isRecast4jType(parameterType));
            }
        }
    }

    private boolean isRecast4jType(Class<?> type) {
        Package typePackage = type.getPackage();
        return typePackage != null && typePackage.getName().startsWith("org.recast4j");
    }

    private void assertVectorInFlatMesh(Vector3f point, float size) {
        assertNotNull(point);
        assertTrue(point.getX() >= 0f && point.getX() <= size);
        assertEquals(0f, point.getY(), 0.001f);
        assertTrue(point.getZ() >= 0f && point.getZ() <= size);
    }
}
