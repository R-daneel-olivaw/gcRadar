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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Date;

import com.gcr.callbacks.GcRadarCallback;

/**
 * The Class AbstractObjectRefrenceKey is a warper class for enclosing the
 * object that needs to be monitored by gcRadar .
 * 
 * @param <T>
 *            The generic type of the object being wrapped
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public abstract class AbstractObjectRefrenceKey<T>
{

    /**
     * Instantiates a new abstract object reference key. But as the class is
     * Abstract, the constructor can not be called.
     * 
     * @param object
     *            the object to be monitored
     * @param identifier
     *            the identifier that will used as an alias to the object being
     *            monitored
     * @param weakReferenceQueue
     *            the weak reference queue
     * @param phantomReferenceQueue
     *            the phantom reference queue
     * @param callback
     *            the callback that will be notified about the detected GC
     *            events regarding the object
     */
    public AbstractObjectRefrenceKey(T object, String identifier, ReferenceQueue<Object> weakReferenceQueue,
	    ReferenceQueue<Object> phantomReferenceQueue, GcRadarCallback callback)
    {
	setwRef(new WeakReference<T>(object, weakReferenceQueue));
	setpRef(new PhantomReference<T>(object, phantomReferenceQueue));

	setClazz(object.getClass());

	setDateAdded(new Date());
	setObjRefrenceKey(identifier);

	setCallback(callback);
    }

    private String objRefrenceKey;

    private Date dateAdded;

    private WeakReference<T> wRef;
    private PhantomReference<T> pRef;

    private Date weakCallbackTime;
    private Date phantomCallbackTime;

    private Class<? extends Object> clazz;

    private GcRadarCallback callback;

    /**
     * Gets the obj refrence key.
     * 
     * @return the obj refrence key
     */
    public String getObjRefrenceKey()
    {
	return objRefrenceKey;
    }

    private void setObjRefrenceKey(String objRefrenceKey)
    {
	this.objRefrenceKey = objRefrenceKey;
    }

    /**
     * Gets the date added.
     * 
     * @return the date added
     */
    public Date getDateAdded()
    {
	// return defensive copy
	if (dateAdded != null)
	{
	    return new Date(dateAdded.getTime());
	}
	else
	{
	    return null;
	}
    }

    private void setDateAdded(Date dateAdded)
    {
	this.dateAdded = dateAdded;
    }

    /**
     * Gets the Weak Reference of the monitored object.
     * 
     * @return the weak reference
     */
    public WeakReference<T> getwRef()
    {
	return wRef;
    }

    private void setwRef(WeakReference<T> wRef)
    {
	this.wRef = wRef;
    }

    /**
     * Gets the Class of the monitored object.
     * 
     * @return the class
     */
    public Class<? extends Object> getClazz()
    {
	return clazz;
    }

    private void setClazz(Class<? extends Object> class1)
    {
	this.clazz = class1;
    }

    /**
     * Gets the callback for the object.
     * 
     * @return the callback
     * @see GcRadarCallback
     */
    public GcRadarCallback getCallback()
    {
	return callback;
    }

    private void setCallback(GcRadarCallback callback)
    {
	this.callback = callback;
    }

    public PhantomReference<T> getpRef()
    {
	return pRef;
    }

    private void setpRef(PhantomReference<T> pRef)
    {
	this.pRef = pRef;
    }

    /**
     * Gets the time at which the object was orphaned.
     * 
     * @return the orphan time
     */
    public Date getWeakCallbackTime()
    {
	// return defensive copy
	if (weakCallbackTime != null)
	{
	    return new Date(weakCallbackTime.getTime());
	}
	else
	{
	    return null;
	}
    }

    public void setWeakCallbackTime(Date weakCallbackTime)
    {
	this.weakCallbackTime = weakCallbackTime;
    }

    /**
     * Gets the time at which the object was reclaimed by garbage collected.
     * 
     * @return the garbage collection time
     */
    public Date getPhantomCallbackTime()
    {
	// return defensive copy
	if (phantomCallbackTime != null)
	{
	    return new Date(phantomCallbackTime.getTime());
	}
	else
	{
	    return null;
	}
    }

    public void setPhantomCallbackTime(Date phantomCallbackTime)
    {
	this.phantomCallbackTime = phantomCallbackTime;
    }
}
