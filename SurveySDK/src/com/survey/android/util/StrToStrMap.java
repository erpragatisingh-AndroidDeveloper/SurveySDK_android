package com.survey.android.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StrToStrMap {

	private List<String> keys;
	private List<String> values;

	public StrToStrMap() {
		keys = new LinkedList<String>();
		values = new LinkedList<String>();
	}
	
	public StrToStrMap(String[] keys_array, String[] values_array) throws Exception {
		
		if (keys_array.length != values_array.length) {
			throw new Exception(
					"Can't create map cause keys and values aren't same size: keys(+"
							+ keys_array.length + ") , values("
							+ values_array.length + ")");
		} 
		
		keys = new LinkedList<String>();
		values = new LinkedList<String>();
		for (int i = 0; i< keys_array.length;i++) {
			keys.add(keys_array[i]);
			values.add(values_array[i]);
		}
	}

	public String[] getItemsList() {
		return keys.toArray(new String[0]);
	}

	public String getValue(String key) {
		for (int i = 0; i < keys.size(); i++) {
			if (key.equals(keys.get(i)))
				return values.get(i);
		}
		return "";
	}

	public void insertEmpty() {
		keys.add(0, "");
		values.add(0, "");
	}

	public String getKey(String value) {
		for (int i = 0; i < values.size(); i++) {
			if (value.equals(values.get(i)))
				return keys.get(i);
		}
		return "";
	}

	public void addItem(String key, String val) {
		this.keys.add(key);
		this.values.add(val);
	}
	
	public void addItemAtIndex(int index, String key, String value) {
		keys.add(index, key);
		keys.add(index, value);
	}

	public boolean isNotEmpty() {
		return keys != null;
	}
	
	public void removeItem(String key) {
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i).equals(key)) {
				keys.remove(i);
				values.remove(i);
				return;
			}
		}
	}
	
	/**
	 * Sort keys.
	 */
	public void sortOnlyKeys() {
		String[] nonSortedKeys = keys.toArray(new String[0]);
		Arrays.sort(nonSortedKeys);
	}

	
}
