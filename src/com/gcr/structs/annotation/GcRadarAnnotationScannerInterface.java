package com.gcr.structs.annotation;

import java.util.List;

import com.gcr.structs.FieldNameValuePair;

public interface GcRadarAnnotationScannerInterface {

	/**
	 * Gets the all fields.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param object
	 *            the object to be scanned
	 * @param key
	 *            the key that will be used to derive the keys of the comprising
	 *            objects
	 * @param isOptimistic
	 *            the is optimistic traversal
	 * @return the all the eligible fields
	 */
	public <T> List<? extends FieldNameValuePair<? extends Object>> getAllFields(
			T object, Object key, boolean isOptimistic);
}
