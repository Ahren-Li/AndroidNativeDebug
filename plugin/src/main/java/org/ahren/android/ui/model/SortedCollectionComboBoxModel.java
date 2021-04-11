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

import org.jetbrains.annotations.NotNull;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

public class SortedCollectionComboBoxModel<E> extends ListComboBoxModel<E> {

    @NotNull
    private final Comparator<E> mComparator;

    private SortedCollectionComboBoxModel(Comparator<E> comparator, Function2<E, E, Boolean> equality, boolean allowDuplicates, Vector<E> data) {
        super(equality, allowDuplicates, data);
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        mComparator = comparator;
    }

    public SortedCollectionComboBoxModel(Comparator<E> comparator, Function2<E, E, Boolean> equality, boolean allowDuplicates) {
        this(comparator, equality, allowDuplicates, new Vector<>());
    }


    public SortedCollectionComboBoxModel(Comparator<E> comparator, Function2<E, E, Boolean> equality) {
        this(comparator, equality, !ALLOW_DUPLICATES);
    }

    public SortedCollectionComboBoxModel(Comparator<E> comparator) {
        this(comparator, Intrinsics::areEqual, !ALLOW_DUPLICATES);
    }

    @Override
    public final void update(E obj){
        CollectionsKt.sortWith(getData(), mComparator);
        super.update(obj);
    }

    @Override
    public void add(E obj) {
        if(!isAllowDuplicates() && contains(obj)) return;
        List<E> list = getData();
        list.add(obj);
        CollectionsKt.sortWith(list, mComparator);
        update(obj);
    }
}
