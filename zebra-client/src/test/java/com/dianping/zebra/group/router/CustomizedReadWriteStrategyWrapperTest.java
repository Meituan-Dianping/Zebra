package com.dianping.zebra.group.router;

import com.dianping.zebra.group.config.datasource.entity.GroupDataSourceConfig;
import org.junit.*;

public class CustomizedReadWriteStrategyWrapperTest {
	@Test
	public void test_wrapper_true(){
		ReadWriteStrategyWrapper wrapper = new ReadWriteStrategyWrapper();
		wrapper.addStrategy(new ReadWriteStrategy() {
			@Override
			public boolean shouldReadFromMaster() {
				return false;
			}

			@Override public void setGroupDataSourceConfig(GroupDataSourceConfig config) {

			}
		});
		
		wrapper.addStrategy(new ReadWriteStrategy() {
			@Override
			public boolean shouldReadFromMaster() {
				return true;
			}

			@Override public void setGroupDataSourceConfig(GroupDataSourceConfig config) {

			}
		});
		
		Assert.assertTrue(wrapper.shouldReadFromMaster());
	}
	
	@Test
	public void test_wrapper_false(){
		ReadWriteStrategyWrapper wrapper = new ReadWriteStrategyWrapper();
		wrapper.addStrategy(new ReadWriteStrategy() {
			@Override
			public boolean shouldReadFromMaster() {
				return false;
			}

			@Override public void setGroupDataSourceConfig(GroupDataSourceConfig config) {

			}
		});
		
		wrapper.addStrategy(new ReadWriteStrategy() {
			@Override
			public boolean shouldReadFromMaster() {
				return false;
			}

			@Override public void setGroupDataSourceConfig(GroupDataSourceConfig config) {

			}
		});
		
		Assert.assertTrue(!wrapper.shouldReadFromMaster());
	}
}
