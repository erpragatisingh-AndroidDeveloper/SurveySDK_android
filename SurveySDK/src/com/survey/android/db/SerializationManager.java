package com.survey.android.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.survey.android.containers.PollContainer;
import com.survey.android.model.AnswerModel;
//import dalvik.system.TemporaryDirectory;

public class SerializationManager {
	public static String TAG = "SERIALIZATION_MANAGER";

	/**
	 * Serializes object to byte array ( necessarily object needs to implement
	 * Serializable interface )
	 * 
	 * @param o
	 *            - object for serialization
	 * @return byte array - representation of object to byte array
	 */
	public static byte[] serializeObject(Object o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(o);
			out.close();
			byte[] buf = bos.toByteArray();
			return buf;
		} catch (IOException ioe) {
			Log.e("serializeObject", "error", ioe);
			return null;
		}
	}

	/**
	 * Deserializes byte array to instance of Object ( for further use needs to
	 * apply appropriate cast )
	 * 
	 * @param b
	 *            - byte array ( serialized object )
	 * @return deserialized instance of Object or null in a case of error
	 */
	public static Object deserializeObject(byte[] b) {
		try {
			ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(b));
			Object object = in.readObject();
			in.close();
			return object;
		} catch (ClassNotFoundException cnfe) {
			Log.e("deserializeObject", "class not found error", cnfe);
			return null;
		} catch (IOException ioe) {
			Log.e("deserializeObject", "io error", ioe);
			return null;
		}
	}

	// Specific for PollContainer class ( only instance of that class is going
	// to be serialized ever )
	// makes things easier but makes a little dirty scope of class

	/**
	 * Deserializes byte array to instance of PollContainer class ( uses
	 * additional parameter because Context is transient and has value of null
	 * when object is deserialized )
	 * 
	 * @param array
	 *            - byte array representation of object
	 * @param context
	 *            - set field from PollContainer to value context ( current
	 *            activity connected to pollContainer )
	 * @return instance of PollContainer class or null in a case of error
	 */
	public static PollContainer deserializePollContainer(byte[] array,
			Context context) {
		try {
			PollContainer tempContainer = (PollContainer) deserializeObject(array);
			if (tempContainer != null) {
				tempContainer.setContext(context);
			}
			// *************************************************************************************************
			List<AnswerModel> tempList = tempContainer.getAnswers();
			if (tempList != null) {
				Set<String> set = new HashSet<String>();
				for (AnswerModel a : tempList) {
					if (!a.getQuestionId().equals("token")) {
						set.add(a.getQuestionId());
					}
				}
				
				int tempCursor = tempContainer.getCursor();
				if (set.size() > 0 && tempCursor >= set.size()) {
					tempContainer.setCursor(set.size() - 1);
				}
				tempContainer.getSection().setType("restored");
			}
			// *************************************************************************************************
			return tempContainer;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
