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

The [`navigation`](navigation/) project is **not** an intent project: it defines the shared-shell
navigation groups once (`getPerspectiveGroup()` for `master-data`, `sales`, `payments`, `settings`),
which the domain entities reference via `group:`.

## One shared shell (no app-hopping)

Each domain project generates its own standalone app shell (handy for running or testing a single
domain), **and** contributes its entities as grouped perspectives to the platform's shared shell.
Open the shared shell at **`/services/web/application/`** after publishing all projects and you get
**one** app with a single grouped sidebar:

- **Master Data** - UoM, Country, Currency, Customer
- **Sales** - Sales Invoices
- **Payments** - Customer Payments
- **Settings** - the nomenclatures (Number, statuses, methods)

Each entry opens that domain's screen embedded in the one shell, so the user never jumps between
per-project UIs. (Grouping is driven by `group:` on each entity + the `navigation` project; it
requires the intent shared-shell support, PRs eclipse-dirigible/dirigible#6089 and #6090.)

## Clone, generate and run — step by step

1. **Start Dirigible** and open the IDE at `http://localhost:8080` (default credentials `admin` / `admin`).
2. **Clone this repo into your workspace.** In the IDE's **Git** perspective use
   `https://github.com/dirigiblelabs/sample-intent-multi-model.git`; each top-level subfolder
   (`uoms`, `countries`, `currencies`, `customers`, `customer-payments`, `sales-invoices`,
   `navigation`) becomes a project.
3. **Generate the model files, leaf-first** so cross-model references resolve (each consumer reads the
   owner's already-generated `.model`). For each project, double-click its `*.intent`, then click
   **Generate** in the Intent Editor, in this order:
   1. `uoms`, `countries`, `currencies`
   2. `customers`
   3. `customer-payments`
   4. `sales-invoices`

   (The `navigation` project has no intent — it just declares the sidebar groups and is published
   as-is.) Generate writes the `.edm`/`.model`/`.bpmn`/`.form`/`.report`/`.roles`/`.csvim` next to
   each intent, and chains the model-to-code generation (Java DAO/REST + Alpine.js Harmonia UI — the
   default recipe).
4. **Publish everything** (Workbench → Publish All). Cross-model dropdowns call the owner project's
   REST service (`/services/java/<ownerProject>/...`), so every owner must be live; the `.csvim`
   seeds load the nomenclatures (payment methods, sent methods, invoice statuses, …).
5. **Open the shared shell:** `http://localhost:8080/services/web/application/`. You land on a
   dashboard with one KPI tile per entity and a single grouped sidebar (Master Data / Sales /
   Payments / Settings) — every app embedded in this one shell.

## Walkthrough — the sales-invoice lifecycle

This is the end-to-end flow the sample is built to show. The `SalesInvoiceApproval` process
(`trigger: { onCreate: SalesInvoice }`) walks an invoice through **Approve → Issue → Send**, with a
**Reject** branch that cancels it.

1. **Create a customer** — either up front via **Master Data → Customer → New** (fill name, country,
   currency, save), **or inline while creating the invoice**: the invoice's **Customer** dropdown has
   a **New** action that opens the Customer create form in a dialog and selects the new record on
   save (this is a cross-model link — the customer lives in the `customers` project).
2. **New invoice — the create (header) form.** Go to **Sales → Sales Invoices → New**. You get the
   document **header**: `Number` (auto-filled by a `calculatedOnCreate` expression, read-only), `Date`
   (required), `Due`, `Customer`, `Currency`, payment/sent method. The totals (Net/Vat/Total) are
   still **0** — there are no line items yet.
3. **Create.** Saving the header does two things: the page switches to **edit mode** and the
   **line-items table appears** (you can only add items once the header exists), and — because the
   process triggers `onCreate` — the **`SalesInvoiceApproval` process starts** and an **Approve** task
   is created for this invoice (DRAFT, status 1).
4. **Add an item.** In the items table click **Add** and enter name, **quantity**, **price**,
   discount, UoM. The line's **Net = Quantity × Price**, **Vat = round(Net × 0.2, 2)** and
   **Total = Net + Vat − Discount** are calculated live; saving the line **recomputes the invoice's
   header totals** (Net/Vat/Total) synchronously. Add as many lines as you need.
5. **Save** the document.
6. **Approve (or Reject).** Open the invoice's **Approve Sales Invoice** task (from the record's
   inline tasks or the **Process Inbox**). It's a **read-only** card showing the invoice's **current**
   values — `Number`, `Date`, `Customer` (by name), and `Total` now reflecting the items you added
   (not the 0 it had at creation), plus `Status`. Three buttons: **Approve** (green), **Reject** (red),
   **Close** (just dismisses the form, leaving the task open).
   - **Approve** → the decision continues to **Issue**; the invoice becomes **APPROVED**.
   - **Reject** → the invoice is **CANCELLED** and the process ends.
7. **Issue.** After approval, the **Issue Sales Invoice** task appears (read-only, **Issue** button in
   blue). Completing it marks the invoice **ISSUED** and continues to **Send**. (In a full codbex
   setup the definitive invoice number is generated at this step.)
8. **Send.** The **Send Sales Invoice** task shows the `Sent Method`; the **Send** button marks the
   invoice **SENT** and the process ends.

Throughout, each task form **fetches the live invoice** when you open it (the process context holds
only the invoice id), so it always shows current data — the total you see on the Approve form already
includes items added after the task was created.

## Notes for running a single domain

The default code-gen template is Java + Harmonia. Each project also generates its **own** standalone
SPA at `/services/web/<project>/gen/<genFolder>/index.html`, useful for testing one domain in
isolation; the shared shell at `/services/web/application/` simply aggregates them.

## Notes

- Project folder names equal each intent's `name:` (and so the table prefix and gen folder), which is
  why the `uses:` entries need no explicit `project:` - it defaults to the model alias.
- `SalesInvoice.number` uses a `calculatedOnCreate` expression. This stand-alone sample uses
  `java.util.UUID.randomUUID().toString()` so it compiles without an external service; in codbex the
  number is produced by `NumberGeneratorService` - swap the expression when that service is present.
