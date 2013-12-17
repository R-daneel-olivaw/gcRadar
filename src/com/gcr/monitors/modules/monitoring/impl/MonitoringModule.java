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

package com.gcr.monitors.modules.monitoring.impl;

import java.lang.Thread.State;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.gcr.monitors.modules.monitoring.MonitoringModuleInterface;
import com.gcr.monitors.modules.notification.NotificationModuleInterface;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * The Class MonitoringModule is the implementation of the interface.
 *
 * {@link MonitoringModuleInterface} that uses a single worker thread for the monitoring.
 * @author R.daneel.olivaw
 * @since 0.1
 */
public abstract class MonitoringModule implements MonitoringModuleInterface
{

    /** The monitoring worker thread. */
    private MonitorThread monitorThread;
    
    /** The notification module. */
    private NotificationModuleInterface notificationMod;

    /** The stop flag that is set when the monitoring thread is stopped. */
    private boolean stopFlag = false;
    
    protected MonitoringModule(List<AbstractObjectRefrenceKey<Object>> keyCollection)
    {
	monitorThread = new MonitorThread(keyCollection);
    }

    /** {@inheritDoc}
     * @see com.gcr.monitors.modules.monitoring.MonitoringModuleInterface#startMonitoring(java.util.List, com.gcr.monitors.modules.notification.NotificationModuleInterface)
     */
    @Override
    public boolean startMonitoring(NotificationModuleInterface notificationMod)
    {
	this.notificationMod = notificationMod;
	monitorThread.start();

	stopFlag = false;

	return true;
    }

    /** {@inheritDoc}
     * @see com.gcr.monitors.modules.monitoring.MonitoringModuleInterface#stopMonitoring(com.gcr.monitors.modules.notification.NotificationModuleInterface)
     */
    @Override
    public boolean stopMonitoring(NotificationModuleInterface notificationMod)
    {
	if (getStatus() == State.RUNNABLE)
	{
	    stopFlag = true;
	    return true;
	}
	else
	{
	    return false;
	}

    }

    /** {@inheritDoc}
     * @see com.gcr.monitors.modules.monitoring.MonitoringModuleInterface#getStatus()
     */
    public State getStatus()
    {
	if (monitorThread == null)
	{
	    return Thread.State.TERMINATED;
	}

	return State.RUNNABLE;
    }

    /*
     * The Class MonitorThread is the thread cla.
     */
    private class MonitorThread extends Thread
    {
	
	/* The watch list. */
	private List<AbstractObjectRefrenceKey<Object>> watchList;

	/*
	 * Instantiates a new monitor thread.
	 *
	 * @param watchList the watch list
	 */
	public MonitorThread(List<AbstractObjectRefrenceKey<Object>> watchList)
	{
	    this.watchList = watchList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
	    super.run();

	    while (!watchList.isEmpty())
	    {
		if (stopFlag)
		{
		    break;
		}

		synchronized (watchList)
		{
		    Iterator<AbstractObjectRefrenceKey<Object>> iterator = watchList.iterator();

		    AbstractObjectRefrenceKey<Object> loopBuffer = null;
		    while (iterator.hasNext())
		    {
			if (stopFlag)
			{
			    break;
			}

			loopBuffer = iterator.next();

			if (loopBuffer.getwRef().isEnqueued() && loopBuffer.getWeakCallbackTime() == null)
			{
			    loopBuffer.setWeakCallbackTime(new Date());
			    // loopBuffer.getCallback().noSurvivingRefrence(loopBuffer);
			    notificationMod.notifyPreGcEvent(loopBuffer);
			}
			else if (loopBuffer.getpRef().isEnqueued())
			{
			    loopBuffer.setPhantomCallbackTime(new Date());
			    // loopBuffer.getCallback().objectReclaimedByGC(loopBuffer);
			    notificationMod.notifyPostGcEvent(loopBuffer);

			    iterator.remove();
			}
		    }

		    if (stopFlag)
		    {
			break;
		    }
		}
		Thread.yield();
	    }

	    monitorThread = null;
	}
    }

}
