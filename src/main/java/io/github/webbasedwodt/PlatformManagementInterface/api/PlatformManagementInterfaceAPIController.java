package io.github.webbasedwodt.PlatformManagementInterface.api;

import io.github.webbasedwodt.common.WebServerController;

/*
 * Copyright (c) 2024. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.javalin.http.Context;

/**
 * This interface represents the Platform Management Interface controller for the exposed APIs.
*/
public interface PlatformManagementInterfaceAPIController extends WebServerController {
    /**
     * Notify registration to a new Platform.
    * @param context the javalin context
    */
    void routeNewRegistration(Context context);
}