/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.remote;

import java.io.Serializable;

public class RemoteServiceResponse implements RRemoteServiceResponse, Serializable {

    private Object result;
    private Throwable error;
    
    public RemoteServiceResponse() {
    }
    
    public RemoteServiceResponse(Object result) {
        this.result = result;
    }

    public RemoteServiceResponse(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }
    
    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "RemoteServiceResponse [result=" + result + ", error=" + error + "]";
    }
    
}
