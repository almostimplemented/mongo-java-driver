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

package org.mongodb.io.async;

import org.mongodb.MongoCredential;
import org.mongodb.MongoException;
import org.mongodb.async.AsyncCommandOperation;
import org.mongodb.async.SingleResultCallback;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.codecs.PrimitiveCodecs;
import org.mongodb.command.MongoCommand;
import org.mongodb.impl.MongoAsyncConnection;
import org.mongodb.io.NativeAuthenticationHelper;
import org.mongodb.result.CommandResult;

public class NativeAsyncAuthenticator extends AsyncAuthenticator {
    NativeAsyncAuthenticator(final MongoCredential credential, final MongoAsyncConnection connection) {
        super(credential, connection);
    }

    @Override
    public void authenticate(final SingleResultCallback<CommandResult> callback) {
        new AsyncCommandOperation(getCredential().getSource(),
                new MongoCommand(NativeAuthenticationHelper.getNonceCommand()),
                new DocumentCodec(PrimitiveCodecs.createDefault()), getConnection().getBufferPool()).execute(getConnection())
                .register(new SingleResultCallback<CommandResult>() {
                    @Override
                    public void onResult(final CommandResult result, final MongoException e) {
                        if (e != null) {
                            callback.onResult(result, e);
                        }
                        else {
                            new AsyncCommandOperation(getCredential().getSource(),
                                    new MongoCommand(NativeAuthenticationHelper.getAuthCommand(getCredential().getUserName(),
                                            getCredential().getPassword(), (String) result.getResponse().get("nonce"))),
                                    new DocumentCodec(PrimitiveCodecs.createDefault()), getConnection().getBufferPool())
                                    .execute(getConnection()).register(new SingleResultCallback<CommandResult>() {
                                @Override
                                public void onResult(final CommandResult result, final MongoException e) {
                                    callback.onResult(result, e);
                                }
                            });
                        }
                    }
                });
    }
}
