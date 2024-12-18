package io.github.webbasedwodt.common;

/*
 * Copyright (c) 2023. Andrea Giulianelli
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

import io.javalin.Javalin;

/**
 * Interface for base controllers of {@link WoDTWebServer}.
*/
public interface WebServerController {
    /**
     * Register the controlled routes inside the app.
    * @param app the Javalin app where to register routes.
    */
    void registerRoutes(Javalin app);
}