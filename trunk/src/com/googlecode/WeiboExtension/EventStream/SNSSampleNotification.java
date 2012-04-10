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
/**
 * This class is used to trace the state of the service throughout the application.
 */
import com.googlecode.WeiboExtension.EventStream.SNSSamplePluginApplication.State;

public class SNSSampleNotification {

    private SNSSamplePluginApplication.State mState = State.NOT_CONFIGURED;
    private Object mExtraValue = null;

    public SNSSampleNotification(State newState) {
        this(newState, null);
    }

    public SNSSampleNotification(State newState, Object extraData) {
        mState = newState;
        mExtraValue = extraData;
    }

    public State getState() {
        return mState;
    }
    public Object getExtraData() {
        return mExtraValue;
    }
    
}
