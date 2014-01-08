package com.survey.android.util;

import com.survey.android.util.Log;


public class Log {
	public final static String LOGTAG = "Survey";
	public static final boolean debug = false;

	private static String desc() {
		try {
			StringBuilder builder = new StringBuilder("[");
			builder.append(Thread.currentThread().getStackTrace()[4].getFileName().replaceFirst("\\..*", ""));
			builder.append(".");
			builder.append(Thread.currentThread().getStackTrace()[4].getMethodName());
			builder.append("():");
			builder.append(Thread.currentThread().getStackTrace()[4].getLineNumber());
			builder.append("] ");
		    return builder.toString();
		} catch (Exception e) {
			Log.w(LOGTAG, "Can't log description! Exception: " + e);
		}
		return "";
	}

	public static void d(String logMe) {
		if (debug) {
			android.util.Log.d(LOGTAG, desc() + logMe);
		}
	}
	
	public static void d(String tag, String logMe) {
		if (debug) {
			android.util.Log.d(tag, desc() + logMe);
		}
	}

	public static void e(String logMe) {
		android.util.Log.e(LOGTAG, desc() + logMe);
	}

	public static void e(String logMe, Exception ex) {
		android.util.Log.e(LOGTAG, desc() + logMe, ex);
	}
	
	public static void e(String tag, String logMe) {
		android.util.Log.e(tag, desc() + logMe);
	}

	public static void e(String tag, String logMe, Exception ex) {
		android.util.Log.e(tag, desc() + logMe, ex);
	}

	public static void i(String logMe) {
		if (debug) {
			android.util.Log.i(LOGTAG, desc() + logMe);
		}
	}
	
	public static void i(String tag, String logMe) {
		if (debug) {
			android.util.Log.i(tag, desc() + logMe);
		}
	}

	public static void v(String logMe) {
		if (debug) {
			android.util.Log.v(LOGTAG, desc() + logMe);
		}
	}

	public static void w(String logMe) {
		android.util.Log.w(LOGTAG, desc() + logMe);
	}

	public static void w(String tag, String logMe) {
		android.util.Log.w(tag, desc() + logMe);
	}
}
