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

package com.gcr.monitors.modules.in.structs.annotation;

import java.util.List;

import com.gcr.structs.FieldNameValuePair;

public interface GcRadarAnnotationScannerInterface {

	/**
	 * Gets the fields from the object. <br>
	 * <br>
	 * <b>Implementation notes:</b> <br>
	 * The implementations need to return one objects of type
	 * {@link FieldNameValuePair} for each field to be added. Each object of
	 * {@link FieldNameValuePair} intern comprises of 2 objects,
	 * <ol>
	 * <li>Strong reference to the object referred by a field</li>
	 * <li>A String type value that will be used by the the monitor as an alias
	 * for the object</li>
	 * </ol>
	 * 
	 * The implementation needs to ensure the following,
	 * <ol>
	 * <li>The string alias for each of the fields to be monitored needs to be
	 * unique</li>
	 * <li>The implementation should not maintain any strong reference of the
	 * fields, as this will prevent reclamation by the Garbage collector and
	 * hence the GC events will not be generated for the object.</li>
	 * <li>The affect of the <code>isOptimistic</code> argument should be
	 * implemented for the sake of api clarity or the exception from norm should
	 * be mentioned explicitly. In the default implementation the argument is
	 * used to configure if in the optimistic mode all fields of the object will
	 * be enlisted or in the pesimistic mode only the fields that are annotated
	 * with the {@link GcRadarToInclude} annotation.</li>
	 * </ol>
	 * 
	 * For the expected behavior of the isOptimistic parameter it is suggested
	 * that the implementor read the documentation for the
	 * {@link GcRadarNotToInclude} and {@link GcRadarToInclude} annotations.
	 * 
	 * @author R.daneel.olivaw
	 * @since 0.4
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
