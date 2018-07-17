### 1.0.8

alter migration function to return nil if successful, `:ignore` or `:failure` when migrations are incomplete.
Add support for Thread cancellation during migrations.
Tests added for backout.

### 1.0.7

Update dependency on `org.clojure/tools.logging` to 0.4.1
Update dependency on `org.clojure/java.jdbc` to 0.7.7
Fix issue with handling directories that have spaces.

### 1.0.6

search Context classloader as fall back to system class loader for migration directory discovery

### 1.0.5
### 1.0.4

updated `migratus.migrations/timestamp` to use UTC

### 1.0.3

updated `pending-list` function to use `log/debug` as well as return names of the migrations as a vector.

### 0.9.2

### 0.9.2

Changedd `datetime` to `timestamp` as it's supported by more databases.

### 0.9.1

#### features

As of version 0.9.1 Migratus writes a human-readable description, and timestamp when the migration was applied.
This is a breaking change, as the schema for the migration table has changed. Users upgrading from pervious versions
need the following additional columns in the migrations table:

```clojure
[:applied "timestamp" "" ""]
[:description "VARCHAR(1024)" "" ""]
```

or

```sql
ALTER TABLE migratus.schema_migrations ADD COLUMN description varchar(1024);
--;;
ALTER TABLE migratus.schema_migrations ADD COLUMN applied timestamp with time zone;
```
