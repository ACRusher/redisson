/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.core;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * Async list functions
 *
 * @author Nikita Koksharov
 *
 * @param <V> the type of elements held in this collection
 */
public interface RListAsync<V> extends RCollectionAsync<V>, RandomAccess {

    /**
     * Add <code>element</code> after <code>elementToFind</code>
     * 
     * @param elementToFind
     * @param element
     * @return new list size
     */
    RFuture<Integer> addAfterAsync(V elementToFind, V element);
    
    /**
     * Add <code>element</code> before <code>elementToFind</code>
     * 
     * @param elementToFind
     * @param element
     * @return new list size
     */
    RFuture<Integer> addBeforeAsync(V elementToFind, V element);
    
    RFuture<Boolean> addAllAsync(int index, Collection<? extends V> coll);

    RFuture<Integer> lastIndexOfAsync(Object o);

    RFuture<Integer> indexOfAsync(Object o);

    /**
     * Set <code>element</code> at <code>index</code>.
     * Works faster than {@link #setAsync(int, Object)} but 
     * doesn't return previous element.
     * 
     * @param index
     * @param element
     */
    RFuture<Void> fastSetAsync(int index, V element);

    RFuture<V> setAsync(int index, V element);

    RFuture<V> getAsync(int index);

    /**
     * Read all elements at once
     *
     * @return
     */
    RFuture<List<V>> readAllAsync();

    /**
     * Trim list and remains elements only in specified range
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, inclusive.
     *
     * @param fromIndex
     * @param toIndex
     * @return
     */
    RFuture<Void> trimAsync(int fromIndex, int toIndex);

    RFuture<Void> fastRemoveAsync(int index);
    
}
