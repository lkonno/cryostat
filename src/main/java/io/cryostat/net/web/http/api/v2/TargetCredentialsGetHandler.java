/*
 * Copyright The Cryostat Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cryostat.net.web.http.api.v2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.cryostat.configuration.CredentialsManager;
import io.cryostat.core.log.Logger;
import io.cryostat.net.AuthManager;
import io.cryostat.net.security.ResourceAction;
import io.cryostat.net.web.DeprecatedApi;
import io.cryostat.net.web.http.HttpMimeType;
import io.cryostat.net.web.http.api.ApiVersion;
import io.cryostat.platform.ServiceRef;

import com.google.gson.Gson;
import io.vertx.core.http.HttpMethod;

@DeprecatedApi(
        deprecated = @Deprecated(forRemoval = false),
        alternateLocation = "/api/v2.2/credentials")
class TargetCredentialsGetHandler extends AbstractV2RequestHandler<List<ServiceRef>> {

    private final CredentialsManager credentialsManager;

    @Inject
    TargetCredentialsGetHandler(
            AuthManager auth, CredentialsManager credentialsManager, Gson gson, Logger logger) {
        super(auth, credentialsManager, gson);
        this.credentialsManager = credentialsManager;
    }

    @Override
    public boolean requiresAuthentication() {
        return true;
    }

    @Override
    public ApiVersion apiVersion() {
        return ApiVersion.V2_1;
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public Set<ResourceAction> resourceActions() {
        return EnumSet.of(ResourceAction.READ_CREDENTIALS);
    }

    @Override
    public String path() {
        return basePath() + "credentials";
    }

    @Override
    public List<HttpMimeType> produces() {
        return List.of(HttpMimeType.JSON);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public IntermediateResponse<List<ServiceRef>> handle(RequestParameters requestParams)
            throws Exception {
        return new IntermediateResponse<List<ServiceRef>>()
                .body(new ArrayList<>(this.credentialsManager.getServiceRefsWithCredentials()));
    }
}
