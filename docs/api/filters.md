# pastely API Filters

pastely supports **powerful filtering** for pastes, folders, and other resources. Filters allow you to query specific content using **logical operators** and **field operators**.

Start with **examples**, then learn the syntax and operators.

## 🔹 Examples

### Simple Filter (HTTP)

```http
GET /paste?filter[visibility]=PUBLIC
```

Fetches all pastes with `visibility = PUBLIC`.


### Complex Filter (HTTP)

```http
GET /paste?filters={"$or":[{"visibility":"PUBLIC"},{"userId":{"$ne":"123"}}]}
```

* Returns pastes **either public** or **not by user `123`**
* `$or` combines multiple conditions
* `$ne` excludes specific values


### JavaScript Client Example

```javascript
const pastes = await pastely.getPastes({
    filters: {
        $or: [
            {'visibility': 'PUBLIC'},
            {'visibility': 'PRIVATE'}
        ]
    }
});
```

* Returns pastes that are **either public or private**
* Nested `$and` / `$or` can combine more conditions


## 🔹 Logical Operators

| Operator | Meaning                             | Example                                                                |
| -------- | ----------------------------------- | ---------------------------------------------------------------------- |
| `$and`   | All conditions must be true         | `{ "$and": [{ "visibility": "PUBLIC" }, { "userId": "123" }] }`        |
| `$or`    | At least one condition must be true | `{ "$or": [{ "visibility": "PUBLIC" }, { "visibility": "PRIVATE" }] }` |

> Logical operators can be **nested** to create complex queries.

---

## 🔹 Field Operators

| Operator   | Meaning               | Example                                     |
| ---------- | --------------------- | ------------------------------------------- |
| `$eq`      | Equals (default)      | `{ "visibility": { "$eq": "PUBLIC" } }`     |
| `$ne`      | Not equal             | `{ "userId": { "$ne": "123" } }`            |
| `$null`    | Field is null         | `{ "deletedAt": { "$null": true } }`        |
| `$notNull` | Field is not null     | `{ "deletedAt": { "$notNull": true } }`     |
| `$gt`      | Greater than          | `{ "createdAt": { "$gt": "2024-01-01" } }`  |
| `$gte`     | Greater or equal      | `{ "createdAt": { "$gte": "2024-01-01" } }` |
| `$lt`      | Less than             | `{ "createdAt": { "$lt": "2024-01-01" } }`  |
| `$lte`     | Less or equal         | `{ "createdAt": { "$lte": "2024-01-01" } }` |
| `$in`      | Value is in array     | `{ "tags": { "$in": ["tag1", "tag2"] } }`   |
| `$nin`     | Value is not in array | `{ "tags": { "$nin": ["tag3"] } }`          |

> If a field is provided without an operator, `$eq` is assumed.

---

## 🔹 Combining Filters

### Multiple Conditions (`$and`)

```javascript
pastely.getPastes({
    filters: {
        $and: [
            { visibility: "PUBLIC" },
            { userId: { "$ne": "123" } }
        ]
    }
});
```

* Only pastes that **match all conditions** are returned.

### Alternatives (`$or`)

```javascript
pastely.getPastes({
    $or: [
        { visibility: "PUBLIC" },
        { visibility: "PRIVATE" }
    ]
});
```

* Pastes that **match any condition** are returned.

### Nested Logic

```javascript
pastely.getPastes({
    filters: {
        $and: [
            { visibility: "PUBLIC" },
            { $or: [{ userId: "123" }, { userId: "456" }] }
        ]
    }
});
```

* Pastes must be **public**, and either **user 123 or 456**.

---

## 🔹 How It Works Internally

* **ORM (Database)**

    * `$eq` → `WHERE field = value`
    * `$ne` → `WHERE field != value`
    * `$gt` / `$gte` / `$lt` / `$lte` → comparison operators
    * `$null` / `$notNull` → NULL checks
    * `$and` / `$or` → nested query groups

* **Elasticsearch**

    * `$eq` → `term` query
    * `$ne` → `must_not term`
    * `$gt` / `$gte` / `$lt` / `$lte` → `range` query
    * `$null` / `$notNull` → `exists` / `must_not exists`
    * `$and` / `$or` → `bool` queries with `must` and `should`

> The same filter structure works for both database and Elasticsearch queries.