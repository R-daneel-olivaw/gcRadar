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

import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.gcr.callbacks.GcRadarCallback;
import com.gcr.monitors.ObjectTreeMonitor;
import com.gcr.monitors.modules.in.InputModuleInterface;
import com.gcr.monitors.modules.in.structs.annotation.GcRadarAnnotationScannerInterface;
import com.gcr.structs.AbstractObjectRefrenceKey;
import com.gcr.structs.AbstractObjectRefrenceKeyComparator;
import com.gcr.structs.FieldNameValuePair;

/**
 * The Class TreeInputModule is used by the {@link ObjectTreeMonitor} for most
 * of its input behaviors.
 * 
 * @author R.daneel.olivaw
 * @since 0.2
 */
public abstract class TreeInputModule implements InputModuleInterface {
	private boolean isOptimistic;
	private GcRadarAnnotationScannerInterface annotationSacnner;

	/** The watch list of objects being monitored. */
	private List<AbstractObjectRefrenceKey<Object>> watchList = new LinkedList<AbstractObjectRefrenceKey<Object>>();

	/** The weak reference queue. */
	private ReferenceQueue<Object> weakReferenceQueue = new ReferenceQueue<Object>();

	/** The phantom reference queue. */
	private ReferenceQueue<Object> phantomReferenceQueue = new ReferenceQueue<Object>();

	/**
	 * The history map that is used to endure that no 2 alias objects are added
	 * more than once.
	 */
	private HashMap<Object, AbstractObjectRefrenceKey<Object>> historyMap = new HashMap<Object, AbstractObjectRefrenceKey<Object>>();

	/**
	 * Instantiates a new tree input module.
	 * 
	 * @param isOptimistic
	 *            operation mode, for more details please refer to
	 *            {@link ObjectTreeMonitor}
	 */
	public TreeInputModule(boolean isOptimistic) {
		this.isOptimistic = isOptimistic;
		annotationSacnner = new GcRadarAnnotationScanner();
	}

	/**
	 * Instantiates a new tree input module.
	 * 
	 * @param isOptimistic
	 *            the is optimistic
	 * @param annotationScanner
	 *            the annotation scanner implementation to be used
	 * 
	 * @throws NullPointerException
	 *             If annotationScanner is null
	 */
	public TreeInputModule(boolean isOptimistic,
			GcRadarAnnotationScannerInterface annotationScanner) {

		if (annotationScanner == null) {
			throw new IllegalArgumentException(
					"Annotation Scanner can not be null");
		}

		this.isOptimistic = isOptimistic;
		annotationSacnner = annotationScanner;
	}

	/**
	 * {@inheritDoc ObjectTreeMonitor#addObject(Object, Object,
	 * GcRadarCallback)}
	 */
	@Override
	public <I> boolean addObject(I object, String identifier,
			GcRadarCallback callback) {
		if (object == null) {
			throw new NullPointerException("Can not add null to monitoring");
		}
		if (identifier == null) {
			throw new NullPointerException("identifier can not be null");
		}

		if (!historyMap.containsKey(identifier)) {
			// get all the non primitive fields from the object.
			List<? extends FieldNameValuePair<? extends Object>> allFields = annotationSacnner
					.getAllFields(object, identifier, isOptimistic);

			synchronized (watchList) {
				// Add the object itself
				AbstractObjectRefrenceKey<Object> refrenceKey = new TreeObjectRefrenceKey<Object>(
						object, identifier, weakReferenceQueue,
						phantomReferenceQueue, callback);
				watchList.add(refrenceKey);
				historyMap.put(identifier, refrenceKey);

				// then add all its comprising fields
				int counter = 1;
				for (FieldNameValuePair<? extends Object> fnPair : allFields) {
					refrenceKey = new TreeObjectRefrenceKey<Object>(
							fnPair.getRefrenceValue(), fnPair.getFieldName(),
							weakReferenceQueue, phantomReferenceQueue, callback);

					watchList.add(refrenceKey);

					historyMap.put(identifier + "[" + counter + "]",
							refrenceKey);

					counter++;
				}
			}

			// help GC
			allFields = null;

			return true;
		} else {
			return false;
		}
	}

	/**
	 * This operation is not supported by the monitor
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public <I> boolean addObject(I object, GcRadarCallback callback) {
		throw new UnsupportedOperationException(
				"TreeInputModule does not support this operation. Please use addObject(I , Object , GcRadarCallback)");
	}

	/**
	 * {@inheritDoc InputModuleInterface#removeObject(Object)}
	 */
	@Override
	public boolean removeObject(String objectKey) {
		if (objectKey == null) {
			throw new NullPointerException("identifier can not be null");
		}

		synchronized (watchList) {
			if (historyMap.containsKey(objectKey)) {
				AbstractObjectRefrenceKey<Object> abstractObjectRefrenceKey = historyMap
						.get(objectKey);
				watchList.remove(abstractObjectRefrenceKey);
				historyMap.remove(objectKey);

				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects() {
		TreeSet<AbstractObjectRefrenceKey<Object>> treeSet = new TreeSet<AbstractObjectRefrenceKey<Object>>(
				new AbstractObjectRefrenceKeyComparator());
		synchronized (watchList) {
			treeSet.addAll(watchList);
		}

		return treeSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPendingObjectsCount() {
		return watchList.size();
	}

	/**
	 * Gets the watch list.
	 * 
	 * @return the watch list
	 */
	protected List<AbstractObjectRefrenceKey<Object>> getWatchList() {
		return watchList;
	}

	/**
	 * The Class TreeObjectRefrenceKey is an implementation of the
	 * AbstractObjectRefrenceKey.
	 * 
	 * @param <T>
	 *            the generic type
	 * @see AbstractObjectRefrenceKey
	 * 
	 */
	private class TreeObjectRefrenceKey<T> extends AbstractObjectRefrenceKey<T> {

		/**
		 * Instantiates a new sequential object refrence key.
		 * 
		 * @param object
		 *            the object to be monitored
		 * @param identifier
		 *            the object that will be used as an alias to the object
		 *            being monitored
		 * @param weakReferenceQueue
		 *            the weak reference queue
		 * @param phantomReferenceQueue
		 *            the phantom reference queue
		 * @param callback
		 *            the callback that will be used to notify in case of GC
		 *            events
		 */
		public TreeObjectRefrenceKey(T object, String identifier,
				ReferenceQueue<Object> weakReferenceQueue,
				ReferenceQueue<Object> phantomReferenceQueue,
				GcRadarCallback callback) {
			super(object, identifier, weakReferenceQueue,
					phantomReferenceQueue, callback);
		}
	}
}
