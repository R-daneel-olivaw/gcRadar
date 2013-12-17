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

package com.gcr.monitors.modules.notification;

import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * The Interface NotificationModuleInterface. This interface needs to be implemented by
 * all the implementations of the notification module to the gcMonitor.
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public interface NotificationModuleInterface
{

    /**
     * Notify start monitoring.
     * 
     * @return true, if successful
     */
    boolean notifyStartMonitoring();

    /**
     * Notify stop monitoring.
     * 
     * @return true, if successful
     */
    boolean notifyStopMonitoring();

    /**
     * Notify that the object being monitored has been orphaned but not garbage
     * collected yet.
     * 
     * @param refrenceKey
     *            the refrence key used as the alias to the object being
     *            monitored
     * @return true, if successful
     */
    boolean notifyPreGcEvent(AbstractObjectRefrenceKey<Object> refrenceKey);

    /**
     * Notify that the object being monitored has been garbage collected.
     * 
     * @param refrenceKey
     *            the refrence key used as the alias to the object being
     *            monitored
     * @return true, if successful
     */
    boolean notifyPostGcEvent(AbstractObjectRefrenceKey<Object> refrenceKey);
}
