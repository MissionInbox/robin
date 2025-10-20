Prometheus Remote Write
=======================

Overview
--------
Robin can push its Micrometer metrics to a Prometheus-compatible backend using the Prometheus Remote Write protocol.
This is useful when Robin runs where scraping is not ideal (firewalled, ephemeral, or short-lived instances) or when you prefer a push-based flow.

- Push job: MetricsCron (started automatically during server startup)
- Registry source: Micrometer registries created by MetricsEndpoint
- Protocol: Prometheus Remote Write (protobuf) with Snappy framed compression (configurable)
- Transport: HTTP POST with appropriate headers

Where to configure
------------------
Create a file named `prometheus.json5` next to your `server.json5` (for example in `cfg/`).

- Typical location:
  - `cfg/prometheus.json5` (for default/container deployments)

Quick start
-----------
1) Create `cfg/prometheus.json5`:

```
{
  // Enable/disable Prometheus remote write push.
  enabled: false,

  // Your remote write endpoint (Prometheus Agent, VictoriaMetrics, Mimir/Thanos Receive, etc.).
  // Example (Prometheus Agent default): "http://localhost:9201/api/v1/write".
  remoteWriteUrl: "",

  // Push interval and HTTP timeout (seconds).
  intervalSeconds: 15,
  timeoutSeconds: 10,

  // Compress payload with Snappy framed (recommended by most receivers). Set to false to disable.
  compress: true,

  // Include/exclude filters (regex); metric names use '_' instead of '.'.
  include: ["^jvm_.*", "^process_.*", "^system_.*"],
  exclude: [],

  // Tip: Variables below are supported via Magic replacement.

  // Static labels added to every series.
  labels: {
    job: "robin",
    instance: "{$hostname}"
  },

  // Optional extra headers to include with the request.
  headers: {},

  // Authentication (choose one)
  bearerToken: "",
  basicAuthUser: "",
  basicAuthPassword: "",

  // Optional multi-tenancy header
  tenantHeaderName: "",
  tenantHeaderValue: "",
}
```

2) Start Robin normally. The push cron will begin after the metrics endpoint initializes.

3) Verify on your backend that series are received (check tenant/headers if using a multi-tenant setup).

Configuration reference (prometheus.json5)
-----------------------------------------
All properties are optional unless specified. Shown here with defaults when omitted.

- enabled: boolean (default false)
  - Enables the periodic remote write push.

- remoteWriteUrl: string (required when enabled)
  - The HTTP endpoint that accepts Prometheus Remote Write payloads.

- intervalSeconds: number (default 15)
  - Period between pushes.

- timeoutSeconds: number (default 10)
  - HTTP call timeout for a single push.

- compress: boolean (default true)
  - When true, payload is Snappy framed compressed and header `Content-Encoding: snappy` is added.
  - When false, payload is sent uncompressed and the `Content-Encoding` header is omitted.
  - Many receivers expect snappy; disable only if your receiver explicitly supports uncompressed Remote Write.

- labels: object<string,string> (default empty)
  - Static labels appended to every time series; `job` and `instance` are commonly used.

- headers: object<string,string> (default empty)
  - Additional HTTP headers to include (for example `X-Scope-OrgID`).

- bearerToken: string (default empty)
  - If set, adds `Authorization: Bearer <token>`.

- basicAuthUser: string (default empty)
- basicAuthPassword: string (default empty)
  - If set (and bearerToken is empty), adds `Authorization: Basic <base64(user:pass)>`.

- tenantHeaderName: string (default empty)
- tenantHeaderValue: string (default empty)
  - Convenience pair for a single tenant header. Equivalent to adding it under `headers`.

- include: array<string> (default empty)
  - Regex filters for metric names. If non-empty, only metrics matching at least one include pattern are sent.

- exclude: array<string> (default empty)
  - Regex filters to exclude metrics by name. Applied after includes.

Notes on metrics mapping
------------------------
- Metric names are derived from Micrometer ids with dots replaced by underscores: `a.b.c` -> `a_b_c`.
- Each Micrometer measurement (statistic) becomes a separate sample labeled with `stat` (e.g., `count`, `sum`, `value`).
- Timestamps are recorded in milliseconds since epoch.

Troubleshooting
---------------
- Nothing is pushed
  - Ensure `enabled: true` and `remoteWriteUrl` is set.
  - Check include/exclude filters. Filters are applied to normalized names (underscores, not dots).

- 415 Unsupported Media Type or compression errors
  - If your backend doesn’t support `Content-Encoding: snappy`, set `compress: false`.
  - If it requires snappy, keep `compress: true` (default).

- 401/403, 404
  - Configure auth and check paths as per your backend.

Security
--------
- Prefer `bearerToken` over Basic Auth when possible.
- Do not commit secrets into source control; use environment interpolation or a secret management approach if applicable in your environment.
- Be mindful of multi-tenant isolation; set the correct tenant header where required.

Operational notes
-----------------
- The cron is designed to be resilient: on errors it logs the issue and tries again at the next interval (no exponential backoff yet).
- Push is skipped if there are zero series after filtering.
- Remote write is independent of the `/prometheus` scrape endpoint; you can use both.

Change log
----------
- Added in version 1.3.2-SNAPSHOT: `MetricsCron` with Prometheus Remote Write and `prometheus.json5` configuration file.
