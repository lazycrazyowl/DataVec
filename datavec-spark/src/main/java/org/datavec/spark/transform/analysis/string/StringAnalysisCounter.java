/*-
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.spark.transform.analysis.string;

import org.datavec.spark.transform.analysis.AnalysisCounter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.datavec.api.writable.Writable;
import org.apache.spark.util.StatCounter;

/**
 * A counter function for doing analysis on String columns, on Spark
 *
 * @author Alex Black
 */
@AllArgsConstructor
@Data
public class StringAnalysisCounter implements AnalysisCounter<StringAnalysisCounter> {

    private StatCounter counter = new StatCounter();
    private long countZeroLength = 0;
    private long countMinLength = 0;
    private long countMaxLength = 0;

    public StringAnalysisCounter() {};

    public int getMinLengthSeen() {
        return (int) counter.min();
    };

    public int getMaxLengthSeen() {
        return (int) counter.max();
    };

    public long getSumLength() {
        return (long) counter.sum();
    };

    public long getCountTotal() {
        return counter.count();
    };

    public double getSampleStdev() {
        return counter.sampleStdev();
    };

    public double getMean() {
        return counter.mean();
    }

    public double getSampleVariance() {
        return counter.sampleVariance();
    }

    @Override
    public StringAnalysisCounter add(Writable writable) {
        int length = writable.toString().length();

        if (length == 0)
            countZeroLength++;

        if (length == getMinLengthSeen())
            countMinLength++;
        else if (length < getMinLengthSeen()) {
            countMinLength = 1;
        }

        if (length == getMaxLengthSeen())
            countMaxLength++;
        else if (length > getMaxLengthSeen()) {
            countMaxLength = 1;
        }
        counter.merge((double) length);

        return this;
    }

    public StringAnalysisCounter merge(StringAnalysisCounter other) {
        int otherMin = other.getMinLengthSeen();
        long newCountMinLength;
        if (getMinLengthSeen() == otherMin) {
            newCountMinLength = countMinLength + other.getCountMinLength();
        } else if (getMinLengthSeen() > otherMin) {
            //Keep other, take count from other
            newCountMinLength = other.getCountMinLength();
        } else {
            //Keep this min, no change to count
            newCountMinLength = countMinLength;
        }

        int otherMax = other.getMaxLengthSeen();
        long newCountMaxLength;
        if (getMaxLengthSeen() == otherMax) {
            newCountMaxLength = countMaxLength + other.getCountMaxLength();
        } else if (getMaxLengthSeen() < otherMax) {
            //Keep other, take count from other
            newCountMaxLength = other.getCountMaxLength();
        } else {
            //Keep this max, no change to count
            newCountMaxLength = countMaxLength;
        }

        return new StringAnalysisCounter(counter.merge(other.getCounter()),
                        countZeroLength + other.getCountZeroLength(), newCountMinLength, newCountMaxLength);
    }
}
