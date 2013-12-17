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

package com.gcr.monitors.modules.notification.impl;

import com.gcr.monitors.modules.notification.NotificationModuleInterface;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * The Class NotificationModule is the implementation of the
 * {@link NotificationModuleInterface} the module notifies the listener of any GC events
 * that take place pertaining the objects being monitored.
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public abstract class NotificationModule implements NotificationModuleInterface
{

    /**{@inheritDoc}
     * 
     * @see com.gcr.monitors.modules.notification.NotificationModuleInterface#
     * notifyStartMonitoring()
     */
    @Override
    public boolean notifyStartMonitoring()
    {
	// TODO Auto-generated method stub
	return false;
    }

    /**{@inheritDoc}
     * 
     * @see
     * com.gcr.monitors.modules.notification.NotificationModuleInterface#notifyStopMonitoring
     * ()
     */
    @Override
    public boolean notifyStopMonitoring()
    {
	// TODO Auto-generated method stub
	return false;
    }

    /**{@inheritDoc}
     * 
     * @see
     * com.gcr.monitors.modules.notification.NotificationModuleInterface#notifyPreGcEvent
     * (com.gcr.structs.AbstractObjectRefrenceKey)
     */
    @Override
    public boolean notifyPreGcEvent(AbstractObjectRefrenceKey<Object> refrenceKey)
    {
	refrenceKey.getCallback().noSurvivingRefrence(refrenceKey);
	return false;
    }

    /**{@inheritDoc}
     * 
     * @see
     * com.gcr.monitors.modules.notification.NotificationModuleInterface#notifyPostGcEvent
     * (com.gcr.structs.AbstractObjectRefrenceKey)
     */
    @Override
    public boolean notifyPostGcEvent(AbstractObjectRefrenceKey<Object> refrenceKey)
    {
	refrenceKey.getCallback().objectReclaimedByGC(refrenceKey);
	return true;
    }

}
