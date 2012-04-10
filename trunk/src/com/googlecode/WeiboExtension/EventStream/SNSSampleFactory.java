/*
 * Copyright 2010 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.googlecode.WeiboExtension.EventStream;

import android.content.Context;
/**
 * This is a factory class used to get the single entity of snsSample. 
 * 
 */
public class SNSSampleFactory {
    private static SNSsample sSNSSampleInstance = null;

    private SNSSampleFactory() {

    }

    public synchronized static final SNSsample getSNSSample(Context context) {
        if (sSNSSampleInstance == null) {
            sSNSSampleInstance = new SNSsample(new Settings(context));
            sSNSSampleInstance.initialize();
        }
        return sSNSSampleInstance;
    }

    public synchronized static final void terminate(boolean reinitialize) {
        if (sSNSSampleInstance != null) {
            sSNSSampleInstance.shutdown();
            if (reinitialize) {
                sSNSSampleInstance.initialize();
            } else {
                sSNSSampleInstance = null;
            }
        }
    }
}