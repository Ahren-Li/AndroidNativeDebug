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

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;

import javax.swing.*;
import java.util.List;

public interface MutableComboBoxModelEx<E> extends MutableComboBoxModel<E> {

    final class DefaultImpls{
        public static <E> void add(MutableComboBoxModelEx<E> modelEx, E obj) {
            modelEx.addElement(obj);
        }

        public static <E> void addAll(MutableComboBoxModelEx<E> modelEx, List<E> list) {
            Intrinsics.checkParameterIsNotNull(list, "elements");
            for(E b : list){
                add(modelEx, b);
            }
        }

        public static <E> void remove(MutableComboBoxModelEx<E> modelEx, E obj) {
            modelEx.removeElement(obj);
        }

        public static <E> void removeAll(MutableComboBoxModelEx<E> modelEx) {
            modelEx.removeIf(o -> Boolean.TRUE);
        }

        public static <E> void removeIf(MutableComboBoxModelEx<E> modelEx, Function1<E, Boolean> function1)
        {
            Intrinsics.checkParameterIsNotNull(function1, "filter");
            IntRange iterable = RangesKt.until(0, modelEx.getSize());
            if(iterable.isEmpty()) return ;

            for(int i : iterable){
                if(function1.invoke(modelEx.get(i))){
                    modelEx.removeElementAt(i);
                }
            }

        }

        public static <E> void reset(MutableComboBoxModelEx<E> modelEx, List<E> list) {
            Intrinsics.checkParameterIsNotNull(list, "elements");
            modelEx.removeAll();
            modelEx.addAll(list);
        }

        public static <E> E get(MutableComboBoxModelEx<E> modelEx, int i) {
            return modelEx.getElementAt(i);
        }

        public static <E> boolean contains(MutableComboBoxModelEx<E> modelEx, E obj){
            IntRange iterable = RangesKt.until(0, modelEx.getSize());
            if(iterable.isEmpty()) return false;

            for(int i : iterable){
                if(Intrinsics.areEqual(modelEx.get(i), obj)){
                    return true;
                }
            }

            return false;
        }

        public static <E> int indexOf(MutableComboBoxModelEx<E> modelEx, E obj){
            IntRange iterable = RangesKt.until(0, modelEx.getSize());
            if(iterable.isEmpty()) return -1;

            for(int i : iterable){
                if(Intrinsics.areEqual(modelEx.get(i), obj)){
                    return i;
                }
            }

            return -1;
        }
    }

    void add(E obj);
    void addAll(List<E> list);
    void remove(E obj);
    void removeAll();
    void removeIf(Function1<E, Boolean> function1);
    void reset(List<E> list);
    E get(int i);
    boolean contains(E obj);
    int indexOf(E obj);
    List<E> getData();
}
