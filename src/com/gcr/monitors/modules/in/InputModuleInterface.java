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

package com.gcr.monitors.modules.in;

import java.util.Set;

import com.gcr.callbacks.GcRadarCallback;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * The Interface InputModuleInterface. This interface needs to be implemented by all the
 * implementations of the input module to the gcMonitor.
 * 
 * @author R.daneel.olivaw
 * @since 0.1
 */
public interface InputModuleInterface
{

    /**
     * Adds the object.
     * 
     * @param <I>
     *            the generic type of the object being monitored
     * @param object
     *            the object to be added to monitoring
     * @param identifier
     *            the identifier object that will be used as an alias for the
     *            object being monitored
     * @param callback
     *            the callback that will be notified about the GC events
     * @return true, if successful
     */
    public <I> boolean addObject(I object, String identifier, GcRadarCallback callback);

    /**
     * Adds the object. Assigns an auto generated identifier to the object.
     * 
     * @param <I>
     *            the generic type of the object being monitored
     * @param object
     *            the object to be added to monitoring
     * @param callback
     *            the callback that will be notified about the GC events
     * @return true, if successful
     */
    public <I> boolean addObject(I object, GcRadarCallback callback);

    /**
     * Removes the object.
     * 
     * @param objectKey
     *            the alias object key that was used while adding the object
     * @return true, if successful
     */
    public boolean removeObject(String objectKey);

    /**
     * Gets the pending objects.
     *
     * @return the pending objects
     */
    public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects();

    /**
     * Gets the pending objects count.
     *
     * @return the pending objects count
     */
    public int getPendingObjectsCount();
}
