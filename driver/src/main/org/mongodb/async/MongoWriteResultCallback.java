/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
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

package org.mongodb.async;

import org.mongodb.Decoder;
import org.mongodb.Document;
import org.mongodb.MongoException;
import org.mongodb.MongoNamespace;
import org.mongodb.command.GetLastError;
import org.mongodb.command.MongoCommandFailureException;
import org.mongodb.impl.MongoAsyncConnection;
import org.mongodb.operation.MongoWrite;
import org.mongodb.protocol.MongoRequestMessage;
import org.mongodb.result.CommandResult;
import org.mongodb.result.WriteResult;

public class MongoWriteResultCallback extends MongoCommandResultBaseCallback {
    private final SingleResultCallback<WriteResult> callback;
    private final MongoWrite writeOperation;
    private final GetLastError getLastError;
    private final MongoNamespace namespace;
    private final MongoRequestMessage nextMessage; // only used for batch inserts that need to be split into multiple messages

    public MongoWriteResultCallback(final SingleResultCallback<WriteResult> callback,
                                    final MongoWrite writeOperation, final GetLastError getLastError,
                                    final Decoder<Document> decoder, final MongoNamespace namespace,
                                    final MongoRequestMessage nextMessage,
                                    final MongoAsyncConnection connection) {
        super(getLastError, decoder, connection);
        this.callback = callback;
        this.writeOperation = writeOperation;
        this.getLastError = getLastError;
        this.namespace = namespace;
        this.nextMessage = nextMessage;
    }

    @Override
    protected void callCallback(final CommandResult commandResult, final MongoException e) {
        if (e != null) {
            callback.onResult(null, e);
        }
        else if (getLastError != null && !commandResult.isOk()) {
            callback.onResult(null, new MongoCommandFailureException(commandResult));
        }
        else {
            MongoCommandFailureException commandException = null;
            if (getLastError != null) {
                commandException = getLastError.getCommandException(commandResult);
            }

            if (commandException != null) {
                callback.onResult(null, commandException);
            }
            else if (nextMessage != null) {
                new GenericAsyncWriteOperation(namespace, writeOperation, nextMessage, getConnection().getBufferPool())
                        .execute(getConnection(), callback);
            }
            else {
                callback.onResult(new WriteResult(writeOperation, commandResult), null);
            }
        }
    }
}
