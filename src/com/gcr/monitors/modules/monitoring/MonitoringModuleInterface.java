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

package com.gcr.monitors.modules.monitoring;

import com.gcr.monitors.modules.notification.NotificationModuleInterface;
import com.gcr.structs.MonitorStateEnum;

/**
 * The Interface MonitoringModuleInterface. This interface needs to be implemented by all
 * the implementations of the monitoring module to the gcMonitor.
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public interface MonitoringModuleInterface
{

    /**
     * Start monitoring.
     * 
     * @param keyCollection
     *            the collection of objects that need to be monitored
     * @param notificationMod
     *            the notification module that will be used for handeling the
     *            events
     * @return true, if successful
     */
    public boolean startMonitoring(NotificationModuleInterface notificationMod);

    /**
     * Stop monitoring.
     * 
     * @param notificationMod
     *            the notification mod
     * @return true, if successful
     */
    public boolean stopMonitoring(NotificationModuleInterface notificationMod);

    /**
     * Gets the status of the monitor.
     * 
     * @return the status
     * @see Thread.State
     */
    public MonitorStateEnum getMonitoringModuleStatus();
}
