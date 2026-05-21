import com.github.silencesu.Easy3dNav.Easy3dNav;
import com.github.silencesu.Easy3dNav.Vector3f;
import com.github.silencesu.Easy3dNav.api.NavDebugApi;
import com.github.silencesu.Easy3dNav.api.NavLocalPolygon;
import com.github.silencesu.Easy3dNav.api.NavMeshInfo;
import com.github.silencesu.Easy3dNav.api.NavPathApi;
import com.github.silencesu.Easy3dNav.api.NavOptions;
import com.github.silencesu.Easy3dNav.api.NavQueryApi;
import com.github.silencesu.Easy3dNav.api.NavRandomApi;
import com.github.silencesu.Easy3dNav.api.NavWallHit;
import com.github.silencesu.Easy3dNav.api.NavWallSegment;
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
        NavOptions validOptions = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();
        NavOptions smallOptions = NavOptions.builder().extents(new Vector3f(1f, 1f, 1f)).build();

        assertTrue(nav.query().isWalkable(new Vector3f(5f, 0f, 5f), validOptions));
        assertFalse(nav.query().isWalkable(new Vector3f(100f, 0f, 100f), smallOptions));
    }

    @Test
    public void queryApiReturnsClosestPointOrNull() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());
        NavOptions validOptions = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();
        NavOptions smallOptions = NavOptions.builder().extents(new Vector3f(1f, 1f, 1f)).build();

        Vector3f closest = nav.query().closestPoint(new Vector3f(5f, 2f, 5f), validOptions);
        Vector3f missing = nav.query().closestPoint(new Vector3f(100f, 0f, 100f), smallOptions);

        assertVectorEquals(new Vector3f(5f, 0f, 5f), closest);
        assertNull(missing);
    }

    @Test
    public void queryApiReturnsHeightOrNull() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());
        NavOptions validOptions = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();
        NavOptions smallOptions = NavOptions.builder().extents(new Vector3f(1f, 1f, 1f)).build();

        Float height = nav.query().getHeight(new Vector3f(5f, 2f, 5f), validOptions);
        Float missing = nav.query().getHeight(new Vector3f(100f, 0f, 100f), smallOptions);

        assertNotNull(height);
        assertEquals(0f, height.floatValue(), 0.001f);
        assertNull(missing);
    }

    @Test
    public void pathApiReportsReachableAndUnreachable() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.init(navmesh.getAbsolutePath());
        NavOptions validOptions = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();
        NavOptions smallOptions = NavOptions.builder().extents(new Vector3f(1f, 1f, 1f)).build();

        assertTrue(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(9f, 0f, 9f),
                validOptions));
        assertFalse(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(100f, 0f, 100f),
                smallOptions));
    }

    @Test
    public void pathApiFindsPathWithDefaultOptions() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        List<Vector3f> path = nav.path().find(new Vector3f(1f, 0f, 1f), new Vector3f(9f, 0f, 9f));

        assertFalse(path.isEmpty());
        assertVectorEquals(new Vector3f(1f, 0f, 1f), path.get(0));
        assertVectorEquals(new Vector3f(9f, 0f, 9f), path.get(path.size() - 1));
    }

    @Test
    public void pathApiFindReturnsEmptyForNullEndpoints() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        assertTrue(nav.path().find(null, new Vector3f(9f, 0f, 9f)).isEmpty());
        assertTrue(nav.path().find(new Vector3f(1f, 0f, 1f), null).isEmpty());
    }

    @Test
    public void legacyFindReturnsEmptyForNullVectorEndpoint() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        assertTrue(nav.find(null, new Vector3f(9f, 0f, 9f)).isEmpty());
        assertTrue(nav.find(new Vector3f(1f, 0f, 1f), null).isEmpty());
    }

    @Test
    public void pathApiUsesNavOptionsForExtentsAndPathLimits() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.setExtents(new float[]{0.1f, 0.1f, 0.1f});
        nav.init(writeU3dFixture().getAbsolutePath());
        Vector3f start = new Vector3f(-1f, 0f, 1f);
        Vector3f end = new Vector3f(9f, 0f, 9f);

        NavOptions options = NavOptions.builder()
                .extents(new Vector3f(2f, 4f, 2f))
                .maxStraightPath(1)
                .straightPathOptions(NavOptions.STRAIGHT_PATH_ALL_CROSSINGS)
                .anyAngle(true)
                .raycastLimit(-1f)
                .build();
        List<Vector3f> path = nav.path().find(start, end, options);

        assertEquals(1, path.size());
        assertVectorInFlatMesh(path.get(0), 10f);
        assertTrue(nav.path().isReachable(start, end, options));
        assertFalse(nav.path().isReachable(start, end));
    }

    @Test
    public void pathApiMovesAlongSurface() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        Vector3f moved = nav.path().moveAlongSurface(new Vector3f(1f, 0f, 1f), new Vector3f(9f, 0f, 9f));

        assertVectorEquals(new Vector3f(9f, 0f, 9f), moved);
    }

    @Test
    public void pathApiMoveAlongSurfaceReturnsNullWhenStartIsInvalid() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        Vector3f moved = nav.path().moveAlongSurface(new Vector3f(100f, 0f, 100f), new Vector3f(9f, 0f, 9f));

        assertNull(moved);
    }

    @Test
    public void pathApiMoveAlongSurfaceUsesNavOptionsExtents() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.setExtents(new float[]{0.1f, 0.1f, 0.1f});
        nav.init(writeU3dFixture().getAbsolutePath());
        Vector3f start = new Vector3f(-1f, 0f, 1f);
        Vector3f end = new Vector3f(2f, 0f, 1f);
        NavOptions options = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();

        Vector3f moved = nav.path().moveAlongSurface(start, end, options);

        assertVectorEquals(end, moved);
        assertNull(nav.path().moveAlongSurface(start, end));
    }

    @Test
    public void navOptionsDefensivelyCopiesExtents() {
        Vector3f extents = new Vector3f(2f, 4f, 2f);
        NavOptions options = NavOptions.builder().extents(extents).build();
        extents.setX(99f);
        Vector3f returned = options.getExtents();
        returned.setY(99f);

        assertEquals(2f, options.getExtents().getX(), 0.001f);
        assertEquals(4f, options.getExtents().getY(), 0.001f);
        assertTrue(options.hasExtents());
    }

    @Test
    public void groupedApisSupportVector3fAndDefaultExtents() throws Exception {
        File navmesh = writeU3dFixture();
        Easy3dNav nav = new Easy3dNav();
        nav.setExtents(new float[]{2f, 4f, 2f});
        nav.init(navmesh.getAbsolutePath());

        Vector3f point = new Vector3f(5f, 2f, 5f);
        NavOptions options = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();
        Vector3f closest = nav.query().closestPoint(point, options);
        Float height = nav.query().getHeight(point, options);

        assertTrue(nav.query().isWalkable(point));
        assertNotNull(closest);
        assertEquals(5f, closest.getX(), 0.001f);
        assertEquals(0f, closest.getY(), 0.001f);
        assertEquals(5f, closest.getZ(), 0.001f);
        assertNotNull(height);
        assertEquals(0f, height.floatValue(), 0.001f);
        assertTrue(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(9f, 0f, 9f)));
        assertFalse(nav.path().isReachable(new Vector3f(1f, 0f, 1f), new Vector3f(100f, 0f, 100f),
                NavOptions.builder().extents(new Vector3f(1f, 1f, 1f)).build()));
    }

    @Test
    public void groupedApiInterfacesDoNotExposeFloatArrays() {
        assertNoFloatArrayInPublicMethods(NavOptions.class);
        assertNoFloatArrayInPublicMethods(NavOptions.Builder.class);
        assertNoFloatArrayInPublicMethods(NavQueryApi.class);
        assertNoFloatArrayInPublicMethods(NavPathApi.class);
        assertNoFloatArrayInPublicMethods(NavRandomApi.class);
        assertNoFloatArrayInPublicMethods(NavWallHit.class);
        assertNoFloatArrayInPublicMethods(NavDebugApi.class);
        assertNoFloatArrayInPublicMethods(NavWallSegment.class);
        assertNoFloatArrayInPublicMethods(NavLocalPolygon.class);
        assertNoFloatArrayInPublicMethods(NavMeshInfo.class);
        assertNoRecast4jTypesInPublicMethods(NavOptions.class);
        assertNoRecast4jTypesInPublicMethods(NavOptions.Builder.class);
        assertNoRecast4jTypesInPublicMethods(NavQueryApi.class);
        assertNoRecast4jTypesInPublicMethods(NavPathApi.class);
        assertNoRecast4jTypesInPublicMethods(NavRandomApi.class);
        assertNoRecast4jTypesInPublicMethods(NavWallHit.class);
        assertNoRecast4jTypesInPublicMethods(NavDebugApi.class);
        assertNoRecast4jTypesInPublicMethods(NavWallSegment.class);
        assertNoRecast4jTypesInPublicMethods(NavLocalPolygon.class);
        assertNoRecast4jTypesInPublicMethods(NavMeshInfo.class);
        assertNoPublicMethod(NavDebugApi.class, "printMeshInfo");
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
    public void queryApiReturnsLocalNeighbourhoodPolygons() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        List<NavLocalPolygon> polygons = nav.query().findLocalNeighbourhood(new Vector3f(5f, 0f, 5f), 3f);

        assertEquals(1, polygons.size());
        assertLocalPolygonInFlatMesh(polygons.get(0), 10f);
    }

    @Test
    public void queryApiReturnsEmptyLocalNeighbourhoodForInvalidInputs() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        assertTrue(nav.query().findLocalNeighbourhood(null, 3f).isEmpty());
        assertTrue(nav.query().findLocalNeighbourhood(new Vector3f(5f, 0f, 5f), 0f).isEmpty());
        assertTrue(nav.query().findLocalNeighbourhood(new Vector3f(100f, 0f, 100f), 3f).isEmpty());
    }

    @Test
    public void queryApiFindLocalNeighbourhoodUsesNavOptionsExtents() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.setExtents(new float[]{0.1f, 0.1f, 0.1f});
        nav.init(writeU3dFixture().getAbsolutePath());
        Vector3f center = new Vector3f(-1f, 0f, 1f);
        NavOptions options = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();

        assertTrue(nav.query().findLocalNeighbourhood(center, 3f).isEmpty());
        List<NavLocalPolygon> polygons = nav.query().findLocalNeighbourhood(center, 3f, options);

        assertFalse(polygons.isEmpty());
        assertLocalPolygonInFlatMesh(polygons.get(0), 10f);
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
    public void groupedApisUseEmptySemanticsForInvalidInputs() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());
        Vector3f point = new Vector3f(5f, 0f, 5f);

        assertFalse(nav.query().isWalkable(null));
        assertNull(nav.query().closestPoint(null));
        assertNull(nav.query().getHeight(null));
        assertFalse(nav.path().isReachable(null, point));
        assertFalse(nav.path().isReachable(point, null));
        assertNull(nav.path().moveAlongSurface(null, point));
        assertNull(nav.path().moveAlongSurface(point, null));
        assertNull(nav.random().pointAround(null, 3f));
        assertNull(nav.random().pointAround(point, 0f));
        assertNull(nav.random().pointWithin(null, 3f));
        assertNull(nav.random().pointWithin(point, 0f));
        assertFalse(nav.query().hasWalkableAround(null, 3f));
        assertFalse(nav.query().hasWalkableAround(point, 0f));
        assertTrue(nav.query().findLocalNeighbourhood(null, 3f).isEmpty());
        assertTrue(nav.query().findLocalNeighbourhood(point, 0f).isEmpty());
        assertNull(nav.query().distanceToWall(null, 10f));
        assertNull(nav.query().nearestWall(point, 0f));
        assertTrue(nav.debug().getWallSegments(null).isEmpty());
    }

    @Test
    public void legacyFindNearestReturnsNullWhenPointIsNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        assertNull(nav.findNearest((Vector3f) null));
    }

    @Test(expected = IllegalStateException.class)
    public void uninitializedQueryThrowsIllegalStateException() {
        Easy3dNav nav = new Easy3dNav();

        nav.query().isWalkable(new Vector3f(1f, 0f, 1f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void legacyRaycastThrowsWhenStartIsInvalid() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        nav.raycast(new Vector3f(100f, 0f, 100f), new Vector3f(9f, 0f, 9f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void legacyRaycastThrowsWhenPointIsNull() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        nav.raycast(null, new Vector3f(9f, 0f, 9f));
    }

    @Test
    public void debugApiReturnsWallSegments() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        List<NavWallSegment> segments = nav.debug().getWallSegments(new Vector3f(5f, 0f, 5f));

        assertEquals(4, segments.size());
        for (NavWallSegment segment : segments) {
            assertSegmentInFlatMesh(segment, 10f);
        }
    }

    @Test
    public void debugApiReturnsEmptyWallSegmentsWhenPointIsInvalid() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        List<NavWallSegment> missing = nav.debug().getWallSegments(new Vector3f(100f, 0f, 100f));
        List<NavWallSegment> nullPoint = nav.debug().getWallSegments(null);

        assertTrue(missing.isEmpty());
        assertTrue(nullPoint.isEmpty());
    }

    @Test
    public void debugApiUsesNavOptionsExtentsForWallSegments() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.setExtents(new float[]{0.1f, 0.1f, 0.1f});
        nav.init(writeU3dFixture().getAbsolutePath());
        Vector3f point = new Vector3f(-1f, 0f, 1f);
        NavOptions options = NavOptions.builder().extents(new Vector3f(2f, 4f, 2f)).build();

        assertTrue(nav.debug().getWallSegments(point).isEmpty());
        assertFalse(nav.debug().getWallSegments(point, options).isEmpty());
    }

    @Test
    public void debugApiReturnsMeshInfo() throws Exception {
        Easy3dNav nav = new Easy3dNav();
        nav.init(writeU3dFixture().getAbsolutePath());

        NavMeshInfo info = nav.debug().meshInfo();

        assertEquals(1, info.getLoadedTileCount());
        assertEquals(1, info.getMaxTileCount());
        assertEquals(1, info.getPolygonCount());
        assertEquals(4, info.getVertexCount());
        assertEquals(2, info.getDetailTriangleCount());
        assertEquals(1, info.getBvTreeNodeCount());
        assertVectorEquals(new Vector3f(0f, 0f, 0f), info.getBoundsMin());
        assertVectorEquals(new Vector3f(10f, 1f, 10f), info.getBoundsMax());
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

    private void assertNoPublicMethod(Class<?> apiClass, String methodName) {
        for (Method method : apiClass.getMethods()) {
            assertFalse(methodName + " should not be exposed", methodName.equals(method.getName()));
        }
    }

    private void assertVectorInFlatMesh(Vector3f point, float size) {
        assertNotNull(point);
        assertTrue(point.getX() >= 0f && point.getX() <= size);
        assertEquals(0f, point.getY(), 0.001f);
        assertTrue(point.getZ() >= 0f && point.getZ() <= size);
    }

    private void assertSegmentInFlatMesh(NavWallSegment segment, float size) {
        assertNotNull(segment);
        assertVectorInFlatMesh(segment.getStart(), size);
        assertVectorInFlatMesh(segment.getEnd(), size);
    }

    private void assertLocalPolygonInFlatMesh(NavLocalPolygon polygon, float size) {
        assertNotNull(polygon);
        assertVectorInFlatMesh(polygon.getCenter(), size);
        assertEquals(4, polygon.getVertices().size());
        for (Vector3f vertex : polygon.getVertices()) {
            assertVectorInFlatMesh(vertex, size);
        }
        assertVectorEquals(new Vector3f(0f, 0f, 0f), polygon.getBoundsMin());
        assertVectorEquals(new Vector3f(size, 0f, size), polygon.getBoundsMax());
    }
}
