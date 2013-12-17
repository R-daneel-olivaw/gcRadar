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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.gcr.callbacks.GcRadarCallback;
import com.gcr.monitors.modules.in.InputModuleInterface;
import com.gcr.structs.AbstractObjectRefrenceKey;
import com.gcr.structs.AbstractObjectRefrenceKeyComparator;

/**
 * The Class InputModule is the implementation of {@link InputModuleInterface}
 * that adds one object at a time to the monitoring list.
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public abstract class InputModule implements InputModuleInterface
{

    /**
     * The entry counter is used to generate identifier objects when not
     * provided. This counter in never decremented.
     */
    private int entryCounter;

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
     * {@inheritDoc}
     * 
     * @see com.gcr.monitors.modules.in.InputModuleInterface#addObject(java.lang.Object,
     *      java.lang.Object, com.gcr.callbacks.GcRadarCallback)
     */
    @Override
    public <I> boolean addObject(I object, String identifier, GcRadarCallback callback)
    {
	if (object == null)
	{
	    throw new NullPointerException("Can not add null to monitoring");
	}
	if (identifier == null)
	{
	    throw new NullPointerException("identifier can not be null");
	}

	synchronized (watchList)
	{

	    if (!historyMap.containsKey(identifier))
	    {
		AbstractObjectRefrenceKey<Object> refrenceKey = new SequentialObjectRefrenceKey<Object>(object, identifier,
			weakReferenceQueue, phantomReferenceQueue, callback);

		watchList.add(refrenceKey);

		historyMap.put(identifier, refrenceKey);

		incrementEntryCounter();

		return true;
	    }
	    else
	    {
		return false;
	    }
	}
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.gcr.monitors.modules.in.InputModuleInterface#addObject(java.lang.Object,
     *      com.gcr.callbacks.GcRadarCallback)
     */
    public <I> boolean addObject(I object, GcRadarCallback callback)
    {
	if (object == null)
	{
	    throw new NullPointerException("Can not add null to monitoring");
	}

	String autoGenKey = object.getClass().getName() + " - " + incrementEntryCounter();

	return addObject(object, autoGenKey, callback);

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.gcr.monitors.modules.in.InputModuleInterface#removeObject(java.lang.Object)
     */
    @Override
    public boolean removeObject(String objectKey)
    {
	if (objectKey == null)
	{
	    throw new NullPointerException("identifier can not be null");
	}

	synchronized (watchList)
	{
	    if (historyMap.containsKey(objectKey))
	    {
		AbstractObjectRefrenceKey<Object> abstractObjectRefrenceKey = historyMap.get(objectKey);
		watchList.remove(abstractObjectRefrenceKey);
		historyMap.remove(objectKey);

		return true;
	    }
	    else
	    {
		return false;
	    }
	}
    }

    protected List<AbstractObjectRefrenceKey<Object>> getWatchList()
    {
	return watchList;
    }

    /**
     * Returns a sorted set containing all the objects monitored that have not
     * been garbage collected.
     * 
     * @return Sorted set of the pending objects
     * 
     * @see com.gcr.monitors.modules.in.InputModuleInterface#getPendingObjects()
     */
    public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects()
    {
	TreeSet<AbstractObjectRefrenceKey<Object>> treeSet = new TreeSet<AbstractObjectRefrenceKey<Object>>(
		new AbstractObjectRefrenceKeyComparator());
	synchronized (watchList)
	{
	    treeSet.addAll(watchList);
	}

	return treeSet;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.gcr.monitors.modules.in.InputModuleInterface#getPendingObjectsCount()
     */
    public int getPendingObjectsCount()
    {
	return watchList.size();
    }

    /**
     * Adds the objects inside the collection to the monitor for monitoring.
     * 
     * @param collection
     *            the collection elements of which need to be added to the
     *            monitoring
     * @param identifier
     *            the root identifier that will be used to derive all the
     *            individual identifiers
     * @param callback
     *            the callback that will be used for all the sending
     *            notifications for the objects to be added
     * @return true, if successful
     * 
     * @throws IllegalArgumentException
     *             if collections is null
     * @since 0.2
     */
    public boolean addAll(Collection<?> collection, String identifier, GcRadarCallback callback)
    {
	if (collection == null)
	{
	    throw new IllegalArgumentException("collections can not be null");
	}

	Iterator<?> iterator = collection.iterator();

	Object nextObject = null;
	while (iterator.hasNext())
	{
	    nextObject = iterator.next();

	    String nextIdentifier = identifier.toString() + " " + entryCounter;
	    addObject(nextObject, nextIdentifier, callback);
	}

	return true;
    }

    /**
     * Removes the objects that are represented by the alias keys.
     * 
     * @param keyCollection
     *            the collection of alias keys
     * @return true, if successful
     * @throws IllegalArgumentException
     *             if collections is null
     * @since 0.2
     */
    public boolean removeAll(Collection<? extends AbstractObjectRefrenceKey<?>> keyCollection)
    {
	if (keyCollection == null)
	{
	    throw new IllegalArgumentException("Key collection can not be null");
	}

	Iterator<? extends AbstractObjectRefrenceKey<?>> iterator = keyCollection.iterator();

	AbstractObjectRefrenceKey<?> next = null;
	while (iterator.hasNext())
	{
	    next = iterator.next();

	    removeObject(next.getObjRefrenceKey());
	}

	return true;
    }

    /**
     * The Class SequentialObjectRefrenceKey is an implementation of the
     * AbstractObjectRefrenceKey.
     * 
     * @param <T>
     *            the generic type
     * @see AbstractObjectRefrenceKey
     * 
     */
    private class SequentialObjectRefrenceKey<T> extends AbstractObjectRefrenceKey<T>
    {

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
	public SequentialObjectRefrenceKey(T object, String identifier, ReferenceQueue<Object> weakReferenceQueue,
		ReferenceQueue<Object> phantomReferenceQueue, GcRadarCallback callback)
	{
	    super(object, identifier, weakReferenceQueue, phantomReferenceQueue, callback);
	}
    }

    /**
     * Increment entry counter.
     * 
     * @return the incremented counter value
     */
    private int incrementEntryCounter()
    {
	return ++entryCounter;
    }
}
