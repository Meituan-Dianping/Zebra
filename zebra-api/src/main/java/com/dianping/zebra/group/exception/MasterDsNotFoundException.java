package com.dianping.zebra.group.exception;

import com.dianping.zebra.exception.ZebraException;

public class MasterDsNotFoundException extends ZebraException {

	private static final long serialVersionUID = -1726616148252641312L;

	public MasterDsNotFoundException() {
		super();
	}

	public MasterDsNotFoundException(String message) {
		super(message);
	}

	public MasterDsNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public MasterDsNotFoundException(Throwable cause) {
		super(cause);
	}
}
