package com.dianping.zebra.shard.router;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dianping.zebra.shard.exception.ShardParseException;
import com.dianping.zebra.shard.exception.ShardRouterException;
import com.dianping.zebra.shard.jdbc.DataSourceRepository;
import com.dianping.zebra.shard.jdbc.base.MockDataSource;
import com.dianping.zebra.shard.router.RouterResult.RouterTarget;
import com.dianping.zebra.shard.router.builder.XmlResourceRouterBuilder;

public class DataSourceRouterImplTest {
	private static ShardRouter router;

	private static Map<String, DataSource> createDataSourcePool() {
		Map<String, DataSource> dsPool = new HashMap<String, DataSource>();

		dsPool.put("Group_00", createMockDataSource("Group_00"));
		dsPool.put("Group_01", createMockDataSource("Group_01"));
		dsPool.put("Group_02", createMockDataSource("Group_02"));
		dsPool.put("Group_03", createMockDataSource("Group_03"));
		dsPool.put("Group_07", createMockDataSource("Group_07"));

		return dsPool;
	}

	private static DataSource createMockDataSource(String identity) {
		return new MockDataSource(identity);
	}

	@BeforeClass
	public static void setUp() {
		DataSourceRepository.getInstance().init(createDataSourcePool());
		RouterBuilder routerFactory = new XmlResourceRouterBuilder("db-router-rule.xml");
		router = routerFactory.build();
	}

	public void baseTest(String sql, List<Object> params) {
		RouterResult target = null;
		try {
			target = router.router(sql, params);
		} catch (ShardRouterException e) {
			e.printStackTrace();
		} catch (ShardParseException e) {
			e.printStackTrace();
		}
		assertNotNull(target);
		List<RouterTarget> targetedSqls = target.getSqls();
		printSql(targetedSqls);
		assertTrue(targetedSqls != null && !targetedSqls.isEmpty());
		assertTrue(!target.getParams().isEmpty());
	}

	private void printSql(List<RouterTarget> targetedSqls) {
		for (RouterTarget targetedSql : targetedSqls) {
			for (String sql : targetedSql.getSqls()) {
				System.out.println(String.format("[%s]  %s", targetedSql.getDatabaseName(), sql));
			}
		}
	}

	public void singleTargetTest(String sql, List<Object> params, String targetDs, String targetTable) {
		RouterResult target = null;
		try {
			target = router.router(sql, params);
		} catch (ShardRouterException e) {
			e.printStackTrace();
		} catch (ShardParseException e) {
			e.printStackTrace();
		}
		assertNotNull(target);
		List<RouterTarget> targetedSqls = target.getSqls();
		printSql(targetedSqls);
		assertTrue(targetedSqls != null && !targetedSqls.isEmpty() && targetedSqls.size() == 1);
		assertTrue(!target.getParams().isEmpty());

		RouterTarget targetedSql = targetedSqls.get(0);
		assertTrue(targetedSql.getDatabaseName().equalsIgnoreCase(targetDs)
		      && targetedSql.getSqls().get(0).contains(targetTable));
	}

	@Test
	public void testCase1() {
		String sql = "UPDATE DP_GroupFollowNote SET NoteClass = ? WHERE UserID = ?";
		List<Object> params = Arrays.asList((Object) 1, 200);

		baseTest(sql, params);
	}

	@Test
	public void testCase2() {
		String sql = "SELECT N.GroupID, F.FollowNoteID, F.UserID, F.NoteId "
		      + "FROM DP_GroupFollowNote F INNER JOIN DP_GroupNote N ON N.NoteID = F.NoteID "
		      + "WHERE F.UserID = ? AND F.NoteClass <> 3";
		List<Object> params = Arrays.asList((Object) 200);

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByUserId_0");
	}

	@Test
	public void testCase3() {
		String sql = "SELECT * FROM DP_GroupFollowNote " + "WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) "
		      + "AND NoteID = ? LIMIT ?, ?";
		List<Object> params = new ArrayList<Object>();
		params.add(3); // UserID
		params.add(25); // NoteID
		params.add(3); // Skip
		params.add(5); // Max

		singleTargetTest(sql, params, "Group_03", "DP_GroupFollowNote_ByNoteId_25");
	}

	@Test
	public void testCase4() {
		String sql = "SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ?";
		List<Object> params = Arrays.asList((Object) 200, 300);

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByNoteId_12");
	}

	@Test
	public void testCase5() {
		String sql = "SELECT * FROM DP_GroupFollowNote " + "WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) "
		      + "AND NoteID = ? AND UserID = ? " + "LIMIT ?, ?";
		// match white list of NodeID's rule
		List<Object> params = Arrays.asList((Object) 200, 100, 200, 3, 5);

		singleTargetTest(sql, params, "Group_00", "DP_GroupFollowNote_ByNoteId_100_103");
	}

	@Test
	public void testCase6() {
		String sql = "SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote "
		      + "WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ? " + "AND UserID = ?";
		List<Object> params = Arrays.asList((Object) 200, 1, 200);

		singleTargetTest(sql, params, "Group_07", "DP_GroupFollowNote_ByNoteId_1");
	}

	@Test
	public void testCase7() {
		String sql = "INSERT INTO DP_GroupFollowNote (NoteID, UserID, NoteClass, ADDTIME, UpdateTime, LastIP, DCashNumber) "
		      + "VALUES(?, ?, ?, ?, ?, ?, ?)";
		List<Object> params = Arrays.asList((Object) 200, 100, 3, new Date(), new Date(), "10.1.1.22", "223344422");

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByNoteId_8");
	}

	@Test
	public void testCase8() {
		String sql = "SELECT * FROM DP_GroupFollowNote WHERE FollowNoteID = ?";
		List<Object> params = Arrays.asList((Object) 200);

		baseTest(sql, params);
	}

	@Test
	public void testCase9() {
		String sql = "SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote "
		      + "WHERE (NoteClass = 1 OR (NoteClass = 4 AND UserID = ?)) AND NoteID = ? " + "AND FollowNoteID <= ?";
		List<Object> params = Arrays.asList((Object) 200, 100, 20);

		singleTargetTest(sql, params, "Group_00", "DP_GroupFollowNote_ByNoteId_100_103");
	}
	
	@Test
	public void testCase10() {
		String sql = "SELECT COUNT(DISTINCT(UserID)) FROM DP_GroupFollowNote WHERE NoteID = ?";
		List<Object> params = Arrays.asList((Object) 200);

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByNoteId_8");
	}

	@Test
	public void testCase11() {
		String sql = "SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote WHERE NoteID = ? AND UserID = ?";
		List<Object> params = Arrays.asList((Object) 200, 300);

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByNoteId_8");
	}

	@Test
	public void testCase12() {
		String sql = "SELECT * FROM DP_GroupFollowNote WHERE NoteID = ? AND NoteClass = 1 ORDER BY FollowNoteID DESC LIMIT 1";
		List<Object> params = Arrays.asList((Object) 200);

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByNoteId_8");
	}

	@Test
	public void testCase13() {
		String sql = "SELECT COUNT(FollowNoteID) FROM DP_GroupFollowNote F INNER JOIN DP_GroupNote N ON F.NoteID = N.NoteID AND N.GroupID = ? AND N.Status = 1 "
		      + "WHERE F.UserID = ? AND F.NoteClass = 1";
		List<Object> params = Arrays.asList((Object) 200, 300);

		singleTargetTest(sql, params, "Group_01", "DP_GroupFollowNote_ByUserId_4");
	}

	@Test
	public void testCase14() {
		String sql = "UPDATE DP_GroupFollowNote SET DCashNumber = DCashNumber + ? WHERE FollowNoteID = ?";
		List<Object> params = Arrays.asList((Object) 200, 300);

		baseTest(sql, params);
	}

	//@Test
	public void testCase15() {
		String sql = "SELECT DISTINCT(GN.NoteID) FROM DP_GroupNote GN INNER JOIN DP_Group G ON GN.GroupID = G.GroupID AND G.Status = 0 "
		      + "INNER JOIN DP_GroupFollowNote GFN ON GN.NoteID = GFN.NoteID "
		      + "WHERE (GN.Status = 1 OR (GN.Status = 3 AND GN.UserID = ?)) AND GN.UserID <> ? "
		      + "AND GFN.UserID = ? AND GNF.NoteClass = 1";
		List<Object> params = Arrays.asList((Object) 200, 300, 400);

		singleTargetTest(sql, params, "Group_02", "DP_GroupFollowNote_ByUserId_0");
	}

	@Test
	public void testCase16() {
		String sql = "UPDATE DP_GroupFollowNote SET UpdateTime = Now(), LastIP = ? WHERE FollowNoteID = ?";
		List<Object> params = Arrays.asList((Object) "10.1.1.22", 300);

		baseTest(sql, params);
	}

	@Test
	public void testCase17() {
		String sql = "UPDATE DP_GroupFollowNote SET NoteClass = ? WHERE  FollowNoteID = ?";
		List<Object> params = Arrays.asList((Object) 3, 300);

		baseTest(sql, params);
	}
	
	@Test
	public void testCase18() {
		String sql = "SELECT * FROM DP_GroupFollowNote WHERE NoteID in (?,?,?,?)";
		List<Object> params = Arrays.asList((Object) 1, 300,100,103);

		baseTest(sql, params);
	}
}