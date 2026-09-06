/*
 * Copyright (c) 2026 Serdar Basegmez
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
module org.openntf.drapi.sample {

    requires org.openntf.drapi;

    // org.openntf.json.jakarta is never imported directly - it is pulled into the runtime module graph automatically because it
    // provides the JsonBindingProvider service that org.openntf.drapi's module-info declares with "uses". It still needs to be on
    // the module path at run time.

    // DrapiConfigBuilder.applyResourceFile() (in org.openntf.drapi) reads config/drapi-sdk-sample.properties via
    // ClassLoader.getResourceAsStream(), which respects module resource encapsulation - a resource is only visible across module
    // boundaries if the module that packages it opens the containing directory, regardless of which module's code requests it.
    opens config;

}
