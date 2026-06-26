# sample-intent-multi-model

A **multi-model** Dirigible *intent* sample: a Billing domain split into six independent projects
that reference each other **across models**. Each subfolder is its own Dirigible project with a
single `*.intent` file at its root (the source of truth) - open it in the Intent Editor and click
**Generate** to produce the model files and then the running app.

It demonstrates three capabilities of the intent layer:

1. **Cross-model references** (`uses:` + a relation `model:`) - an entity reuses master data owned by
   another project instead of redefining it. The owner owns the single table; consumers store an
   integer FK and render a dropdown sourced from the owner's REST service (no duplicated table).
2. **Many-to-many via an explicit intermediate entity** - `SalesInvoiceCustomerPayment` links an
   invoice to a customer payment with a partial `amount`, where the two sides live in different
   projects.
3. **Faithful field attributes** - `unique`, `precision`/`scale`, `calculatedOnCreate`, and entity
   `audit: true`.

## The projects

| Project | Owns | References (cross-model) |
|---|---|---|
| [`uoms`](uoms/uoms.intent) | Dimension, UoM | - |
| [`countries`](countries/countries.intent) | Country | - |
| [`currencies`](currencies/currencies.intent) | Currency, CurrencyRate | - |
| [`customers`](customers/customers.intent) | Customer | Country, Currency |
| [`customer-payments`](customer-payments/customer-payments.intent) | CustomerPayment | Customer, Currency |
| [`sales-invoices`](sales-invoices/sales-invoices.intent) | SalesInvoice, SalesInvoiceItem, SalesInvoiceCustomerPayment, settings | Customer, Currency, UoM, CustomerPayment |

`SalesInvoice -> SalesInvoiceItem` is a 1:n composition (local). `SalesInvoice <-> CustomerPayment` is
the cross-model n:m, modelled by the `SalesInvoiceCustomerPayment` intermediate entity.

## How to run

1. Import all six projects into your workspace (clone this repo; each subfolder is a project).
2. **Generate leaf-first** so cross-model references resolve (each consumer reads the owner's
   generated `.model`):
   1. `uoms`, `countries`, `currencies`
   2. `customers`
   3. `customer-payments`
   4. `sales-invoices`
3. Run the model-to-code generation for each project and **publish all six** - cross-model dropdowns
   call the owner project's service (`/services/java/<ownerProject>/...`), so every owner must be live.

The default code-gen template is Java + Harmonia (Java DAO/REST + Alpine.js UI).

## Notes

- Project folder names equal each intent's `name:` (and so the table prefix and gen folder), which is
  why the `uses:` entries need no explicit `project:` - it defaults to the model alias.
- `SalesInvoice.number` uses a `calculatedOnCreate` expression. This stand-alone sample uses
  `java.util.UUID.randomUUID().toString()` so it compiles without an external service; in codbex the
  number is produced by `NumberGeneratorService` - swap the expression when that service is present.
