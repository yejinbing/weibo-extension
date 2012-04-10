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
 * 
 * @author Johan Abramsson
 * This class contains constant definitions used by the EventStreamSNSEngine class.
 *
 */
public interface EventstreamSNSEngineConstants {
	// Friend ID Starter
    public static final String FRIEND_ID_PREFIX = "EventStreamSNSSampleEngingeFriend-";
    public static final String EVENT_ID_PREFIX = "EventStreamSNSSampleEngingeEvent-";

//    public static final String AUTHORITY = "com.sonyericsson.android.timescapepluginsnspivotsample";
    public static final String ACTION_MANUAL_SYNC = "MANUAL_SYNC_OF_SNS";
    public static final String ACTION_FILL_ALL_FRIENDS = "FILL_FRIENDS_TO_SNS";
    
    int TYPE_SIMPLE_MESSAGE_WITH_FRIENDS_IMAGE = 101;
    int TYPE_SIMPLE_MESSAGE_WITH_MY_IMAGE = 102;
    int TYPE_MESSAGE_WITH_FRIENDS_IMAGE = 201;
    int TYPE_MESSAGE_WITH_MY_IMAGE = 202;
    int TYPE_GALLERY = 300;
    int TYPE_GALLERY_GROUPED_BY_ID = 400;


}
