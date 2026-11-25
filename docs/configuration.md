# Configuration

Configuration is read from, in increasing priority:

1. `src/main/resources/application.yml` – defaults for all environments.
2. `src/main/resources/application-cloud.yml` – overrides for the `cloud` environment (ECS).
3. Environment variables and system properties.
4. AWS Systems Manager Parameter Store (only in the `cloud` environment), see below.

There are no `bootstrap*.yml` files any more. Micronaut 5 deprecates the bootstrap context and the
`ConfigurationClient`; do not reintroduce them.

## AWS Parameter Store

Parameters are imported with `micronaut.config.import` in `application-cloud.yml`:

```yaml
micronaut:
  config:
    import:
      - provider: parameterstore
        path: /trafficinfo/config/application        # shared by all trafficinfo services
        use-secure-parameters: true
      - provider: parameterstore
        path: /trafficinfo/config/baseline-micronaut  # this service only
        use-secure-parameters: true
```

When you create a new service from this baseline, change the second path to the service's own
folder in Parameter Store.

### Caveats

- **Only the listed paths are read.** The old config client also read implicit paths such as
  `<root>/application_<env>` and `<root>/<app>_<env>`. Imports don't. Add an entry for every folder
  the service needs.
- **A wrong or empty path fails silently.** Unless `optional: true` is set, a missing path still
  gives no error; the service simply starts without those values. Check that the folder has keys
  (see the commands below).
- **Import options go on each entry.** The importer runs in an isolated context and does not read
  `aws.client.system-manager.parameterstore.*` or `aws.region` from the rest of the config. Put
  `use-secure-parameters`, `region`, credentials and retry options on each import entry. Without
  `use-secure-parameters: true`, SecureString values come back encrypted.
- **Use the structured form shown above.** The string form `parameterstore:/path` fails at
  startup with `Connection string must declare protocol://`.
- **On duplicate keys the last entry wins.** All imports get the same priority, so a key set in
  two folders takes its value from the entry listed last. Keep the service's own path last so it
  overrides the shared one, as the old config client did. Example: `jwksUrl` is set in both
  `/trafficinfo/config/application` and `/trafficinfo/config/baseline-micronaut` with
  **different** values (checked in prod, Nov 2026). The service uses the value from
  `baseline-micronaut`. Swapping the two entries silently changes which Cognito user pool tokens
  are validated against.
- **Values are read once at startup.** There is no refresh, and `RefreshEvent` listeners do not
  fire for Parameter Store values. Restart the service (new ECS deployment) to pick up a change.
- **Imported values override environment variables**, system properties and the YAML files.
- **Parameters directly under `/trafficinfo/config`** (e.g. `elasticcloud.*`) are not inside any
  imported folder and are not read.

### Checking Parameter Store

List the folders, the keys in each imported folder, and keys set in both (duplicates):

```bash
AWS_PROFILE=<profile> aws ssm get-parameters-by-path --path /trafficinfo/config --recursive \
  --query "Parameters[].Name" --output text | tr '\t' '\n' | cut -d/ -f4 | sort -u

list() { AWS_PROFILE=<profile> aws ssm get-parameters-by-path --path "/trafficinfo/config/$1" --recursive \
  --query "Parameters[].Name" --output text | tr '\t' '\n' | sed "s|^/trafficinfo/config/$1/||" | sort; }
list application
list baseline-micronaut
comm -12 <(list application) <(list baseline-micronaut)
```

In zsh, put `AWS_PROFILE=...` directly in front of the `aws` command (as above) or `export` it
first. `AWS_PROFILE=x list` does not work in zsh. Check every account (dev, test, stage, prod),
since the values can differ between them.

### Verifying after deploy

At startup, `ApplicationConfigLogger` logs all property sources (imported ones are named after
their path) and an **"Effective configuration"** section with the resolved value of every key.
Only allowlisted values are shown (see [Configuration logging](#configuration-logging)). After a
deploy, check that:

- the imported property sources are present and contain keys,
- `jwksUrl` and the other service URLs show the expected values,
- there are no `<unresolvable…>` entries.

The raw property-source dump shows placeholders such as `${jwksUrl}` unresolved, so use the
"Effective configuration" section to check the values.

## Configuration logging

`ApplicationConfigLogger` logs the configuration at startup. To avoid leaking secrets it uses an
**allowlist** (`ConfigValueMasker.ALLOWED_KEYS`):

- **Values are only logged for allowlisted keys.** All other keys are still listed, with
  `<hidden>` as the value, so the log shows which keys are set and where they come from.
- **The OS environment (`env`) and system properties (`system`) are never logged**, only how many
  keys they contain. Containers often get secrets as environment variables under arbitrary names.
- **Credentials in URLs are removed even for allowlisted keys**
  (`postgres://user:pass@db` → `postgres://<hidden>@db`).
- `<unresolvable>` errors only include the error message for allowlisted keys, since the message
  can contain the raw value.

A denylist of "secret-looking" key names was used before. It leaked secrets with unexpected names
(`DB_PASS`, `PRIVATE_PEM`, credentials inside URLs), printed the whole OS environment, and hid
harmless values such as JWKS URLs because their keys contain `token`. With the allowlist,
forgetting a key hides a value instead of leaking a secret.

### Adding keys to the allowlist

When a value you need for troubleshooting shows as `<hidden>`, add its key to `ALLOWED_KEYS` in
`ConfigValueMasker.kt`:

- Only add keys whose value is **never** secret, in any environment.
- Never add keys that can contain credentials: connection strings, client ids/secrets, passwords,
  tokens, key stores, or anything under an `ssl` section.
- Prefer exact keys over wildcards. `*` matches exactly one key segment
  (`micronaut.http.services.*.url`), `**` any number of segments (`jackson.**`). Matching is
  case-insensitive, and list indexes (`[0]`) are ignored.
- Never allowlist `env` or `system` values.
- Extend `ConfigValueMaskerSpec` if you add wildcard patterns.

### Startup script (`Docker/runapp.sh`)

The script runs with `set -x`, so every command it executes, including the full `java` command
line, is printed to the container log (and CloudWatch). The JVM command line is also readable
from `/proc` inside the container, and `-D` options become system properties.

- **Never pass secrets as command-line arguments or `-D` options.** Pass them as environment
  variables and set them **before** `set -x`.
- The APM token is configured this way: the task definition sets `APM_SECRET_TOKEN`, and the
  script exports it as `ELASTIC_APM_SECRET_TOKEN` (read natively by the Elastic APM agent) before
  `set -x`. The agent logs it masked (`secret_token: 'XXXX' (source: Environment Variables)`).
- Other `-Delastic.apm.*` options (service name, server URL, environment) are not secret and stay
  on the command line.

## JWT validation

Incoming Bearer tokens are validated against two JWKS endpoints
(`micronaut.security.token.jwt.signatures.jwks.*` in `application.yml`):

| Name                 | Placeholder             | Source                                                                 |
|----------------------|-------------------------|------------------------------------------------------------------------|
| `awscognito`         | `${jwksUrl}`            | Parameter Store (see the duplicate-key caveat above); local default in `application.yml` |
| `trafficinfoCognito` | `${trafficinfoJwksUrl}` | **Not in Parameter Store**, so the default in `application.yml` is used in every environment |

Parameter Store has a `centralJwksUrl` in `/trafficinfo/config/application`, but the service does
not read it.

## Metrics

`micronaut.metrics.enabled` is `false` in `application.yml`, so metrics (including CloudWatch
export) are off unless Parameter Store turns them on. `application-cloud.yml` only sets the
CloudWatch namespace and batch size.

## Jackson (JSON)

Micronaut 5 uses Jackson 3 and renamed its configuration keys. **Old keys are silently ignored.**

| Micronaut 4                          | Micronaut 5                          |
|--------------------------------------|--------------------------------------|
| `jackson.serialization.*`            | `jackson.serialization-features.*`   |
| `jackson.deserialization.*`          | `jackson.deserialization-features.*` |
| `writeDatesAsTimestamps`, `adjustDatesToContextTimeZone` | `jackson.date-time-features.*` |

`JacksonConfigSpec` checks that these settings work (offsets kept, ISO dates, unknown properties
ignored, indented output). Extend it when you add Jackson settings.

Jackson 3 also changed a default: `FAIL_ON_NULL_FOR_PRIMITIVES` is now `true`, so a missing or
`null` JSON field mapped to a Kotlin `Int`/`Boolean` fails instead of becoming `0`/`false`. To get
the old behaviour, set `jackson.deserialization-features.failOnNullForPrimitives: false`.

In code, use `tools.jackson.*` (e.g. `tools.jackson.databind.ObjectMapper`). Annotations such as
`@JsonProperty` stay in `com.fasterxml.jackson.annotation`. An injected
`com.fasterxml.jackson.databind.ObjectMapper` (Jackson 2) is **not** configured by `jackson.*` and
is not provided by Micronaut 5.

## Unknown configuration keys

Micronaut ignores keys it does not recognise, so a typo or a key renamed in a new version has no
effect and gives no error. Check keys against the configuration reference of the module's
**versioned** guide, e.g. `https://micronaut-projects.github.io/micronaut-security/<version>/guide/`.
For keys that matter, add a test that fails if the key were ignored.
