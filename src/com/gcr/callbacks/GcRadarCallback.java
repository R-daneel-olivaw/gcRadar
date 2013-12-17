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

package com.gcr.callbacks;

import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * This interface must be implemented by the entity that wishes to receive
 * callback from gcRadar
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 * 
 */
public interface GcRadarCallback
{
    /**
     * This method is called by the monitor to notify that the registered object
     * has been reclaimed by the Garbage Collector.
     * 
     * @param objWrapper
     *            {@link AbstractObjectRefrenceKey} The key that represents the
     *            object been reclaimed. This object is used by gcRadar as a
     *            wrapper around the object being monitored.
     * 
     */
    <T> void objectReclaimedByGC(AbstractObjectRefrenceKey<T> objWrapper);

    /**
     * This method is called by the monitor to notify that the registered object
     * has been orphaned i.e. there is no surviving direct reference to the
     * registered object.
     * 
     * @param objWrapper
     *            {@link AbstractObjectRefrenceKey} The key that represents the
     *            object been reclaimed. This object is used by gcRadar as a
     *            wrapper around the object being monitored.
     */
    <T> void noSurvivingRefrence(AbstractObjectRefrenceKey<T> objWrapper);
}
