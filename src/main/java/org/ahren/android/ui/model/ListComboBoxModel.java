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
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

public class ListComboBoxModel<E> extends DefaultComboBoxModel<E> implements MutableComboBoxModelEx<E> {

    public static final boolean ALLOW_DUPLICATES = true;

    @NotNull
    private final Function2<E, E, Boolean> mEqualityFunction;
    private final boolean mAllowDuplicates;
    private final Vector<E> mVector;

    public ListComboBoxModel(Function2<E, E, Boolean> equality, boolean allowDuplicates, Vector<E> data) {
        super(data);
        Intrinsics.checkParameterIsNotNull(equality, "equalityFunction");
        mEqualityFunction = equality;
        mAllowDuplicates = allowDuplicates;
        mVector = data;
    }

    public ListComboBoxModel(Function2<E, E, Boolean> equality, boolean allowDuplicates) {
        this(equality, allowDuplicates, new Vector<>());
    }

    public ListComboBoxModel( Function2<E, E, Boolean> equality) {
        this(equality, !ALLOW_DUPLICATES);
    }

    public ListComboBoxModel() {
        this(Intrinsics::areEqual, !ALLOW_DUPLICATES);
    }

    protected void update(E obj){
        if(obj != null){
            int i = indexOf(obj);
            fireIntervalAdded(this, i, i);
        }

        fireContentsChanged(this, -1, -1);
    }

    @Override
    public void add(E obj) {
        if(!mAllowDuplicates && contains(obj)) return;
        mVector.add(obj);
        update(obj);
    }

    @Override
    public void addAll(List<E> list) {
        Intrinsics.checkParameterIsNotNull(list, "elements");
        boolean needUpdate = false;
        if(mAllowDuplicates){
            mVector.addAll(list);
        }else{
            for(E e : list){
                if(!contains(e)){
                    mVector.add(e);
                    needUpdate = true;
                }
            }
        }

        if(needUpdate) update(null);
    }

    @Override
    public void remove(E obj) {
        DefaultImpls.remove(this, obj);
    }

    @Override
    public void removeAll() {
        removeAllElements();
    }

    @Override
    public void removeIf(Function1<E, Boolean> needRove) {
        Intrinsics.checkParameterIsNotNull(needRove, "filter");
        DefaultImpls.removeIf(this, needRove);
    }

    @Override
    public void reset(List<E> list) {
        DefaultImpls.reset(this, list);
    }

    @Override
    public E get(int i) {
        return DefaultImpls.get(this, i);
    }

    @Override
    public boolean contains(E obj) {
        if(obj != null && !mVector.isEmpty()){
            for(E e : mVector){
                return mEqualityFunction.invoke(e, obj);
            }
        }

        return false;
    }

    @Override
    public int indexOf(E obj) {
        return DefaultImpls.indexOf(this, obj);
    }

    @Override
    public List<E> getData() {
        return mVector;
    }

    public boolean isAllowDuplicates() {
        return mAllowDuplicates;
    }
}
