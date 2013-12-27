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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gcr.monitors.ObjectTreeMonitor;
import com.gcr.monitors.modules.monitoring.MonitoringModuleInterface;
import com.gcr.monitors.modules.monitoring.structs.MonitorStateEnum;
import com.gcr.monitors.modules.notification.NotificationModuleInterface;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * The Class MonitoringModule is the implementation of the interface.
 * 
 * {@link MonitoringModuleInterface} that uses a single worker thread for the
 * monitoring.
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public abstract class MonitoringModule implements MonitoringModuleInterface {

	/** The monitoring worker thread. */
	private MonitorThread monitorThread;

	/** The notification module. */
	private NotificationModuleInterface notificationMod;

	private final ReentrantLock lock = new ReentrantLock();

	private Condition lockTillFinish;

	/** The stop flag that is set when the monitoring thread is stopped. */
	protected MonitoringModule(
			List<AbstractObjectRefrenceKey<Object>> keyCollection) {
		monitorThread = new MonitorThread(keyCollection);
		lockTillFinish = lock.newCondition();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.gcr.monitors.modules.monitoring.MonitoringModuleInterface#startMonitoring(java.util.List,
	 *      com.gcr.monitors.modules.notification.NotificationModuleInterface)
	 */
	@Override
	public boolean startMonitoring(NotificationModuleInterface notificationMod) {

		this.notificationMod = notificationMod;

		if (monitorThread.getState() != Thread.State.RUNNABLE) {
			monitorThread.start();
		}
		monitorThread.setStopFlag(false);

		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.gcr.monitors.modules.monitoring.MonitoringModuleInterface#stopMonitoring(com.gcr.monitors.modules.notification.NotificationModuleInterface)
	 */
	@Override
	public boolean stopMonitoring(NotificationModuleInterface notificationMod) {
		if (monitorThread.getState() != Thread.State.NEW) {

			monitorThread.setStopFlag(true);

			// if a monitoring thread is already running we stop it naturally by
			// raising a flag for it to stop. The we create a new instance of a
			// monitoring thread and reuse the watch-list from the previous
			// monitoring thread.
			MonitorThread monitorThreadBuffer = new MonitorThread(
					monitorThread.getWatchList());

			monitorThread = monitorThreadBuffer;

			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.gcr.monitors.modules.monitoring.MonitoringModuleInterface#getMonitoringModuleStatus()
	 */
	public MonitorStateEnum getMonitoringModuleStatus() {
		if (monitorThread == null) {
			return MonitorStateEnum.TERMINATED;
		}

		return MonitorStateEnum.RUNNING;
	}

	/**
	 * This method will hold the execution of the calling thread till the time
	 * one of the following happens,
	 * <ul>
	 * <li>All the objects in the monitor are claimed by garbage collector.</li>
	 * <li>The waiting thread is interrupted by some other thread</li>
	 * <li>The monitoring is stopped by calling the
	 * {@link ObjectTreeMonitor#startMonitoring()} method</li>
	 * </ul>
	 * 
	 * @throws InterruptedException
	 *             in case the waiting thread is interrupted
	 * @throws UnsupportedOperationException
	 *             in case the monitor is not running
	 * @since 0.4
	 */
	public void lock() throws InterruptedException {
		try {
			lock.lock();
			lockTillFinish.await();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This method will hold the execution of the calling thread till the time
	 * one of the following happens,
	 * <ul>
	 * <li>All the objects in the monitor are claimed by garbage collector.</li>
	 * <li>The waiting thread is interrupted by some other thread</li>
	 * <li>The timeout runs-out</li>
	 * <li>The monitoring is stopped by calling the
	 * </ul>
	 * 
	 * @param time
	 *            the time
	 * @param unit
	 *            the unit
	 * @throws InterruptedException
	 *             in case the waiting thread is interrupted
	 *             {@link ObjectTreeMonitor#startMonitoring()} method</li> </ul>
	 * @since 0.4
	 */
	public void lock(long time, TimeUnit unit) throws InterruptedException {
		try {
			lock.lock();
			lockTillFinish.await(time, unit);
		} finally {
			lock.unlock();
		}
	}

	// ===========INNER CLASSES==========

	/*
	 * The Class MonitorThread is the thread class.
	 */
	private class MonitorThread extends Thread {

		/* The watch list. */
		private final List<AbstractObjectRefrenceKey<Object>> watchList;

		private boolean stopFlag_i;

		/*
		 * Instantiates a new monitor thread.
		 * 
		 * @param watchList the watch list
		 */
		public MonitorThread(List<AbstractObjectRefrenceKey<Object>> watchList) {
			this.watchList = watchList;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			super.run();

			while (!getWatchList().isEmpty()) {
				if (isStopFlag()) {
					break;
				}

				synchronized (getWatchList()) {
					Iterator<AbstractObjectRefrenceKey<Object>> iterator = getWatchList()
							.iterator();

					AbstractObjectRefrenceKey<Object> loopBuffer = null;
					while (iterator.hasNext()) {
						if (isStopFlag()) {
							break;
						}

						loopBuffer = iterator.next();

						if (loopBuffer.getwRef().isEnqueued()
								&& loopBuffer.getWeakCallbackTime() == null) {
							loopBuffer.setWeakCallbackTime(new Date());
							// loopBuffer.getCallback().noSurvivingRefrence(loopBuffer);
							notificationMod.notifyPreGcEvent(loopBuffer);
						} else if (loopBuffer.getpRef().isEnqueued()) {
							loopBuffer.setPhantomCallbackTime(new Date());
							// loopBuffer.getCallback().objectReclaimedByGC(loopBuffer);
							notificationMod.notifyPostGcEvent(loopBuffer);

							iterator.remove();
						}
					}

					if (isStopFlag()) {
						break;
					}
				}
				Thread.yield();
			}

			try {
				lock.lock();
				// release locks if any
				lockTillFinish.signalAll();
			} finally {
				lock.unlock();
			}
		}

		private List<AbstractObjectRefrenceKey<Object>> getWatchList() {
			return watchList;
		}

		private boolean isStopFlag() {
			return stopFlag_i;
		}

		private void setStopFlag(boolean stopFlag) {
			this.stopFlag_i = stopFlag;
		}
	}

}
