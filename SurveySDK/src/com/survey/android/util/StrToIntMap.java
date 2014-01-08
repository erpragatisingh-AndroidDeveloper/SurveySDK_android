package com.survey.android.util;

import java.util.LinkedList;
import java.util.List;

public class StrToIntMap {
	private List<String> keys;
	private List<Integer> values;

	public StrToIntMap() {
		keys = new LinkedList<String>();
		values = new LinkedList<Integer>();
	}

	public String[] getItemsList() {
		return keys.toArray(new String[0]);
	}

	public int getValue(String key) {
		for (int i = 0; i < keys.size(); i++) {
			if (key.equals(keys.get(i)))
				return values.get(i);
		}
		return -1;
	}

	public String getKey(int value) {
		for (int i = 0; i < values.size(); i++) {
			if (value == values.get(i))
				return keys.get(i);
		}
		return "";
	}

	public void addItem(String key, Integer val) {
		this.keys.add(key);
		this.values.add(val);
	}

	public boolean isNotEmpty() {
		return keys != null;
	}
}
