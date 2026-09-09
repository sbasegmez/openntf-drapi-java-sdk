# API Surface Semantics (draft)

Design draft mapping every operation in `openapi.basis.json` (v1.42.6) to a fluent Java call. Not yet implemented; this is the target shape for the SDK surface.

## Root rule

Every operation that requires a `dataSource` parameter lives under `client.dataSource(name)`. Everything else lives directly under `client`. The spec has no exceptions to this rule.

```java
DrapiClient client = DrapiClient.builder(config).build();
DrapiDataSource ds = client.dataSource("projects");

ds.documents().get(unid);
ds.documents().update(unid, document);
client.server().info();
```

Conventions used below:

- All calls return `CompletableFuture<T>` (as `DocumentsApi.get` does today).
- Optional parameters are collected in an `XxxOptions` value object with a builder, so every operation has a short overload and a full overload. Only the short one is listed.
- Request bodies with many fields are `XxxRequest` value objects with a builder; required fields are marked with `*`.
- Endpoints that are duplicates (GET and POST `/auth/logout`) are mapped once.
- `richTextAs` is an enum (`RichTextAs`, already in `org.openntf.drapi.meta`).

## Client level

| Fluent call                                                     | Operation                 | Endpoint                      |
|-----------------------------------------------------------------|---------------------------|-------------------------------|
| `client.dataSource(name)`                                       | (handle, no call)         |                               |
| `client.logout()`                                               | authLogout                | POST /auth/logout             |
| `client.server().info()`                                        | getInfo                   | GET /info                     |
| `client.server().userInfo()` / `userInfo(UserInfoOptions)`      | getUserInfo               | GET /userinfo                 |
| `client.server().userInfo(UserInfoRequest)`                     | getUserInfoPost           | POST /userinfo                |
| `client.server().richTextProcessors()`                          | getRichtextProcessors     | GET /richtextprocessors       |
| `client.server().previewFeatures()`                             | getPreviewFeatures        | GET /preview                  |
| `client.server().operationConstraints()`                        | getOperationConstrains    | GET /operations               |
| `client.scopes().list()` / `list(ScopeListOptions)`             | fetchScopes               | GET /scopes                   |
| `client.oauth().consents()`                                     | getCurrentUserConsents    | GET /consents                 |
| `client.oauth().revokeAllConsents()`                            | deleteCurrentUserConsents | DELETE /consents              |
| `client.oauth().consent(clientId)` / `consent(clientId, scope)` | getCurrUserConsent        | GET /consent/{client_id}      |
| `client.oauth().revokeConsent(unid)`                            | deleteCurrUserConsent     | DELETE /consent/revoke/{unid} |
| `client.oauth().apps()`                                         | fetchApps                 | GET /apps                     |
| `client.oauth().updateAppCallbackUrl(AppCallbackRequest)`       | updateCallbackUrl         | POST /apps                    |
| `client.dominoIq().completion(CompletionRequest)`               | DominoIQCompletion        | POST /dominoiq/completion     |
| `client.odata().scopes()`                                       | fetchOdataList            | GET /odata                    |
| (internal: AuthenticationProvider)                              | authLogin                 | POST /auth                    |
| (internal: AuthenticationProvider)                              | authRenewJwt              | POST /auth/extend             |
| (internal: AuthenticationProvider)                              | authLocal                 | GET /auth/local               |
| (internal: AuthenticationProvider)                              | loginForOAuthFlow         | POST /authforoauthflow        |
| (not exposed; `server().userInfo()` covers it)                  | authLoginBasic            | GET /auth/basic               |
| (not exposed; login-page concern)                               | getExternalIdp            | GET /auth/idpList             |

Options and request objects:

- `UserInfoOptions`: checkAccess (list of strings)
- `UserInfoRequest`: body is an unspecified JSON object in the spec; model as a map until the shape is documented
- `ScopeListOptions`: includeAll (boolean), skipIcon (boolean)
- `AppCallbackRequest`: clientId* (string), clientSecret* (string), redirectUri* (string)
- `CompletionRequest`: command* (string), payload* (string), server (string)

Notes:

- There is no public `AuthApi`. Login and renewal are handled automatically by the `AuthenticationProvider`; `/auth/basic` duplicates `/userinfo`; `/auth/idpList` serves login pages, not server-side clients.
- `client.logout()` asks the provider to release its session. Providers that obtained the JWT themselves (basic, OAuth) call `/auth/logout` and clear the token cache; the token provider was handed a token it did not create and must not invalidate it. Whether `close()` (AutoCloseable) should also call logout is open.
- `/operations` and `/preview` describe the server, not a database, so they sit under `server()`.

## Data source level

`ds = client.dataSource("projects")`

### Documents

| Fluent call                                                                                 | Operation            | Endpoint                          |
|---------------------------------------------------------------------------------------------|----------------------|-----------------------------------|
| `ds.documents().get(unid)` / `get(unid, GetOptions)`                                        | getDocument          | GET /document/{unid}              |
| `ds.documents().create(document)` / `create(document, CreateOptions)`                       | createDocument       | POST /document                    |
| `ds.documents().update(unid, document)` / `update(unid, document, UpdateOptions)`           | updateDocument       | PUT /document/{unid}              |
| `ds.documents().patch(unid, changes)` / `patch(unid, changes, UpdateOptions)`               | patchDocument        | PATCH /document/{unid}            |
| `ds.documents().delete(unid)` / `delete(unid, mode)`                                        | deleteDocument       | DELETE /document/{unid}           |
| `ds.documents().metadata(unid)`                                                             | getDocumentMetadata  | GET /docmeta/{unid}               |
| `ds.documents().modes(unid)`                                                                | getDocumentFormModes | GET /documentmodes/{unid}         |
| `ds.documents().richText(unid, richTextAs)` / `richText(unid, richTextAs, RichTextOptions)` | getRichText          | GET /richtext/{richTextAs}/{unid} |

Options objects:

- `GetOptions`: mode (string), meta (boolean), richTextAs (RichTextAs), markRead (boolean), markUnread (boolean)
- `CreateOptions`: richTextAs (RichTextAs), parentUnid (string)
- `UpdateOptions`: mode (string), parentUnid (string), revision (string), richTextAs (RichTextAs), markUnread (boolean)
- `RichTextOptions`: mode (string), item (string)

`update` replaces the document within the form mode; `patch` merges the given items and removes items sent as null.

### Raw documents (no data conversion)

Sub-namespace so the raw variants do not clutter the main interface.

| Fluent call                                                                  | Operation         | Endpoint        |
|------------------------------------------------------------------------------|-------------------|-----------------|
| `ds.documents().raw().get(unid)`                                             | getDocumentRaw    | GET /raw/{unid} |
| `ds.documents().raw().create(form, json)` / `create(form, json, parentUnid)` | createDocumentRaw | POST /raw       |
| `ds.documents().raw().update(unid, json)` / `update(unid, json, parentUnid)` | updateDocumentRaw | PUT /raw/{unid} |

Options objects: none beyond the optional `parentUnid` argument.

### Bulk

| Fluent call                                                                                 | Operation                  | Endpoint           |
|---------------------------------------------------------------------------------------------|----------------------------|--------------------|
| `ds.documents().bulk().create(documents)` / `create(documents, richTextAs)`                 | bulkCreateDocuments        | POST /bulk/create  |
| `ds.documents().bulk().get(unids)` / `get(unids, BulkGetOptions)`                           | bulkGetDocumentsByUnid     | POST /bulk/unid    |
| `ds.documents().bulk().update(BulkUpdateRequest)` / `update(BulkUpdateRequest, richTextAs)` | bulkUpdateDocumentsByQuery | PATCH /bulk/update |
| `ds.documents().bulk().delete(unids)` / `delete(unids, BulkOptions)`                        | bulkDeleteDocuments        | POST /bulk/delete  |
| `ds.documents().bulk().etags(unids)` / `etags(unids, BulkOptions)`                          | bulkDocumentEtagByUnid     | POST /bulk/etag    |

Options and request objects:

- `BulkOptions`: mode (string), markRead (boolean), markUnread (boolean)
- `BulkGetOptions`: mode (string), markRead (boolean), markUnread (boolean), meta (boolean), richTextAs (RichTextAs)
- `BulkUpdateRequest`: query* (string), replaceItems* (map), mode (string), variables (map), forms (list of strings), includeFormAlias (boolean), maxScanDocs (integer), maxScanEntries (integer), timeoutSecs (integer), viewRefresh (boolean), ftRefresh (boolean), noViews (boolean), start (integer), count (integer), returnUpdatedDocument (boolean)

`BulkGetOptions` could extend `BulkOptions`; the spec sends `mode`, `markRead` and `markUnread` in the body and `meta`, `richTextAs` as query parameters, but the caller need not know that.

### Query

Separate namespace. `query()` is the capability; the query language is the method name.

| Fluent call                                                                | Operation              | Endpoint             |
|----------------------------------------------------------------------------|------------------------|----------------------|
| `ds.query().dql(DqlQuery)` / `dql(DqlQuery, QueryOptions)`                 | query (action=execute) | POST /query          |
| `ds.query().explain(DqlQuery)`                                             | query (action=explain) | POST /query          |
| `ds.query().parse(DqlQuery)`                                               | query (action=parse)   | POST /query          |
| `ds.query().qrp(QrpQuery, layout)` / `qrp(QrpQuery, layout, count)`        | queryQrpJson           | POST /query/qrp/json |
| `ds.query().formula(FormulaQuery)` / `formula(FormulaQuery, QueryOptions)` | queryFormula           | POST /queryformula   |

Options and request objects:

- `DqlQuery`: query* (string), variables (map), mode (string), forms (list of strings), includeFormAlias (boolean), maxScanDocs (integer), maxScanEntries (integer), timeoutSecs (integer), viewRefresh (boolean), ftRefresh (boolean), noViews (boolean), markRead (boolean)
- `QrpQuery`: dql* (string), columns* (list of column definitions), returnUnid (boolean), returnReplicaId (boolean)
- `FormulaQuery`: query* (string), mode (string), forms (list of strings), includeFormAlias (boolean), markRead (boolean), since (ISO 8601 string), sinceSecondsAgo (integer)
- `QueryOptions`: count (integer), start (integer), richTextAs (RichTextAs)

The `action` enum is better expressed as three methods than as an option, since the response shape differs. `/bulk/update` stays under `documents().bulk()` although it is DQL-driven, because its outcome is an update rather than a result set.

### Attachments

Scoped to a document, so the unid is given once.

| Fluent call                                                                  | Operation                  | Endpoint                          |
|------------------------------------------------------------------------------|----------------------------|-----------------------------------|
| `ds.documents().attachments(unid).names()` / `names(AttachmentNamesOptions)` | getDocumentAttachmentNames | GET /attachmentnames/{unid}       |
| `ds.documents().attachments(unid).get(name)`                                 | getDocumentAttachment      | GET /attachments/{unid}/{name}    |
| `ds.documents().attachments(unid).upload(file)` / `upload(file, fieldName)`  | createDocumentAttachment   | POST /attachments/{unid}          |
| `ds.documents().attachments(unid).delete(name)` / `delete(name, fieldName)`  | deleteDocumentAttachment   | DELETE /attachments/{unid}/{name} |

Options objects:

- `AttachmentNamesOptions`: includeEmbedded (boolean), includeProtocolUrl (boolean), includeAttachmentMetadata (boolean)

`upload` is multipart/form-data; the spec allows several files per request, so an overload taking a list is possible. The alternative to the scoped form is a flat `ds.attachments().get(unid, name)`; the scoped form reads better and keeps the binary-handling code in one small class.

### Profile and named documents

Same shape for both; they differ only in the identifying parameter.

| Fluent call                                                                                       | Operation                   | Endpoint                 |
|---------------------------------------------------------------------------------------------------|-----------------------------|--------------------------|
| `ds.profileDocuments().get(profileName)` / `get(profileName, KeyedOptions)`                       | getProfileDocument          | GET /profiledocument     |
| `ds.profileDocuments().save(profileName, document)` / `save(profileName, document, KeyedOptions)` | createUpdateProfileDocument | POST /profiledocument    |
| `ds.profileDocuments().delete(profileName)` / `delete(profileName, KeyedOptions)`                 | deleteProfileDocument       | DELETE /profiledocument  |
| `ds.profileDocuments().list()` / `list(ProfileListOptions)`                                       | getProfileDocumentList      | GET /profiledocumentlist |
| `ds.namedDocuments().get(name)` / `get(name, KeyedOptions)`                                       | getNamedDocument            | GET /nameddocument       |
| `ds.namedDocuments().save(name, document)` / `save(name, document, KeyedOptions)`                 | createUpdateNamedDocument   | POST /nameddocument      |
| `ds.namedDocuments().delete(name)` / `delete(name, KeyedOptions)`                                 | deleteNamedDocument         | DELETE /nameddocument    |
| `ds.namedDocuments().list()` / `list(NamedListOptions)`                                           | getNamedDocumentList        | GET /nameddocumentlist   |

Options objects:

- `KeyedOptions`: key (string), user (boolean)
- `ProfileListOptions`: profileName (string), key (string), user (boolean)
- `NamedListOptions`: name (string), key (string), user (boolean)

`save` rather than `create`/`update` because the endpoint is create-or-update. `user=true` means the key is the current user's name.

### Lists (views and folders)

| Fluent call                                                                      | Operation                                | Endpoint               |
|----------------------------------------------------------------------------------|------------------------------------------|------------------------|
| `ds.lists().all()` / `all(ListsOptions)`                                         | fetchViews                               | GET /lists             |
| `ds.lists().get(name)` / `get(name, ListsGetOptions)`                            | fetchViewEntries                         | GET /lists/{name}      |
| `ds.lists().pivot(name, pivotColumn)` / `pivot(name, pivotColumn, PivotOptions)` | pivotViewEntries                         | GET /listspivot/{name} |
| `ds.lists().folder(name).add(unids)` / `add(unids, mode)`                        | bulkDocumentFolderByUnid (action=add)    | POST /bulk/folder      |
| `ds.lists().folder(name).remove(unids)` / `remove(unids, mode)`                  | bulkDocumentFolderByUnid (action=remove) | POST /bulk/folder      |

Options objects:

- `ListsOptions`: type (enum: all, folders, views), columns (boolean), filter (string)
- `ListsGetOptions`: mode (string), scope (enum: all, categories, documents), start (integer), count (integer), key (list of strings), keyType (enum: text, number, time), keyAllowPartial (boolean), startKey (string), untilKey (string), startsWith (string), category (list of strings), column (string), direction (enum: asc, desc), documents (boolean), meta (boolean), metaAdditional (boolean), richTextAs (RichTextAs), distinctDocuments (boolean), includeEmptyRows (boolean), ftSearchQuery (string), unreadOnly (boolean), markRead (boolean), markUnread (boolean)
- `PivotOptions`: mode (string), scope (enum: all, categories, documents), start (integer), count (integer), key (list of strings), startsWith (string), column (string), direction (enum: asc, desc)

`EntriesOptions` is the largest options object in the API and is the strongest case for the builder pattern. `PivotOptions` is a strict subset of it. `/bulk/folder` is placed here rather than under `documents().bulk()` because the caller is thinking about the folder, not the documents.

### Code execution

| Fluent call                                                                                             | Operation               | Endpoint                      |
|---------------------------------------------------------------------------------------------------------|-------------------------|-------------------------------|
| `ds.code().runAgent(agentName)` / `runAgent(agentName, payload)`                                        | executeAgent            | POST /run/agent               |
| `ds.code().runAgentWithContext(agentName, unids)` / `runAgentWithContext(agentName, unids, returnMode)` | executeAgentWithContext | POST /run/agentWithContext    |
| `ds.code().runAgentAsync(agentName)` / `runAgentAsync(AgentAsyncRequest)`                               | executeAgentAsync       | POST /run/agentAsync          |
| `ds.code().agentStatus(uuid)`                                                                           | getAgentInfo            | GET /run/agentAsync/{uuid}    |
| `ds.code().cancelAgent(uuid)`                                                                           | cancelAgent             | DELETE /run/agentAsync/{uuid} |
| `ds.code().run(code)` / `run(code, context)`                                                            | execute                 | POST /run/code                |
| `ds.code().formula(FormulaRequest)`                                                                     | runFormula              | POST /run/formula             |

Options and request objects:

- `AgentAsyncRequest`: agentName* (string), payload (map), runAsServer (boolean), callbackUrl (string), callbackUrlError (string), method (string)
- `FormulaRequest`: formula* (string), type* (enum: Domino, OpenFormula), user* (string, empty for current user), save* (boolean), unid (string), query (string), queryParameters (map)

`payload` for `runAgent` is a map. `context` for `run` is a data context object; the spec leaves its shape open. An alternative is `ds.agents()` with `run`, `runWithContext`, `runAsync`, `status`, `cancel`, plus a separate home for `/run/code` and `/run/formula`; `code()` keeps all seven together and matches the `/run` prefix in the spec.

### Scope and schema

| Fluent call                                | Operation      | Endpoint                      |
|--------------------------------------------|----------------|-------------------------------|
| `ds.scope().get()`                         | getScope       | GET /scope                    |
| `ds.scope().form(form)`                    | getScopeForm   | GET /scope/form/{form}        |
| `ds.scope().mode(form, mode)`              | fetchFormModes | GET /scope/mode/{form}/{mode} |
| `ds.scope().access()`                      | getScopeAccess | GET /scope/access             |
| `ds.scope().openApi()` / `openApi(voltmx)` | getOpenAPI     | GET /openapi                  |

Options objects: none beyond the optional `voltmx` boolean.

Note the singular: `client.scopes()` lists all scopes, `ds.scope()` describes this one. `/openapi` is tagged `data` in the spec but describes the data source, so it lives here. `ds.schema()` is the fallback name if the two prove too close in practice.

### OData (optional module)

| Fluent call                                                                      | Operation           | Endpoint                              |
|----------------------------------------------------------------------------------|---------------------|---------------------------------------|
| `ds.odata().serviceInfo()`                                                       | getOdataServiceInfo | GET /odata/{dataSource}               |
| `ds.odata().metadata()`                                                          | getOdataMetadata    | GET /odata/{dataSource}/$metadata     |
| `ds.odata().list(form)` / `list(form, ODataQuery)`                               | fetchOdata          | GET /odata/{dataSource}/{name}        |
| `ds.odata().create(form, entity)`                                                | createOdata         | POST /odata/{dataSource}/{name}       |
| `ds.odata().get(form, unid)` / `get(form, unid, select)`                         | getOdataItem        | GET /odata/{dataSource}/{name}/{unid} |
| `ds.odata().update(form, unid, entity)` / `update(form, unid, entity, revision)` | updateOdataDocument | PUT /odata/{dataSource}/{name}/{unid} |

Options objects:

- `ODataQuery`: select (string), filter (string), top (integer), skip (integer)
- OData version headers (`OData-Version`, `OData-MaxVersion`, `MaxDataServiceVersion`) are set by the module, not by the caller

## Coverage

All 77 operations are accounted for. Four are internal to the authentication provider and two (`authLoginBasic`, `getExternalIdp`) are deliberately not exposed. Two paths (`/auth/logout` GET and POST) collapse into one call.

## Resulting interfaces

Client level: `DrapiClient` (with `logout()`), `ServerApi`, `ScopesApi`, `OAuthApi`, `DominoIqApi`, `ODataApi` (root part).

Data source level: `DocumentsApi` (with `RawDocumentsApi`, `BulkDocumentsApi`, `DocumentAttachmentsApi` as sub-interfaces), `QueryApi`, `ProfileDocumentsApi`, `NamedDocumentsApi`, `ListsApi` (with `FolderApi`), `CodeApi`, `ScopeApi`, `ODataApi` (data source part).

---

Auto-generated by Claude (Cowork session, 3 September 2026) from openapi.basis.json v1.42.6. Reviewed and directed by Serdar Basegmez.
