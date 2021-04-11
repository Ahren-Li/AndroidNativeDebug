/*
 * Copyright 2019 Ahren Li(www.lili.kim) AndroidNativeDebug
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ahren.android.ui.model;

import com.google.common.collect.ImmutableList;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class AndroidListModel<E> extends DefaultListModel<E> {

    private ImmutableList<E> mEntries;

    public AndroidListModel(@NotNull List<E> list){
        super();
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        builder.addAll(list);
        mEntries = builder.build();
    }

    public AndroidListModel(@NotNull Object[] list, @NotNull Function1<Object, E> creator){
        super();
        mEntries = createEntries(list, creator);
    }

    @Override
    public int getSize() {
        return mEntries.size();
    }

    @Override
    public E getElementAt(int index) {
        return mEntries.get(index);
    }

    public void entryContentChanged(E object){
        int index = findEntry(object);
        if (index >= 0) {
            fireContentsChanged(object, index, index);
        }
    }

    private int findEntry(E obj) {
        for(int i = 0; i < this.mEntries.size(); ++i) {
            if ((mEntries.get(i)).equals(obj)) {
                return i;
            }
        }

        return -1;
    }

    @NotNull
    private ImmutableList<E> createEntries(@NotNull Object[] list, Function1<Object, E> creator) {
        ImmutableList.Builder<E> entries = ImmutableList.builder();

        for(Object obj : list){
            E e = creator.invoke(obj);
            if(e != null){
                entries.add(creator.invoke(obj));
            }
        }

        return entries.build();
    }
}
