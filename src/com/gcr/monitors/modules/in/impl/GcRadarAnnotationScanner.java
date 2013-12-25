/*This file is part of gcRadar.

gcRadar is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation version 3 of the License.

gcRadar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with gcRadar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gcr.monitors.modules.in.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.gcr.structs.FieldNameValuePair;
import com.gcr.structs.annotation.GcRadarAnnotationScannerInterface;
import com.gcr.structs.annotation.GcRadarNotToInclude;
import com.gcr.structs.annotation.GcRadarToInclude;

/**
 * The Class GcRadarAnnotationScanner is used by the {@link TreeInputModule} for
 * scanning classes to list eligible fields to be added to the monitoring by the
 * GcMonitor.
 * 
 * The class has package level implementation to prevent mistaken an unnecessary
 * instantiation.
 * 
 * @author R.daneel.olivaw
 * @since 0.2
 */
class GcRadarAnnotationScanner implements GcRadarAnnotationScannerInterface {

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
	@Override
	public <T> List<? extends FieldNameValuePair<? extends Object>> getAllFields(
			T object, Object key, boolean isOptimistic) {
		if (isOptimistic) {
			return optimisticGetAllFields(object, key);
		} else {
			return pessimisticGetAllFields(object, key);
		}
	}

	private <T> List<FieldNameValuePair<? super Object>> optimisticGetAllFields(
			T object, Object key) {
		List<FieldNameValuePair<? super Object>> fieldValues = new ArrayList<FieldNameValuePair<? super Object>>();
		Class<? extends Object> objectClass = object.getClass();

		// Get fields from the object class optimistic
		putFields(fieldValues, object, objectClass, key, true);

		Class<?> superclass = null;
		while ((superclass = objectClass.getSuperclass()) != null) {
			// We do not want to add the parent's fields if the class does not
			// extend any class.
			if (superclass != null && superclass != Object.class) {
				String superKey = key + "," + superclass.getName();
				// Get fields from the parent class
				putFields(fieldValues, object, superclass, superKey, true);

				// Now prepare to scan parent's parent
				objectClass = superclass;
			} else {
				// once object is reached then we exit the loop
				break;
			}
		}

		return fieldValues;
	}

	private <T> List<FieldNameValuePair<? super Object>> pessimisticGetAllFields(
			T object, Object key) {
		List<FieldNameValuePair<? super Object>> fieldValues = new ArrayList<FieldNameValuePair<? super Object>>();
		Class<? extends Object> objectClass = object.getClass();

		// Get fields from the object class pessimistic
		putFields(fieldValues, object, objectClass, key, false);

		Class<?> superclass = null;
		while ((superclass = objectClass.getSuperclass()) != null) {
			// We do not want to add the parent's fields if the class does not
			// extend any class.
			if (superclass != null && superclass != Object.class) {
				String superKey = key + "," + superclass.getName();
				// Get fields from the parent class
				putFields(fieldValues, object, superclass, superKey, false);

				// Now prepare to scan parent's parent
				objectClass = superclass;
			} else {
				// once object is reached then we exit the loop
				break;
			}
		}

		return fieldValues;
	}

	private void putFields(List<FieldNameValuePair<? super Object>> outputList,
			Object object, Class<?> objectClass, Object key, boolean optimistic) {

		if (isExclusionAnnotationPresent(objectClass)) {
			return;
		}
		// Class<? extends Object> objectClass = object.getClass();
		Field[] declaredFields = objectClass.getDeclaredFields();

		for (Field f : declaredFields) {

			// Do not include the field if the 'GcRadarNotToInclude' annotation
			// is present
			if (checkAnnotations(f, optimistic)) {
				Class<?> fieldClass = f.getType();

				// Do not add primitive types
				if (!fieldClass.isPrimitive()) {

					// For making private members accessible
					f.setAccessible(true);

					// For detecting and avoiding adding the non-static inner
					// class reference
					if (f.getName().indexOf("this$") == -1) {

						// Do not add static members
						if (!Modifier.isStatic(f.getModifiers())) {
							try {
								Object cast = fieldClass.cast(f.get(object));

								// Do not add 'null' variables
								if (cast != null) {
									outputList
											.add(new FieldNameValuePair<Object>(
													(key + "," + f.getName()),
													cast));
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private boolean checkAnnotations(Field f, boolean optimistic) {
		if (optimistic) {
			return !isExclusionAnnotationPresent(f);
		} else {
			return isInclusionAnnotationPresent(f);
		}
	}

	private boolean isInclusionAnnotationPresent(Field f) {
		return f.isAnnotationPresent(GcRadarToInclude.class);
	}

	private boolean isExclusionAnnotationPresent(Field f) {

		return f.isAnnotationPresent(GcRadarNotToInclude.class);
	}

	private boolean isExclusionAnnotationPresent(Class<?> c) {

		return c.isAnnotationPresent(GcRadarNotToInclude.class);
	}
}
