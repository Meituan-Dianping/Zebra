package com.dianping.zebra.administrator.exception;

public class ZebraException extends Exception{

	private static final long serialVersionUID = -2179018904519319909L;

	public ZebraException(String message) {
      super(message);
   }

   public ZebraException(String message, Throwable e) {
		super(message, e);
	}
}
