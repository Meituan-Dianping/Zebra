/**
 * Project: zebra-client
 *
 * File Created at Feb 25, 2014
 *
 */
package com.dianping.zebra.group.jdbc.param;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Leo Liang
 */
public abstract class ParamContext implements Serializable {
	private static final long serialVersionUID = -743208754173084268L;

	protected int index;

	protected Object[] values;

	public ParamContext(int index, Object[] values) {
		this.index = index;
		if (values != null && values.length != 0) {
			this.values = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				this.values[i] = values[i];
			}
		}
	}

	/**
	 * @return the update
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 *           the update to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return the values
	 */
	public Object[] getValues() {
		return values;
	}

	/**
	 * @param values
	 *           the values to set
	 */
	public void setValues(Object[] values) {
		if (values != null && values.length != 0) {
			this.values = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				this.values[i] = values[i];
			}
		}
	}

	public abstract void setParam(PreparedStatement stmt) throws SQLException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return formatToString(values);
	}

	public String formatToString(Object[] a) {
		if (a == null) {
			return "null";
		}

		int iMax = a.length - 1;
		if (iMax == -1) {
			return "null";
		}

		if (a.length == 1) {
			return String.valueOf(a[0]);
		} else {
			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(a[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}
}
