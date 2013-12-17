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

import java.util.Comparator;

/**
 * The Class AbstractObjectRefrenceKeyComparator is used to compare 2 objects of
 * type {@link AbstractObjectRefrenceKey}.
 * 
 * @param <T>
 *            the generic type of the objects of type
 *            {@link AbstractObjectRefrenceKey}
 * @author R.daneel.olivaw
 * @since 0.1
 */
public class AbstractObjectRefrenceKeyComparator implements Comparator<AbstractObjectRefrenceKey<?>>
{

    /**
     * Compares 2 objects of AbstractObjectRefrenceKey type.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(AbstractObjectRefrenceKey<?> o1, AbstractObjectRefrenceKey<?> o2)
    {
	if (o1.getWeakCallbackTime() == null && o2.getWeakCallbackTime() == null)
	{
	    if (o1.getDateAdded().before(o2.getDateAdded()))
	    {
		return 1;
	    }
	    else if (o1.getDateAdded().after(o2.getDateAdded()))
	    {
		return -1;
	    }
	    else
	    {
		return 0;
	    }
	}
	else if (o1.getWeakCallbackTime() != null && o2.getWeakCallbackTime() != null)
	{
	    if (o1.getWeakCallbackTime().before(o2.getWeakCallbackTime()))
	    {
		return 1;
	    }
	    else if (o1.getWeakCallbackTime().after(o2.getWeakCallbackTime()))
	    {
		return -1;
	    }
	    else
	    {
		return 0;
	    }
	}
	else
	{
	    if (o1.getWeakCallbackTime() != null)
	    {
		return 1;
	    }
	    else
	    {
		return -1;
	    }
	}
    }
}
