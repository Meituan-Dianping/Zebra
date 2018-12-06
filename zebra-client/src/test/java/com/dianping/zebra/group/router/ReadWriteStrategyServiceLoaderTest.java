package com.dianping.zebra.group.router;

import java.util.ServiceLoader;

import junit.framework.Assert;

import org.junit.Test;

public class ReadWriteStrategyServiceLoaderTest {

	@Test
	public void load_dpdl_readWriteStrategy(){
		ServiceLoader<ReadWriteStrategy> strategies = ServiceLoader.load(ReadWriteStrategy.class);

		int size = 0;
		if (strategies != null) {
			for (ReadWriteStrategy strategy : strategies) {
				size++;
				
				if(size == 1){
					Assert.assertEquals(true, strategy instanceof LocalContextReadWriteStrategy);
				}
			}
			
			Assert.assertEquals(1, size);
		}
	}
}
