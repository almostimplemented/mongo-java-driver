/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.operation;

import org.bson.BsonDocument;

import static com.mongodb.assertions.Assertions.notNull;

public abstract class BaseUpdateRequest extends WriteRequest {
    private final BsonDocument filter;
    private boolean isUpsert = false;

    public BaseUpdateRequest(final BsonDocument filter) {
        super();
        this.filter = notNull("filter", filter);
    }

    public BsonDocument getFilter() {
        return filter;
    }

    public boolean isUpsert() {
        return isUpsert;
    }

    public BaseUpdateRequest upsert(final boolean isUpsert) {
        this.isUpsert = isUpsert;
        return this;
    }

    public abstract boolean isMulti();
}
