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

package com.gcr.structs;

/**
 * The Class FieldNameValuePair is used by {@link GcRadarAnnotationScanner} for
 * recording the eligible fields. It is used to store name-value pairs of type
 * String-T.
 * 
 * @author R.daneel.olivaw
 * @since 0.2
 * 
 * @param <T>
 *            the generic type
 */
public class FieldNameValuePair<T> {

	private T refrenceValue;
	private String fieldName;
	private Class<? extends Object> valueClazz;

	/**
	 * Instantiates a new field name value pair.
	 * 
	 * @param fieldName
	 *            the field name
	 * @param refrenceValue
	 *            the reference value
	 */
	public FieldNameValuePair(String fieldName, T refrenceValue) {
		this.setFieldName(fieldName);
		this.setRefrenceValue(refrenceValue);
		this.setValueClazz(refrenceValue.getClass());
	}

	/**
	 * Gets the refrence value.
	 * 
	 * @return the refrence value
	 */
	public T getRefrenceValue() {
		return refrenceValue;
	}

	private void setRefrenceValue(T refrenceValue) {
		this.refrenceValue = refrenceValue;
	}

	/**
	 * Gets the field name.
	 * 
	 * @return the field name
	 */
	public String getFieldName() {
		return fieldName;
	}

	private void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * Gets the class of the referred object.
	 * 
	 * @return the value clazz
	 */
	public Class<? extends Object> getValueClazz() {
		return valueClazz;
	}

	private void setValueClazz(Class<? extends Object> class1) {
		this.valueClazz = class1;
	}

	// The toString() method should be removed as it can be used to steal
	// information
	// @Override
	// public String toString() {
	// return "FieldNameValuePair [refrenceValue=" + refrenceValue
	// + ", fieldName=" + fieldName + ", valueClazz=" + valueClazz
	// + "]";
	// }
}
