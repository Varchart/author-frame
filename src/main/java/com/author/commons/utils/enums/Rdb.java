package com.author.commons.utils.enums;

public enum Rdb {
	db0(0), db1(1), db2(2), db3(3), db4(4), db5(5), db6(6), db7(7), db8(8), db9(9), db10(10);

	private final int c;

	Rdb(int c) {
		this.c = c;
	}

	public int c() {
		return c;
	}
}
