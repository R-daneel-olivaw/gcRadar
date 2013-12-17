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

package com.gcr.monitors;

import java.lang.Thread.State;
import java.util.List;
import java.util.Set;

import com.gcr.callbacks.GcRadarCallback;
import com.gcr.monitors.modules.in.impl.InputModule;
import com.gcr.monitors.modules.monitoring.impl.MonitoringModule;
import com.gcr.monitors.modules.notification.impl.NotificationModule;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * This Object monitor runs on a worker thread and captures GC events on the
 * registered objects and notifies the callback class. The class uses the
 * following modules internally,
 * <ul>
 * <li>Individual object feed</li>
 * <li>Single worker threaded monitor</li>
 * <li>Callback for notification on GC events</li>
 * </ul>
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 * 
 */
public class SimpleObjectSingleThreadedMonitor<I>
{
    private InputModule inMod;
    private MonitoringModule monitoringMod;
    private NotificationModule notificationMod;

    private boolean stopFlag = false;

    /**
     * The constructor for creating the
     * {@link SimpleObjectSingleThreadedMonitor} object. The constructor will
     * initialize the 3 internal modules,
     * <ol>
     * <li>Individual object feed</li>
     * <li>Single worker threaded monitor</li>
     * <li>Callback for notification on GC events</li>
     * </ol>
     * in the above order. It also annotates the monitor as running(not
     * stopped).
     */
    public SimpleObjectSingleThreadedMonitor()
    {
	this.inMod = new IndividualObjectFeed_Impl();
	this.monitoringMod = new SingleThreadedMonitor_Impl(((IndividualObjectFeed_Impl) inMod).getWatchList());
	this.notificationMod = new CallbackNotificationModule_Impl();

	stopFlag = false;
    }

    /**
     * The method will add the object to the monitoring list & start or restart
     * the worker thread for the monitoring.
     * 
     * @param object
     *            - The object to be monitored
     * @param identifier
     *            - The key that will be used as a key for the object to be
     *            added for the monitoring purposes.
     * @param callback
     *            - {@link GcRadarCallback} is used when a GC event needs to be
     *            reported
     * @return <code>true</code> if the object was added successfully<br>
     *         <code>false</code> if the object was not added as the identifier
     *         used to add the object has already been used.
     * 
     * @throws NullPointerException
     *             if the object to be added of identifier is <code>null</code>.
     * @throws UnsupportedOperationException
     *             if the monitoring has been explicitly stopped by calling the
     *             {@link stopMonitoring()} method.
     */
    public <T extends I> boolean addObject(T object, String identifier, GcRadarCallback callback)
    {
	if (stopFlag)
	{
	    throw new UnsupportedOperationException("Objects can not be added after the moter has been stopped");
	}

	if (inMod.addObject(object, identifier, callback))
	{
	    State monitoringStatus = monitoringMod.getStatus();

	    if (monitoringStatus == State.TERMINATED)
	    {
		startMonitoring();
	    }

	    return true;
	}
	else
	{
	    return false;
	}
    }

    /**
     * The method will add the object to the monitoring list & start or restart
     * the worker thread for the monitoring. Assigns an auto generated
     * identifier to the object.
     * 
     * @param object
     *            - The object to be monitored
     * @param callback
     *            - {@link GcRadarCallback} is used when a GC event needs to be
     *            reported
     * @return <code>true</code> if the object was added successfully<br>
     *         <code>false</code> if the object was not added as the identifier
     *         used to add the object has already been used.
     * 
     * @throws NullPointerException
     *             if the object to be added of identifier is <code>null</code>.
     * @throws UnsupportedOperationException
     *             if the monitoring has been explicitly stopped by calling the
     *             {@link stopMonitoring()} method.
     */
    public <T extends I> boolean addObject(T object, GcRadarCallback callback)
    {
	if (stopFlag)
	{
	    throw new UnsupportedOperationException("Objects can not be added after the moter has been stopped");
	}

	if (inMod.addObject(object, callback))
	{
	    State monitoringStatus = monitoringMod.getStatus();

	    if (monitoringStatus == State.TERMINATED)
	    {
		startMonitoring();
	    }

	    return true;
	}
	else
	{
	    return false;
	}
    }

    /**
     * This method will remove the object from monitoring
     * 
     * @param objectKey
     *            - the identifier key used at the time of adding the object
     * @return <code>true</code> if the object was removed sucessfully.<br>
     *         <code>false</code> if the object was not removed.
     * @throws NullPointerException
     *             if objectKey is <code>null</code>
     * @throws UnsupportedOperationException
     *             if the monitoring has been explicitly stopped by calling the
     *             {@link stopMonitoring()} method.
     */
    public boolean removeObject(String objectKey)
    {
	if (stopFlag)
	{
	    throw new UnsupportedOperationException("Objects can not be removed after the moter has been stopped");
	}

	return inMod.removeObject(objectKey);
    }

    /**
     * Trigger the start of monitoring of the objects for GC events.
     * 
     * @return <code>true</code> if the monitoring has been started
     *         successfully.<br>
     *         <code>false</code> if monitoring could not be started.
     */
    public boolean startMonitoring()
    {
	notificationMod.notifyStartMonitoring();
	stopFlag = false;

	return monitoringMod.startMonitoring(notificationMod);
    }

    /**
     * Trigger the stop monitoring of the objects for GC events.
     * 
     * @return <code>true</code> if the monitoring has been stopped
     *         successfully.<br>
     *         <code>false</code> if monitoring could not be stopped.
     */
    public boolean stopMonitoring()
    {
	notificationMod.notifyStopMonitoring();
	stopFlag = true;

	return monitoringMod.stopMonitoring(notificationMod);
    }

    public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects()
    {
	return inMod.getPendingObjects();
    }

    public int getPendingObjectsCount()
    {
	return inMod.getPendingObjectsCount();
    }

    private class IndividualObjectFeed_Impl extends InputModule
    {
	@Override
	protected List<AbstractObjectRefrenceKey<Object>> getWatchList()
	{
	    return super.getWatchList();
	}
    }

    private class SingleThreadedMonitor_Impl extends MonitoringModule
    {
	protected SingleThreadedMonitor_Impl(List<AbstractObjectRefrenceKey<Object>> keyCollection)
	{
	    super(keyCollection);
	}
	// Full implementation in super as functionality used as is
    }

    private class CallbackNotificationModule_Impl extends NotificationModule
    {
	// Full implementation in super as functionality used as is
    }

}
