package com.dianping.zebra.group.router;

public class ZebraForceMasterHelper {

	/**
	 * 通过使用piegon的context来设置走主库，该方式将会透传到后端应用，使后端应用同样走主库，使用该方式请慎用。
	 * 因为使用piegon的context,所以piegon会自动的对该context进行清理，故调用完该方法后，无需进行清理动作。
	 * forceMasterInPiegonContext比forceMasterInLocalContext有更高的优先级。
	 */
	public static void forceMasterInPiegonContext() {
		DpdlReadWriteStrategy.setReadFromMaster();
	}

	/**
	 * 使用本地的context来设置走主库，该方式只会影响本应用内本次请求的所有sql走主库，不会影响到piegon后端服务。
	 * 调用过该方法后，一定要在请求的末尾调用clearLocalContext进行清理操作。
	 * 优先级比forceMasterInPiegonContext低。
	 */
	public static void forceMasterInLocalContext() {
		LocalContextReadWriteStrategy.setReadFromMaster();
	}

	/**
	 * 配合forceMasterInLocalContext进行使用，在请求的末尾调用该方法，对LocalContext进行清理。
	 */
	public static void clearLocalContext() {
		LocalContextReadWriteStrategy.clearContext();
	}
}
