package com.dianping.zebra.group.router;

public enum RouterType {

	// default routerType,
	@Deprecated
	ROUND_ROBIN("round-robin"),
	MASTER_SLAVE("master-slave"), 	// new version > 2.8.3

	@Deprecated
	LOAD_BALANCE("load-balance"), 	
	SLAVE_ONLY("slave-only"), 		// new version > 2.8.3

	@Deprecated
	FAIL_OVER("fail-over"), 		
	MASTER_ONLY("master-only"); 	// new version > 2.8.3

	public static RouterType getRouterType(String type) {
		if (type.equalsIgnoreCase("load-balance") || type.equalsIgnoreCase("slave-only")) {
			return SLAVE_ONLY;
		} else if (type.equalsIgnoreCase("master-only")) {
			return MASTER_ONLY;
		} else {
			return MASTER_SLAVE;
		}
	}

	private String type;

	private RouterType(String type) {
		this.type = type;
	}

	public String getRouterType() {
		return type;
	}
}
